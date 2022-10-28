package com.jelly.MightyMiner.baritone.automine;

import com.jelly.MightyMiner.baritone.automine.config.AutoMineType;
import com.jelly.MightyMiner.baritone.automine.config.MineBehaviour;
import com.jelly.MightyMiner.baritone.automine.logging.Logger;
import com.jelly.MightyMiner.baritone.automine.pathing.config.PathBehaviour;
import com.jelly.MightyMiner.baritone.automine.pathing.AStarPathFinder;
import com.jelly.MightyMiner.baritone.automine.pathing.config.PathMode;
import com.jelly.MightyMiner.baritone.automine.pathing.exceptions.NoBlockException;
import com.jelly.MightyMiner.baritone.automine.pathing.exceptions.NoPathException;
import com.jelly.MightyMiner.baritone.automine.structures.BlockNode;
import com.jelly.MightyMiner.baritone.automine.structures.BlockType;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.utils.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.LinkedList;

public class AutoMineBaritone{


    Minecraft mc = Minecraft.getMinecraft();
    BlockRenderer blockRenderer = new BlockRenderer();

    MineBehaviour mineBehaviour;



    LinkedList<BlockNode> blocksToMine = new LinkedList<>();
    LinkedList<BlockNode> minedBlocks = new LinkedList<>();

    boolean inAction = false;
    Rotation rotation = new Rotation();

    int deltaJumpTick = 0;

    enum PlayerState {
        WALKING,
        MINING,
        NONE
    }
    PlayerState currentState;
    Block[] targetBlockType;
    volatile boolean enabled;

    AStarPathFinder pathFinder;
    BlockPos playerFloorPos;

    boolean jumpFlag;
    int jumpCooldown;

    boolean shouldGoToFinalBlock;

    public AutoMineBaritone(MineBehaviour mineBehaviour){

        this.mineBehaviour = mineBehaviour;

        pathFinder = new AStarPathFinder(getPathBehaviour());
    }


    public void clearBlocksToWalk(){
        blocksToMine.clear();
        blockRenderer.renderMap.clear();
        minedBlocks.clear();
    }



    public void mineFor(BlockPos blockPos){
        enable();
        clearBlocksToWalk();
        KeybindHandler.resetKeybindState();
        shouldGoToFinalBlock = false;

        if(mineBehaviour.isShiftWhenMine())
            KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);


        if(!mineBehaviour.isMineFloor()) {
            if(playerFloorPos != null)
                pathFinder.removeFromBlackList(playerFloorPos);

            playerFloorPos = BlockUtils.getPlayerLoc().down();
            pathFinder.addToBlackList(playerFloorPos);
        }

        new Thread(() -> {
            try{
                blocksToMine = pathFinder.getPath(blockPos);
            } catch (NoPathException e){
                Logger.playerLog("Error when getting path!: " + e);
                unregister();
                return;
            }

            for (BlockNode blockNode : blocksToMine) {
                blockRenderer.renderMap.put(blockNode.getBlockPos(), Color.ORANGE);
            }
            blockRenderer.renderMap.put(blocksToMine.getFirst().getBlockPos(), Color.RED);
            Logger.log("Starting to mine");

            inAction = true;
            currentState = PlayerState.NONE;
            stuckTickCount = 0;
        }).start();
    }


    public void mineFor(Block... blockType) {
        targetBlockType = blockType;
        shouldGoToFinalBlock = false;
        enable();
        clearBlocksToWalk();

        KeybindHandler.resetKeybindState();

        if(mineBehaviour.isShiftWhenMine())
            KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);

        if(!mineBehaviour.isMineFloor()) {
            if(playerFloorPos != null)
                pathFinder.removeFromBlackList(playerFloorPos);

            playerFloorPos = BlockUtils.getPlayerLoc().down();
            pathFinder.addToBlackList(playerFloorPos);
        }

        new Thread(() -> {
            try{
                if(mineBehaviour.isMineWithPreference())
                    blocksToMine = pathFinder.getPathWithPreference(blockType);
                else
                    blocksToMine = pathFinder.getPath(blockType);
            } catch (NoPathException | NoBlockException e){
                Logger.playerLog("Error when getting path!: " + e);
                unregister();
                return;
            }
            for (BlockNode blockNode : blocksToMine) {
                blockRenderer.renderMap.put(blockNode.getBlockPos(), Color.ORANGE);
            }
            blockRenderer.renderMap.put(blocksToMine.getFirst().getBlockPos(), Color.RED);

            Logger.log("Starting to mine");
            inAction = true;
            currentState = PlayerState.NONE;
            stuckTickCount = 0;
        }).start();
    }


    public void mineForInSingleThread(Block... blockType) throws Exception{ // ONLY USABLE IN SHORT DISTANCE!!!!
        enable();
        targetBlockType = blockType;
        shouldGoToFinalBlock = false;

        clearBlocksToWalk();

        KeybindHandler.resetKeybindState();

        if(mineBehaviour.isShiftWhenMine())
            KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);

        if(!mineBehaviour.isMineFloor()) {
            if(playerFloorPos != null)
                pathFinder.removeFromBlackList(playerFloorPos);

            playerFloorPos = BlockUtils.getPlayerLoc().down();
            pathFinder.addToBlackList(playerFloorPos);
        }

        if(mineBehaviour.isMineWithPreference())
            blocksToMine = pathFinder.getPathWithPreference(blockType);
        else
            blocksToMine = pathFinder.getPath(blockType);

        for (BlockNode blockNode : blocksToMine) {
            blockRenderer.renderMap.put(blockNode.getBlockPos(), Color.ORANGE);
        }
        blockRenderer.renderMap.put(blocksToMine.getFirst().getBlockPos(), Color.RED);

        Logger.log("Starting to mine");
        inAction = true;
        currentState = PlayerState.NONE;
        stuckTickCount = 0;
    }


    public void goTo(BlockPos blockPos){
        shouldGoToFinalBlock = true;
        enable();
        clearBlocksToWalk();
        KeybindHandler.resetKeybindState();

        if(mineBehaviour.isShiftWhenMine())
            KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);


        if(!mineBehaviour.isMineFloor()) {
            if(playerFloorPos != null)
                pathFinder.removeFromBlackList(playerFloorPos);

            playerFloorPos = BlockUtils.getPlayerLoc().down();
            pathFinder.addToBlackList(playerFloorPos);
        }

        new Thread(() -> {

            try{
                blocksToMine = pathFinder.getPath(blockPos, PathMode.GOTO);
            } catch (NoPathException e){
                Logger.playerLog("Error when getting path: " + e);
                unregister();
                return;
            }

            for (BlockNode blockNode : blocksToMine) {
                blockRenderer.renderMap.put(blockNode.getBlockPos(), Color.ORANGE);
            }
            blockRenderer.renderMap.put(blocksToMine.getFirst().getBlockPos(), Color.RED);

            Logger.log("Starting to mine");
            inAction = true;
            currentState = PlayerState.NONE;
            stuckTickCount = 0;
        }).start();
    }



    private void enable() {
        MinecraftForge.EVENT_BUS.register(this);
        enabled = true;
    }


    public void disableBaritone() {
        Logger.log("Baritone completed");
        enabled = false;
        pauseBaritone();
        unregister();
    }

    private void unregister(){
        MinecraftForge.EVENT_BUS.unregister(this);
    }
    private void pauseBaritone() {
        inAction = false;
        currentState = PlayerState.NONE;
        KeybindHandler.resetKeybindState();

        if(mineBehaviour.isShiftWhenMine())
            KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, true);

        if(!blocksToMine.isEmpty() && blocksToMine.getLast().getBlockType() == BlockType.MINE)
            pathFinder.addToBlackList(blocksToMine.getLast().getBlockPos());

        clearBlocksToWalk();
    }
    public boolean isEnabled(){
        return enabled;
    }


    @SubscribeEvent
    public void onOverlayRenderEvent(RenderGameOverlayEvent event){

        if(event.type == RenderGameOverlayEvent.ElementType.TEXT){
            if(blocksToMine != null){
                if(!blocksToMine.isEmpty()){
                    for(int i = 0; i < blocksToMine.size(); i++){
                        mc.fontRendererObj.drawString(blocksToMine.get(i).getBlockPos().toString() + " " + blocksToMine.get(i).getBlockType().toString() , 5, 5 + 10 * i, -1);
                    }
                }
            }
            if(currentState != null)
                mc.fontRendererObj.drawString(currentState.toString(), 300, 5, -1);
        }
    }



    int stuckTickCount = 0;

    @SubscribeEvent
    public void onTickEvent(TickEvent.ClientTickEvent event){


        if(event.phase != TickEvent.Phase.START || !inAction || blocksToMine.isEmpty())
            return;


        if (shouldRemoveFromList(blocksToMine.getLast())) {
            stuckTickCount = 0;
            minedBlocks.add(blocksToMine.getLast());
            blockRenderer.renderMap.remove(blocksToMine.getLast().getBlockPos());
            blocksToMine.removeLast();
        } else {
            //stuck handling
            stuckTickCount++;
            if(stuckTickCount > 20 * mineBehaviour.getRestartTimeThreshold()){
                new Thread(restartBaritone).start();
                return;
            }
        }

        if(blocksToMine.isEmpty() || (BlockUtils.isPassable(blocksToMine.getFirst().getBlockPos()) && blocksToMine.getFirst().getBlockType() == BlockType.MINE)){
            if(!shouldGoToFinalBlock || BlockUtils.getPlayerLoc().equals(minedBlocks.getLast().getBlockPos())) {
                disableBaritone();
                return;
            }
        }

        updateState();



        switch (currentState){
            case WALKING:
                BlockPos targetWalkBlock = (blocksToMine.isEmpty() || blocksToMine.getLast().getBlockType() == BlockType.MINE) ? minedBlocks.getLast().getBlockPos() : blocksToMine.getLast().getBlockPos();
                float reqYaw = AngleUtils.getRequiredYaw(targetWalkBlock);

                if(inAction)
                    rotation.intLockAngle(reqYaw, mc.thePlayer.rotationPitch, 5); // camera angle

                if(!jumpFlag && mc.thePlayer.posY - mc.thePlayer.lastTickPosY == 0 && jumpCooldown == 0 && mc.thePlayer.onGround){

                    if(targetWalkBlock.getY() > mc.thePlayer.posY){
                        jumpFlag = true;
                        jumpCooldown = 10;
                    }
                }

                KeybindHandler.updateKeys(
                        AngleUtils.getAngleDifference(reqYaw, AngleUtils.getActualRotationYaw()) < -4 * 10 + 45,
                        AngleUtils.getAngleDifference(reqYaw, AngleUtils.getActualRotationYaw()) >= 45,
                        false, false, false, false, false,
                        jumpFlag);

                jumpFlag = false;
                if(jumpCooldown > 0) jumpCooldown --;
                break;


            case MINING:
                BlockPos targetMineBlock = blocksToMine.getLast().getBlockPos();
                mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbar("Pick", "Drill", "Gauntlet");
                KeybindHandler.updateKeys(
                        false, false, false, false,
                        mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null &&
                                mc.objectMouseOver.getBlockPos().equals(targetMineBlock),
                        false,
                        mineBehaviour.isShiftWhenMine(),
                        false);


                if(mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null){
                    // special cases for optimization
                    if(BlockUtils.isAdjacentXZ(targetMineBlock, BlockUtils.getPlayerLoc()) && !AngleUtils.shouldLookAtCenter(targetMineBlock) &&
                            (( targetMineBlock.getY() - mc.thePlayer.posY == 0 && BlockUtils.getBlock(targetMineBlock.up()).equals(Blocks.air) )|| targetMineBlock.getY() - mc.thePlayer.posY == 1)){
                        rotation.intLockAngle(AngleUtils.getRequiredYaw(targetMineBlock), 28, mineBehaviour.getRotationTime());
                    } else if (!BlockUtils.isPassable(targetMineBlock) && !rotation.rotating)
                        rotation.intLockAngle(AngleUtils.getRequiredYaw(targetMineBlock), AngleUtils.getRequiredPitch(targetMineBlock), mineBehaviour.getRotationTime());

                }
                break;
        }


        if (deltaJumpTick > 0)
            deltaJumpTick--;
    }

    @SubscribeEvent
    public void onRenderEvent(RenderWorldLastEvent event){
        blockRenderer.renderAABB(event);
        if(rotation.rotating)
            rotation.update();


    }

    private void updateState(){

        if(mineBehaviour.getMineType() == AutoMineType.STATIC) {
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
            currentState =  blocksToMine.getLast().getBlockType().equals(BlockType.MINE) ? PlayerState.MINING : PlayerState.WALKING;
            return;
        }

        if(blocksToMine.getLast().getBlockType() == BlockType.WALK) {
            currentState = PlayerState.WALKING;
            return;
        }

        switch (currentState){
            case NONE:
                currentState = blocksToMine.getLast().getBlockType().equals(BlockType.MINE) ? PlayerState.MINING : PlayerState.WALKING;
                break;
            case WALKING:
                if((minedBlocks.getLast().getBlockType() == BlockType.WALK && blocksToMine.getLast().getBlockType() == BlockType.MINE) ||
                        (minedBlocks.getLast().getBlockType() == BlockType.MINE && BlockUtils.onTheSameXZ(minedBlocks.getLast().getBlockPos(), BlockUtils.getPlayerLoc())))
                    currentState = PlayerState.MINING;
                break;
            case MINING:
                if (blocksToMine.getLast().getBlockType() == BlockType.MINE && shouldWalkTo(minedBlocks.getLast().getBlockPos()))
                    currentState = PlayerState.WALKING;
                break;
        }
    }

    private final Runnable restartBaritone = () -> {
        try {
            pauseBaritone();
            Logger.playerLog("Restarting baritone");
            Thread.sleep(200);
            KeybindHandler.setKeyBindState(KeybindHandler.keybindS, true);
            Thread.sleep(100);
            mineFor(targetBlockType);
        } catch (InterruptedException ignored) {}
    };

    private boolean shouldRemoveFromList(BlockNode lastBlockNode){
        if(lastBlockNode.getBlockType() == BlockType.MINE)
            return BlockUtils.isPassable(lastBlockNode.getBlockPos()) || BlockUtils.getBlock(lastBlockNode.getBlockPos()).equals(Blocks.bedrock);
        else
            return BlockUtils.onTheSameXZ(lastBlockNode.getBlockPos(), BlockUtils.getPlayerLoc()) || !BlockUtils.fitsPlayer(lastBlockNode.getBlockPos().down());
    }

    private boolean shouldWalkTo(BlockPos blockPos){

        return  blockPos.getY() <= (mc.thePlayer.posY) + 1 &&
                ((blockPos.getY() > Math.round(mc.thePlayer.posY) && BlockUtils.isPassable(BlockUtils.getPlayerLoc().up(2))) ||
                (blockPos.getY() < Math.round(mc.thePlayer.posY) && BlockUtils.isPassable(blockPos.up(2))) ||
                (blockPos.getY() == Math.round(mc.thePlayer.posY))) && BlockUtils.fitsPlayer(blockPos.down()) && !BlockUtils.onTheSameXZ(blockPos, BlockUtils.getPlayerLoc());
    }

    private PathBehaviour getPathBehaviour(){
        return new PathBehaviour(
                mineBehaviour.getForbiddenMiningBlocks() == null ? null : mineBehaviour.getForbiddenMiningBlocks(),
                mineBehaviour.getAllowedMiningBlocks() == null ? null : mineBehaviour.getAllowedMiningBlocks(),
                mineBehaviour.getMaxY(),
                mineBehaviour.getMinY(),
                mineBehaviour.getMineType() == AutoMineType.DYNAMIC ? 30 : 4,
                mineBehaviour.getMineType() == AutoMineType.STATIC
        );
    }


}
