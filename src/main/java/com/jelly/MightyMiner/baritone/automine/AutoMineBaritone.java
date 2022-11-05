package com.jelly.MightyMiner.baritone.automine;

import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathMode;
import com.jelly.MightyMiner.baritone.automine.config.AutoMineType;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.baritone.automine.config.PathFindSetting;
import com.jelly.MightyMiner.baritone.automine.logging.Logger;
import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathFinderBehaviour;
import com.jelly.MightyMiner.baritone.automine.calculations.AStarPathFinder;
import com.jelly.MightyMiner.baritone.automine.calculations.exceptions.NoBlockException;
import com.jelly.MightyMiner.baritone.automine.calculations.exceptions.NoPathException;
import com.jelly.MightyMiner.baritone.automine.movement.PathExecutor;
import com.jelly.MightyMiner.baritone.automine.structures.Path;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.utils.*;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoMineBaritone{


    Minecraft mc = Minecraft.getMinecraft();
    public enum BaritoneState{
        PATH_FINDING,
        EXECUTING,
        IDLE,
        FAILED
    }
    volatile BaritoneState state = BaritoneState.IDLE;

    PathFindSetting pathSetting;
    BaritoneConfig config;

    AStarPathFinder pathFinder;
    PathExecutor executor;
    BlockPos playerFloorPos;

    Block[] targetBlockType;
    BlockPos targetBlockPos;

    Path path;



    public AutoMineBaritone(BaritoneConfig config){
        this.config = config;
        executor = new PathExecutor();
        pathFinder = new AStarPathFinder(getPathBehaviour());
    }


    public void mineFor(Block... blockType) {
        Logger.playerLog("Starting to mine");
        registerEvent();
        pathSetting = new PathFindSetting(config.isMineWithPreference(), PathMode.MINE, false);
        path = null;

        targetBlockType = blockType;
        startPathFinding();
    }

    public void goTo(BlockPos blockPos){
        registerEvent();
        pathSetting = new PathFindSetting(config.isMineWithPreference(), PathMode.GOTO, true);
        path = null;

        targetBlockPos = blockPos;
        startPathFinding();
    }




    public BaritoneState getState(){
        return state;
    }


    private void registerEvent() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    private void unregister() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    public void disableBaritone() {
        Logger.playerLog("Disabled baritone");
        state = BaritoneState.IDLE;
        executor.reset();
        terminate();
    }

    // failed = true -> Will actually terminate whole thing, otherwise just restart and pretend nothing has happened...
    private void failBaritone(boolean failed) {

        executor.reset();
        if(path != null)
            pathFinder.addToBlackList(path.getBlocksInPath().getFirst().getBlockPos());

        if(failed) {
            state = BaritoneState.FAILED;
            terminate();
        }
        else
            startPathFinding();

    }

    private void terminate() {
        unregister();
        KeybindHandler.resetKeybindState();
    }

    // logic is bit intricate here, sorry
    @SubscribeEvent
    public void TickEvent(TickEvent.ClientTickEvent event){
        if(mc.thePlayer == null || mc.theWorld == null || state == BaritoneState.IDLE || state == BaritoneState.FAILED)
            return;


        switch(state){
            case PATH_FINDING:
                KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, config.isShiftWhenMine());
                break;
            case EXECUTING:
                if (executor.hasSuccessfullyFinished()) {
                    disableBaritone();
                    return;
                }

                if (executor.hasFailed()) {
                    failBaritone(false);
                    return;
                }

                if (!executor.isExecuting()) {
                    executor.executePath(path, config);
                }
        }
    }


    private void startPathFinding(){
        state = BaritoneState.PATH_FINDING;

        KeybindHandler.resetKeybindState();
        KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, config.isShiftWhenMine());

        new Thread(() -> {

            if(!config.isMineFloor()) {
                if(playerFloorPos != null)
                    pathFinder.removeFromBlackList(playerFloorPos);

                playerFloorPos = BlockUtils.getPlayerLoc().down();
                pathFinder.addToBlackList(playerFloorPos);
            }

            try {
                switch (pathSetting.getPathMode()) {
                    case MINE:
                        path =  (pathSetting.isFindWithBlockPos() ? pathFinder.getPath(PathMode.MINE, targetBlockPos) : pathFinder.getPath(PathMode.MINE, pathSetting.isMineWithPreference(), targetBlockType));
                        break;
                    case GOTO: // can add more options later
                        path = pathFinder.getPath(PathMode.GOTO, targetBlockPos);
                        break;
                }
                state = BaritoneState.EXECUTING;

            }catch (NoBlockException | NoPathException e){
                Logger.playerLog("Pathfind failed: " + e);
                failBaritone(true);
            }
        }).start();
    }


    private PathFinderBehaviour getPathBehaviour(){
        return new PathFinderBehaviour(
                config.getForbiddenMiningBlocks() == null ? null : config.getForbiddenMiningBlocks(),
                config.getAllowedMiningBlocks() == null ? null : config.getAllowedMiningBlocks(),
                config.getMaxY(),
                config.getMinY(),
                config.getMineType() == AutoMineType.DYNAMIC ? 30 : 4,
                config.getMineType() == AutoMineType.STATIC
        );
    }


}
