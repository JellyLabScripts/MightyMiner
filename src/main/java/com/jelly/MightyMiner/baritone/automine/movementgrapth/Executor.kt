package com.jelly.MightyMiner.baritone.automine.movementgrapth

import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathMode
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig
import com.jelly.MightyMiner.baritone.automine.config.MiningType
import com.jelly.MightyMiner.baritone.automine.structures.BlockNode
import com.jelly.MightyMiner.baritone.automine.structures.BlockType
import com.jelly.MightyMiner.baritone.automine.structures.Path
import com.jelly.MightyMiner.handlers.KeybindHandler
import com.jelly.MightyMiner.player.ContinuousRotator
import com.jelly.MightyMiner.render.BlockRenderer
import com.jelly.MightyMiner.utils.AngleUtils
import com.jelly.MightyMiner.utils.BlockUtils
import com.jelly.MightyMiner.utils.PlayerUtils
import net.minecraft.client.Minecraft
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderGameOverlayEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Color
import java.util.*
import kotlin.math.roundToInt

class Executor {
    private lateinit var path: Path
    private lateinit var config: BaritoneConfig
    var mc: Minecraft = Minecraft.getMinecraft()
    private var stuckTickCount = 0
    private var jumpFlag = false
    private var jumpCooldown = 0
    private var rotatorContext = ContinuousRotator()
    private var deltaJumpTick = 0
    private var blocksToMine: LinkedList<BlockNode> = LinkedList()
    private var minedBlocks = LinkedList<BlockNode>()
    private var blockRenderer = BlockRenderer()
    private var currentState: PlayerState = PlayerState.IDLE
    private var shouldGoToFinalBlock = false

    enum class PlayerState {
        IDLE, WALKING, MINING, FAILED, FINISHED
    }

    fun executePath(path: Path, config: BaritoneConfig) {
        this.path = path
        this.config = config
        if (path.blocksInPath.isEmpty()) {
            fail()
            return
        }
        shouldGoToFinalBlock = path.mode == PathMode.GOTO
        //   pathSize = path.getBlocksInPath().size();
        blocksToMine = path.blocksInPath
        currentState = PlayerState.IDLE
        stuckTickCount = 0
        deltaJumpTick = 0
        jumpCooldown = 0
        jumpFlag = false
        minedBlocks.clear()
        blockRenderer.renderMap.clear()
        blockRenderer.renderMap[path.blocksInPath.first.pos] = Color.RED

        for (i in path.blocksInPath) {
            blockRenderer.renderMap[i.pos] = Color.ORANGE
        }

        MinecraftForge.EVENT_BUS.register(this)
        updateState()
    }

    val isExecuting: Boolean
        get() = currentState == PlayerState.MINING || currentState == PlayerState.WALKING

    val hasFailed: Boolean
        get() = currentState == PlayerState.FAILED

    fun hasSuccessfullyFinished(): Boolean {
        return currentState == PlayerState.FINISHED
    }

    fun reset() {
        currentState = PlayerState.IDLE
        unregister()
    }

    fun disable() {
        currentState = PlayerState.FINISHED
        rotatorContext.canceled = true
        unregister()
    }

    private fun fail() {
        currentState = PlayerState.FAILED
        rotatorContext.canceled = true
        unregister()
    }

    fun unregister() {
        MinecraftForge.EVENT_BUS.unregister(this)
    }

    @SubscribeEvent
    fun onTickEvent(event: ClientTickEvent) {
        if (mc.thePlayer == null || event.phase != TickEvent.Phase.START || currentState == PlayerState.FAILED || currentState == PlayerState.FINISHED) {
            return
        }
        if (!blocksToMine.isEmpty() && shouldRemoveFromList(blocksToMine.last)) {
            stuckTickCount = 0
            minedBlocks.add(blocksToMine.last)
            blockRenderer.renderMap.remove(blocksToMine.last.pos)
            blocksToMine.removeLast()
        } else {
            stuckTickCount++
            if (stuckTickCount > 20 * config.restartTimeThreshold) {
                fail()
                return
            }
        }
        if (blocksToMine.isEmpty() || BlockUtils.isPassable(blocksToMine.first.pos) && blocksToMine.first.type == BlockType.MINE) {
            if (!shouldGoToFinalBlock || !minedBlocks.isEmpty() && BlockUtils.getPlayerLoc() == minedBlocks.last.pos) {
                disable()
                return
            }
        }
        updateState()
        when (currentState) {
            PlayerState.WALKING -> {
                val targetWalkBlock =
                    if (blocksToMine.isEmpty() || blocksToMine.last.type == BlockType.MINE) minedBlocks.last.pos else blocksToMine.last.pos
                val reqYaw = AngleUtils.getRequiredYawCenter(targetWalkBlock)
                rotatorContext.changeGoal(reqYaw, mc.thePlayer.rotationPitch)
                if (// is on ground
                    !jumpFlag && mc.thePlayer.posY - mc.thePlayer.lastTickPosY == 0.0 && mc.thePlayer.onGround && jumpCooldown == 0) {
                    if (targetWalkBlock.y > mc.thePlayer.posY) {
                        jumpFlag = true
                        jumpCooldown = 10
                    }
                }
                KeybindHandler.updateKeys(
                    AngleUtils.getAngleDifference(reqYaw, AngleUtils.getActualRotationYaw()) < -4 * 10 + 45,
                    AngleUtils.getAngleDifference(reqYaw, AngleUtils.getActualRotationYaw()) >= 45,
                    false, false, false, false, false,
                    jumpFlag
                )
                jumpFlag = false
                if (jumpCooldown > 0) {
                    jumpCooldown--
                }
            }

            PlayerState.MINING -> {
                val targetMineBlock = blocksToMine.last.pos
                mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbar("Pick", "Drill", "Gauntlet")
                KeybindHandler.updateKeys(
                    false, false, false, false,
                    mc.objectMouseOver != null && mc.objectMouseOver.blockPos != null && mc.objectMouseOver.blockPos == targetMineBlock,
                    false,
                    config.isShiftWhenMine,
                    false
                )
                if (mc.objectMouseOver != null && mc.objectMouseOver.blockPos != null) {
                    // special cases for optimization
                    if (BlockUtils.isAdjacentXZ(
                            targetMineBlock,
                            BlockUtils.getPlayerLoc()
                        ) && !AngleUtils.shouldLookAtCenter(targetMineBlock) &&
                        (targetMineBlock.y - mc.thePlayer.posY == 0.0 && BlockUtils.getBlock(targetMineBlock.up()) == Blocks.air || targetMineBlock.y - mc.thePlayer.posY == 1.0)
                    ) {
                        rotatorContext.changeGoal(AngleUtils.getRequiredYaw(targetMineBlock), 28f)
                    } else if (!BlockUtils.isPassable(targetMineBlock)) rotatorContext.changeGoal(
                        AngleUtils.getRequiredYaw(
                            targetMineBlock
                        ), AngleUtils.getRequiredPitch(targetMineBlock)
                    )
                }
            }

            else -> {}
        }
        if (deltaJumpTick > 0) {
            deltaJumpTick--
        }
    }

    @SubscribeEvent
    fun onRenderEvent(event: RenderWorldLastEvent?) {
        blockRenderer.renderAABB(event)
        if (!rotatorContext.canceled) {
            rotatorContext.update()
        }
    }

    @SubscribeEvent
    fun onOverlayRenderEvent(event: RenderGameOverlayEvent) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return


        if (!blocksToMine.isEmpty()) {
            for (i in blocksToMine.indices) {
                mc.fontRendererObj.drawString(
                    "${blocksToMine[i].pos} ${blocksToMine[i].type}",
                    5,
                    5 + 10 * i,
                    -1
                )
            }
        }

        mc.fontRendererObj.drawString(currentState.toString(), 300, 5, -1)
    }

    private fun updateState() {
        if (config.mineType == MiningType.STATIC) {
            currentState = PlayerState.MINING
            return
        }
        if (shouldGoToFinalBlock && blocksToMine.isEmpty()) {
            currentState = PlayerState.WALKING
            return
        }
        if (blocksToMine.isEmpty()) return
        if (minedBlocks.isEmpty()) {
            currentState = if (blocksToMine.last.type == BlockType.MINE) PlayerState.MINING else PlayerState.WALKING
            return
        }
        if (blocksToMine.last.type == BlockType.WALK) {
            currentState = PlayerState.WALKING
            return
        }
        when (currentState) {
            PlayerState.IDLE -> currentState =
                if (blocksToMine.last.type == BlockType.MINE) PlayerState.MINING else PlayerState.WALKING

            PlayerState.WALKING -> if (minedBlocks.last.type == BlockType.WALK && blocksToMine.last.type == BlockType.MINE || minedBlocks.last.type == BlockType.MINE && BlockUtils.onTheSameXZ(
                    minedBlocks.last.pos,
                    BlockUtils.getPlayerLoc()
                )
            ) currentState = PlayerState.MINING

            PlayerState.MINING -> if (blocksToMine.last.type == BlockType.MINE && shouldWalkTo(minedBlocks.last.pos)) currentState =
                PlayerState.WALKING
        }
    }

    private fun shouldRemoveFromList(lastBlockNode: BlockNode): Boolean {
        return if (lastBlockNode.type == BlockType.MINE) BlockUtils.isPassable(lastBlockNode.pos) || BlockUtils.getBlock(lastBlockNode.pos) == Blocks.bedrock else BlockUtils.onTheSameXZ(lastBlockNode.pos, BlockUtils.getPlayerLoc()) || !BlockUtils.fitsPlayer(lastBlockNode.pos.down())
    }

    private fun shouldWalkTo(blockPos: BlockPos): Boolean {
        return  /*lockPos.getY() <= (mc.thePlayer.posY) + 1 &&*/((blockPos.y > mc.thePlayer.posY.roundToInt() && BlockUtils.isPassable(
            BlockUtils.getPlayerLoc().up(2)
        ) || blockPos.y < mc.thePlayer.posY.roundToInt() && BlockUtils.isPassable(blockPos.up(2)) || blockPos.y == mc.thePlayer.posY.roundToInt())
                && (BlockUtils.fitsPlayer(blockPos.down()) || BlockUtils.fitsPlayer(blockPos.down(2)))
                && !BlockUtils.onTheSameXZ(blockPos, BlockUtils.getPlayerLoc()))
    }
}