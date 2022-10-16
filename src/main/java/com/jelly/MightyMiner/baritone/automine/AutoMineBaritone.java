package com.jelly.MightyMiner.baritone.automine;

import com.jelly.MightyMiner.baritone.automine.config.AutoMineType;
import com.jelly.MightyMiner.baritone.automine.config.MineBehaviour;
import com.jelly.MightyMiner.baritone.automine.pathing.AStarPathFinder;
import com.jelly.MightyMiner.baritone.automine.pathing.config.PathBehaviour;
import com.jelly.MightyMiner.baritone.autowalk.movement.Moves;
import com.jelly.MightyMiner.baritone.logging.Logger;
import com.jelly.MightyMiner.baritone.structures.BlockNode;
import com.jelly.MightyMiner.baritone.structures.BlockType;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.LinkedList;

public class AutoMineBaritone{

    Minecraft mc = Minecraft.getMinecraft();
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
    boolean enabled;

    AStarPathFinder pathFinder;
    BlockPos playerFloorPos;

    Moves lastMove;
    boolean jumpFlag;
    int jumpCooldown;

    public AutoMineBaritone(MineBehaviour mineBehaviour){
        this.mineBehaviour = mineBehaviour;
        pathFinder = new AStarPathFinder(getPathBehaviour());
    }


    public void clearBlocksToWalk(){
        blocksToMine.clear();
        BlockRenderer.renderMap.clear();
        minedBlocks.clear();
    }

    public void enableBaritone(BlockPos blockPos){
        enabled = true;
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
            long start = System.currentTimeMillis();
            try {
                blocksToMine = pathFinder.getPath(blockPos);
            } catch (Throwable e) {
                Logger.playerLog("Error when getting path!");
                e.printStackTrace();
            }
            Logger.playerLog("finished pathfinding in " + (System.currentTimeMillis() - start));

            if (!blocksToMine.isEmpty()) {
                for (BlockNode blockNode : blocksToMine) {
                    BlockRenderer.renderMap.put(blockNode.getBlockPos(), Color.ORANGE);
                }
                BlockRenderer.renderMap.put(blocksToMine.getFirst().getBlockPos(), Color.RED);
            } else {
                Logger.playerLog("blocks to mine EMPTY!");
            }
            Logger.log("Starting to mine");
            inAction = true;
            currentState = PlayerState.NONE;
            stuckTickCount = 0;
        }).start();
    }


    public void enableBaritone(Block... blockType) {
        enabled = true;
        targetBlockType = blockType;

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
            } catch (Throwable e){
                Logger.playerLog("Error when getting path!");
                e.printStackTrace();
            }
            if (!blocksToMine.isEmpty()) {
                for (BlockNode blockNode : blocksToMine) {
                    BlockRenderer.renderMap.put(blockNode.getBlockPos(), Color.ORANGE);
                }
                BlockRenderer.renderMap.put(blocksToMine.getFirst().getBlockPos(), Color.RED);
            } else {
                Logger.playerLog("blocks to mine EMPTY!");
            }
            Logger.log("Starting to mine");
            inAction = true;
            currentState = PlayerState.NONE;
            stuckTickCount = 0;
        }).start();
    }

    public void enableBaritoneSingleThread(Block... blockType) throws Exception{ // ONLY USABLE IN SHORT DISTANCE!!!!
        enabled = true;
        targetBlockType = blockType;

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

        if (!blocksToMine.isEmpty()) {
            for (BlockNode blockNode : blocksToMine) {
                BlockRenderer.renderMap.put(blockNode.getBlockPos(), Color.ORANGE);
            }
            BlockRenderer.renderMap.put(blocksToMine.getFirst().getBlockPos(), Color.RED);
        } else {
            Logger.playerLog("blocks to mine EMPTY!");
        }
        Logger.log("Starting to mine");
        inAction = true;
        currentState = PlayerState.NONE;
        stuckTickCount = 0;
    }


    public void disableBaritone() {
        pauseBaritone();
        enabled = false;
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
    public void onTickEvent(TickEvent.Phase phase){

        if(phase != TickEvent.Phase.START || !inAction || blocksToMine.isEmpty())
            return;


        if (shouldRemoveFromList(blocksToMine.getLast())) {
            stuckTickCount = 0;
            minedBlocks.add(blocksToMine.getLast());
            BlockRenderer.renderMap.remove(blocksToMine.getLast().getBlockPos());
            blocksToMine.removeLast();
        } else {
            //stuck handling
            stuckTickCount++;
            if(stuckTickCount > 20 * mineBehaviour.getRestartTimeThreshold()){
                new Thread(restartBaritone).start();
                return;
            }
        }

        if(blocksToMine.isEmpty() || (BlockUtils.isPassable(blocksToMine.getFirst().getBlockPos()) && blocksToMine.getFirst().getBlockType() != BlockType.WALK)){
            disableBaritone();
            return;
        }

        updateState();

        BlockPos targetMineBlock = blocksToMine.getLast().getBlockPos();

        switch (currentState){
            case WALKING:
                float reqYaw = AngleUtils.getRequiredYaw(blocksToMine.getLast().getBlockPos());
                if(inAction && !blocksToMine.isEmpty())
                    rotation.intLockAngle(reqYaw, 0, 5); // camera angle


                if((targetMineBlock.getY() > mc.thePlayer.posY || (!minedBlocks.isEmpty() && minedBlocks.getLast().getBlockPos().getY() > mc.thePlayer.posY))
                        && !jumpFlag && mc.thePlayer.posY - mc.thePlayer.lastTickPosY == 0 && jumpCooldown == 0 && mc.thePlayer.onGround) {
                    jumpFlag = true;
                    jumpCooldown = 10;
                }
                KeybindHandler.updateKeys(
                        AngleUtils.getAngleDifference(reqYaw, AngleUtils.getActualRotationYaw()) < -4 * 10 + 45,
                        AngleUtils.getAngleDifference(reqYaw, AngleUtils.getActualRotationYaw()) >= 45,
                        false,
                        false,
                        false,
                        false,
                        false,
                        jumpFlag);

                jumpFlag = false;
                if(jumpCooldown > 0) jumpCooldown --;
                break;


            case MINING:
                mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbar("Pick", "Drill", "Gauntlet");
                KeybindHandler.updateKeys(
                        false,
                        false,
                        false,
                        false,
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

    public void onRenderEvent(){
        if(rotation.rotating)
            rotation.update();

    }

    private void updateState(){

        if(mineBehaviour.getMineType() == AutoMineType.STATIC) {
            currentState = PlayerState.MINING;
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

        if(currentState == PlayerState.WALKING){
            if(minedBlocks.getLast().getBlockType() == BlockType.WALK){
                if(blocksToMine.getLast().getBlockType() == BlockType.MINE)
                    currentState = PlayerState.MINING;

            } else if(minedBlocks.getLast().getBlockType() == BlockType.MINE) {
                if (BlockUtils.onTheSameXZ(minedBlocks.getLast().getBlockPos(), BlockUtils.getPlayerLoc()))
                    currentState = PlayerState.MINING;
            }

        } else if(currentState == PlayerState.MINING){
            if (blocksToMine.getLast().getBlockType() == BlockType.MINE) {
                if( (BlockUtils.fitsPlayer(minedBlocks.getLast().getBlockPos().down()) || BlockUtils.fitsPlayer(minedBlocks.getLast().getBlockPos().down(2)))
                        && !BlockUtils.onTheSameXZ(minedBlocks.getLast().getBlockPos(), BlockUtils.getPlayerLoc())) {
                    currentState = PlayerState.WALKING;
                }
            }

        } else if(currentState == PlayerState.NONE){
            currentState = blocksToMine.getLast().getBlockType().equals(BlockType.MINE) ? PlayerState.MINING : PlayerState.WALKING;
        }
    }

    private final Runnable restartBaritone = () -> {
        try {
            pauseBaritone();
            Logger.playerLog("Restarting baritone");
            Thread.sleep(200);
            KeybindHandler.setKeyBindState(KeybindHandler.keybindS, true);
            Thread.sleep(100);
            enableBaritone(targetBlockType);
        } catch (InterruptedException ignored) {}
    };

    private boolean shouldRemoveFromList(BlockNode lastBlockNode){
        if(lastBlockNode.getBlockType() == BlockType.MINE)
            return BlockUtils.isPassable(lastBlockNode.getBlockPos()) || BlockUtils.getBlock(lastBlockNode.getBlockPos()).equals(Blocks.bedrock);
        else
            return BlockUtils.onTheSameXZ(lastBlockNode.getBlockPos(), BlockUtils.getPlayerLoc()) || !BlockUtils.fitsPlayer(lastBlockNode.getBlockPos().down());
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
