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

        Logger.playerLog("Starting to Mining");
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

    public boolean isEnabled(){
        return state == BaritoneState.PATH_FINDING || state == BaritoneState.EXECUTING;
    }

    private void registerEvent() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    private void unregister() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    public void disableBaritone() {
        Logger.playerLog("Baritone completed");
        executor.disable();
        state = BaritoneState.IDLE;
        KeybindHandler.resetKeybindState();
        unregister();
    }




    private void failBaritone() {
        state = BaritoneState.FAILED;
        KeybindHandler.resetKeybindState();

        if(path != null)
            pathFinder.addToBlackList(path.getBlocksInPath().getFirst().getBlockPos());

    }


    @SubscribeEvent
    public void TickEvent(TickEvent.ClientTickEvent event){
        if(mc.thePlayer == null || mc.theWorld == null || state == BaritoneState.IDLE || state == BaritoneState.FAILED)
            return;


        switch (state){
            case PATH_FINDING:
                break;
            case EXECUTING:
                if(executor.hasSuccessfullyFinished())
                    disableBaritone();

                if(executor.hasFailed())
                    failBaritone();

                if(!executor.isExecuting())
                    executor.executePath(path, config);
                break;
        }
    }


    private void startPathFinding(){
        state = BaritoneState.PATH_FINDING;
        KeybindHandler.resetKeybindState();

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
                state = BaritoneState.FAILED;
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
