package com.jelly.MightyMiner.baritone.automine.movement;

import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathMode;
import com.jelly.MightyMiner.baritone.automine.config.MiningType;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.baritone.automine.logging.Logger;
import com.jelly.MightyMiner.baritone.automine.structures.BlockNode;
import com.jelly.MightyMiner.baritone.automine.structures.BlockType;
import com.jelly.MightyMiner.baritone.automine.structures.Path;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.LinkedList;

public class PathExecutor {
    Minecraft mc = Minecraft.getMinecraft();


    int stuckTickCount = 0;

    boolean jumpFlag;
    int jumpCooldown;

    //ContinuousRotator rotatorContext = new ContinuousRotator();
    Rotation rotator = new Rotation();

    int deltaJumpTick = 0;

    LinkedList<BlockNode> blocksToMine = new LinkedList<>();
    LinkedList<BlockNode> minedBlocks = new LinkedList<>();

    BlockRenderer blockRenderer = new BlockRenderer();

    BaritoneConfig config;


    PlayerState currentState = PlayerState.IDLE;
    boolean shouldGoToFinalBlock;

    enum PlayerState {
        IDLE,
        WALKING,
        MINING,
        FAILED,
        FINISHED
    }

    Path path;


    public void executePath(Path path, BaritoneConfig config){

        if(path == null || path.getBlocksInPath().isEmpty()){
            Logger.log("Path is empty!");
            fail();
            return;
        }

        shouldGoToFinalBlock = path.getMode() == PathMode.GOTO;
     //   pathSize = path.getBlocksInPath().size();

        this.path = path;
        this.blocksToMine = path.getBlocksInPath();
        this.config = config;

        currentState = PlayerState.IDLE;
        stuckTickCount = 0;
        deltaJumpTick = 0;
        jumpCooldown = 0;
        jumpFlag = false;

        minedBlocks.clear();
        blockRenderer.renderMap.clear();

        blockRenderer.renderMap.put(path.getBlocksInPath().getFirst().getPos(), Color.RED);
        for(int i = 1; i < path.getBlocksInPath().size(); i++){
            blockRenderer.renderMap.put(path.getBlocksInPath().get(i).getPos(), Color.ORANGE);
        }

        MinecraftForge.EVENT_BUS.register(this);
        updateState();
    }

    public boolean isExecuting(){
        return currentState == PlayerState.MINING || currentState == PlayerState.WALKING;
    }

    public boolean hasFailed(){
        return currentState == PlayerState.FAILED;
    }

    public boolean hasSuccessfullyFinished(){
        return currentState == PlayerState.FINISHED;
    }

    public void reset(){
        this.currentState = PlayerState.IDLE;
        unregister();
    }

    public void disable(){
        this.currentState = PlayerState.FINISHED;
        unregister();
    }
    private void fail(){
        currentState = PlayerState.FAILED;
        unregister();
    }
    public void unregister(){
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onTickEvent(TickEvent.ClientTickEvent event){

        if(mc.thePlayer == null
                || event.phase != TickEvent.Phase.START
                || currentState == PlayerState.FAILED
                || currentState == PlayerState.FINISHED
        ) {
            return;
        }


        if (!blocksToMine.isEmpty() && shouldRemoveFromList(blocksToMine.getLast())) {
            stuckTickCount = 0;
            minedBlocks.add(blocksToMine.getLast());
            blockRenderer.renderMap.remove(blocksToMine.getLast().getPos());
            blocksToMine.removeLast();
        } else {
            stuckTickCount++;
            if(stuckTickCount > 20 * config.getRestartTimeThreshold()){
                fail();
                return;
            }
        }

        if(blocksToMine.isEmpty() || (BlockUtils.isPassable(blocksToMine.getFirst().getPos()) && blocksToMine.getFirst().getType() == BlockType.MINE)){
            if(!shouldGoToFinalBlock || (!minedBlocks.isEmpty() && BlockUtils.getPlayerLoc().equals(minedBlocks.getLast().getPos()))) {
                disable();
                return;
            }
        }

        updateState();


        switch (currentState) {
            case WALKING:
                BlockPos targetWalkBlock = (blocksToMine.isEmpty() || blocksToMine.getLast().getType() == BlockType.MINE) ? minedBlocks.getLast().getPos() : blocksToMine.getLast().getPos();

                float reqYaw = AngleUtils.getRequiredYawCenter(targetWalkBlock);
                rotator.initAngleLock(reqYaw, mc.thePlayer.rotationPitch, 5);

                if(!jumpFlag
                        // is not falling/jumping
                        && mc.thePlayer.posY - mc.thePlayer.lastTickPosY == 0
                        // is on ground
                        && mc.thePlayer.onGround
                        && jumpCooldown == 0
                ){
                    if(targetWalkBlock.getY() > mc.thePlayer.posY){
                        jumpFlag = true;
                        jumpCooldown = 10;
                    }
                }

                KeybindHandler.updateKeys(
                        AngleUtils.getAngleDifference(reqYaw, AngleUtils.getActualRotationYaw()) < -4 * 10 + 45,
                        AngleUtils.getAngleDifference(reqYaw, AngleUtils.getActualRotationYaw()) >= 45,
                        false, false, false, false, false,
                        jumpFlag
                );

                jumpFlag = false;
                if(jumpCooldown > 0) {
                    jumpCooldown --;
                }
                break;
            case MINING:
                BlockPos targetMineBlock = blocksToMine.getLast().getPos();
                mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbar("Pick", "Drill", "Gauntlet");
                KeybindHandler.updateKeys(
                        false, false, false, false,
                        mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null &&
                                mc.objectMouseOver.getBlockPos().equals(targetMineBlock),
                        false,
                        config.isShiftWhenMine(),
                        false);

                Vec3 closerVisibilityLine = BlockUtils.getCloserVisibilityLine(targetMineBlock, 50);
                if(closerVisibilityLine == null) {
                    fail();
                    return;
                }
                rotator.initAngleLock(AngleUtils.getRotation(closerVisibilityLine).getFirst(), AngleUtils.getRotation(closerVisibilityLine).getSecond(), config.getMineRotationTime());
                break;
        }


        if (deltaJumpTick > 0) {
            deltaJumpTick--;
        }
    }


    @SubscribeEvent
    public void onRenderEvent(RenderWorldLastEvent event){
        blockRenderer.renderAABB(event);
        if(rotator.rotating)
            rotator.update();
    }


    @SubscribeEvent
    public void onOverlayRenderEvent(RenderGameOverlayEvent event){
        if(event.type == RenderGameOverlayEvent.ElementType.TEXT){
            if(blocksToMine != null){
                if(!blocksToMine.isEmpty()){
                    for(int i = 0; i < blocksToMine.size(); i++){
                        mc.fontRendererObj.drawString(blocksToMine.get(i).getPos().toString() + " " + blocksToMine.get(i).getType().toString() , 5, 5 + 10 * i, -1);
                    }
                }
            }
            if(currentState != null) {
                mc.fontRendererObj.drawString(currentState.toString(), 300, 5, -1);
            }
        }
    }

    private void updateState(){

        if(config.getMineType() == MiningType.STATIC) {
            currentState = PlayerState.MINING;
            return;
        }
        if(shouldGoToFinalBlock && blocksToMine.isEmpty()){
            currentState = PlayerState.WALKING;
            return;
        }

        if(blocksToMine.isEmpty())
            return;

        if(minedBlocks.isEmpty()){
            currentState =  blocksToMine.getLast().getType().equals(BlockType.MINE) ? PlayerState.MINING : PlayerState.WALKING;
            return;
        }

        if(blocksToMine.getLast().getType() == BlockType.WALK) {
            currentState = PlayerState.WALKING;
            return;
        }

        switch (currentState){
            case IDLE:
                currentState = blocksToMine.getLast().getType().equals(BlockType.MINE) ? PlayerState.MINING : PlayerState.WALKING;
                break;
            case WALKING:
                if((minedBlocks.getLast().getType() == BlockType.WALK && blocksToMine.getLast().getType() == BlockType.MINE) ||
                        (minedBlocks.getLast().getType() == BlockType.MINE && BlockUtils.onTheSameXZ(minedBlocks.getLast().getPos(), BlockUtils.getPlayerLoc())))
                    currentState = PlayerState.MINING;
                break;
            case MINING:
                if (blocksToMine.getLast().getType() == BlockType.MINE && shouldWalkTo(minedBlocks.getLast().getPos()))
                    currentState = PlayerState.WALKING;
                break;
        }
    }

    private boolean shouldRemoveFromList(BlockNode lastBlockNode){
        if(lastBlockNode.getType() == BlockType.MINE)
            return BlockUtils.isPassable(lastBlockNode.getPos()) || BlockUtils.getBlock(lastBlockNode.getPos()).equals(Blocks.bedrock);
        else
            return BlockUtils.onTheSameXZ(lastBlockNode.getPos(), BlockUtils.getPlayerLoc()) || !BlockUtils.fitsPlayer(lastBlockNode.getPos().down());
    }


    private boolean shouldWalkTo(BlockPos blockPos){

        return  /*lockPos.getY() <= (mc.thePlayer.posY) + 1 &&*/
                ((blockPos.getY() > Math.round(mc.thePlayer.posY) && BlockUtils.isPassable(BlockUtils.getPlayerLoc().up(2))) ||
                        (blockPos.getY() < Math.round(mc.thePlayer.posY) && BlockUtils.isPassable(blockPos.up(2))) ||
                        (blockPos.getY() == Math.round(mc.thePlayer.posY)))
                && (BlockUtils.fitsPlayer(blockPos.down()) || BlockUtils.fitsPlayer(blockPos.down(2)))
                && !BlockUtils.onTheSameXZ(blockPos, BlockUtils.getPlayerLoc());
    }


}
