package com.jelly.mightyminerv2.pathfinder.calculate.path

import com.jelly.mightyminerv2.handler.RotationHandler
import com.jelly.mightyminerv2.util.*
import com.jelly.mightyminerv2.util.helper.Angle
import com.jelly.mightyminerv2.util.helper.Clock
import com.jelly.mightyminerv2.util.helper.RotationConfiguration
import com.jelly.mightyminerv2.pathfinder.calculate.Path
import com.jelly.mightyminerv2.pathfinder.util.gameSettings
import com.jelly.mightyminerv2.pathfinder.util.mc
import com.jelly.mightyminerv2.pathfinder.util.player
import com.jelly.mightyminerv2.pathfinder.util.world
import net.minecraft.util.BlockPos
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Color
import java.util.LinkedList

object PathExecutor {
    var enabled = false
    val pathQueue = LinkedList<Path>()
    val path = mutableListOf<BlockPos>()

    // to avoid looping over the whole list every time since im a schizophrenic fuck. id take speed over memory any day
    val hashedPath = linkedMapOf<Pair<Int, Int>, MutableList<Pair<Int, Int>>>()
    var targetIndex = 0
    var lastIndex = -1
    val timer = Clock()
    var failed = false
    var succeeded = false;

    fun queuePath(path: Path) {
        if (path.path.isEmpty()) {
            Logger.sendMessage("Path is empty")
            return
        }
        this.pathQueue.offer(path)
    }

    fun start(): Boolean {
        if (this.pathQueue.isEmpty()) {
            Logger.sendMessage("Path queue is empty. Not starting")
            this.failed = !this.enabled;
            this.succeeded = this.enabled
            return false
        }

        // Soft Reset
        this.failed = false;
        this.succeeded = false;
        this.path.clear()
        this.hashedPath.clear()
        this.targetIndex = 0;
        this.lastIndex = -1;
        this.timer.reset()
        RotationHandler.getInstance().stop()

        val smoothPath = this.pathQueue.poll().getSmoothedPath()
        this.path.addAll(smoothPath)
        for (i in smoothPath.indices) {
            val block = smoothPath[i]
            hashedPath.computeIfAbsent(Pair(block.x, block.z)) { mutableListOf() }
                .add(Pair(block.y, i))
        }
        this.enabled = true
        return true
    }

    fun start(path: Path) {
        if (path.path.isEmpty()) {
            Logger.sendMessage("Path is empty")
            this.failed = true;
            return
        }
        this.failed = false;
        this.succeeded = false;
        this.path.clear()
        this.hashedPath.clear()
        val smoothPath = path.getSmoothedPath()
        this.path.addAll(smoothPath);
        for (i in smoothPath.indices) {
            val block = smoothPath[i]
            hashedPath.computeIfAbsent(Pair(block.x, block.z)) { mutableListOf() }
                .add(Pair(block.y, i))
        }
        this.enabled = true;
        Logger.sendMessage("Started PathExecutor")
    }

    fun stop() {
        this.enabled = false;
        this.path.clear();
        this.hashedPath.clear();
        this.pathQueue.clear();
        this.targetIndex = 0;
        this.lastIndex = -1;
        this.timer.reset()
        StrafeUtil.enabled = false;

        KeyBindUtil.releaseAllExcept()
        RotationHandler.getInstance().stop()
        Logger.sendMessage("Stopped PathExecutor")
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (player == null || world == null || !this.enabled || mc.currentScreen != null) return

        if (player.motionX == 0.0 && player.motionZ == 0.0) {
            if (this.timer.isScheduled) {
                if (this.timer.passed()) {
                    this.failed = true
                    Logger.sendMessage("Stopped Moving for too long. Disabling")
                    this.stop()
                    return
                }
            } else {
                this.timer.schedule(1000)
            }
        } else {
            this.timer.reset()
        }

        // i hate this part
        // this there can be multiple nodes in the path with the same x and z values (very rare)
        // so i save all of the y values of them and take the index with the closest y thats lower than the player (duh)
        val currentIndex =
            hashedPath[Pair(
                MathHelper.floor_double(player.posX),
                MathHelper.floor_double(player.posZ)
            )]
                ?.filter { it.first < player.posY }
                ?.maxByOrNull { it.first }
                ?.second
        if (currentIndex != null && currentIndex != this.lastIndex) {
            Logger.sendLog("Standing On Node $currentIndex")
            this.lastIndex = currentIndex
            this.targetIndex = currentIndex + 1
            RotationHandler.getInstance().stop()
            Logger.sendLog("Position Updated. LastPos: ${this.lastIndex}, CurrentPos: ${this.targetIndex}, PathSize: ${path.size}")
            if (this.targetIndex == path.size) {
                Logger.sendMessage("Path Traversed. Disabling")
                this.succeeded = true
                this.failed = false
                if (!this.start()) this.stop()
                return
            }
        }

        var target = path[this.targetIndex]
        val yaw = AngleUtil.get360RotationYaw(
            AngleUtil.getRotation(
                player.positionVector.addVector(
                    player.motionX,
                    0.0,
                    player.motionZ
                ), Vec3(target).addVector(0.5, 0.0, 0.5), false
            ).yaw
        )
        val yawDiff = Math.abs(AngleUtil.get360RotationYaw() - yaw);

        if (yawDiff > 10 && !RotationHandler.getInstance().isEnabled()) {
            Logger.sendLog("Started Rotation. YawDiff: ${yawDiff}")
            // kotlin is gay as shit
            val config = RotationConfiguration(Angle(yaw, 20f), 300, null)
            config.easeFunction(RotationConfiguration.Ease.EASE_OUT_QUAD)
            RotationHandler.getInstance().easeTo(config)
        }

        StrafeUtil.enabled = true;
        StrafeUtil.yaw = yaw;

        val shouldJump = player.onGround
                && Math.hypot(player.posX - (target.x + 0.5), player.posZ - (target.z + 0.5)) <= 1.0
                && target.y >= player.posY
                && Math.abs(target.y - player.posY) < 0.1
        KeyBindUtil.setKeyBindState(gameSettings.keyBindForward, true)
        KeyBindUtil.setKeyBindState(
            gameSettings.keyBindSprint,
            yawDiff < 40 && !shouldJump && player.onGround
        )
        if (shouldJump) {
            Logger.sendMessage("Jumping")
            player.jump()
        }
    }

    fun failed(): Boolean {
        return !enabled && failed && !succeeded
    }

    fun succeeded(): Boolean {
        return !enabled && !failed && succeeded
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!this.enabled) return;
        this.path.forEach { RenderUtil.drawBlockBox(it, Color(0, 255, 255, 50)) }
        RenderUtil.drawBlockBox(this.path[this.targetIndex], Color(255, 0, 0, 100))
    }
}