package com.jelly.MightyMiner.baritone.automine;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.calculations.AStarPathFinder;
import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathFinderBehaviour;
import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathMode;
import com.jelly.MightyMiner.baritone.automine.calculations.exceptions.NoBlockException;
import com.jelly.MightyMiner.baritone.automine.calculations.exceptions.NoPathException;
import com.jelly.MightyMiner.baritone.automine.config.BaritoneConfig;
import com.jelly.MightyMiner.baritone.automine.config.MiningType;
import com.jelly.MightyMiner.baritone.automine.config.PathFindSetting;
import com.jelly.MightyMiner.baritone.automine.logging.Logger;
import com.jelly.MightyMiner.baritone.automine.movement.PathExecutor;
import com.jelly.MightyMiner.baritone.automine.structures.Path;
import com.jelly.MightyMiner.baritone.automine.structures.SemiPath;
import com.jelly.MightyMiner.events.ChunkLoadEvent;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.utils.BlockUtils.BlockData;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AutoMineBaritone {


    Minecraft mc = Minecraft.getMinecraft();

    public enum BaritoneState {
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

    ArrayList<BlockData<EnumDyeColor>> targetBlockType;
    BlockPos targetBlockPos;

    volatile Path path;
    //int chunkLoadCount;




    public AutoMineBaritone(BaritoneConfig config) {
        this.config = config;
        executor = new PathExecutor();
        pathFinder = new AStarPathFinder(getPathBehaviour());
    }


    public final void mineFor(Block... block) {
        mineFor(BlockUtils.addData(Arrays.stream(block).collect(Collectors.toCollection(ArrayList::new))));
    }

    @SafeVarargs
    public final void mineFor(BlockData<EnumDyeColor>... blockType) {
        mineFor(Arrays.stream(blockType).collect(Collectors.toCollection(ArrayList::new)));
    }

    public void mineFor(ArrayList<BlockData<EnumDyeColor>> blockType) {
        Logger.playerLog("Starting to mine");
        registerEventListener();
        pathSetting = new PathFindSetting(config.isMineWithPreference(), PathMode.MINE, false);
        path = null;

        targetBlockType = blockType;
        startPathFinding();
    }

    public void goTo(BlockPos blockPos) {
        registerEventListener();
        pathSetting = new PathFindSetting(config.isMineWithPreference(), PathMode.GOTO, true);
        path = null;

        targetBlockPos = blockPos;
        startPathFinding();
    }

    public BaritoneState getState(){
        return this.state;
    }


    private void registerEventListener() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void unregisterEventListeners() {
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
        if (path != null && path.getBlocksInPath() != null && !path.getBlocksInPath().isEmpty()) {
            pathFinder.addToBlackList(path.getBlocksInPath().getFirst().getPos());
        }

        if (failed) {
            state = BaritoneState.FAILED;
            terminate();
        } else {
            Logger.log("Restarting pathfind");
            startPathFinding();
        }

    }

    private void terminate() {
        unregisterEventListeners();
        KeybindHandler.resetKeybindState();
    }

    // logic is bit intricate here, sorry
    @SubscribeEvent
    public void TickEvent(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || state == BaritoneState.IDLE || state == BaritoneState.FAILED) {
            return;
        }

        switch (state) {
            case PATH_FINDING:
                KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, config.isShiftWhenMine());
                break;
            case EXECUTING:
                if (executor.hasSuccessfullyFinished()) {
                    Logger.log("Executor has finished");
                    // TODO: Fix this
                    if (path instanceof SemiPath) {
                        startSemiPathFinding();
                    } else {
                        disableBaritone();
                    }
                } else if (executor.hasFailed()) {
                    Logger.log("Executor has failed");
                    failBaritone(false);
                } else if (!executor.isExecuting()) {
                    Logger.log("Executor is starting to execute a path");
                    executor.executePath(path, config);
                }
//                 else if(chunkLoadCount > 6 && path instanceof SemiPath)
//                   startSemiPathFinding();

        }
    }

    @SubscribeEvent
    public void ChunkLoadEvent(ChunkLoadEvent event) {
    }

    private void startSemiPathFinding() {
        //chunkLoadCount = 0;
        startPathFinding();
        executor.reset();
    }


    private void startPathFinding() {
        state = BaritoneState.PATH_FINDING;
        Logger.playerLog("Started pathfinding");

        KeybindHandler.resetKeybindState();
        KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, config.isShiftWhenMine());

        new Thread(() -> {
            if (!config.isMineFloor()) {
                if (playerFloorPos != null) {
                    pathFinder.removeFromBlackList(playerFloorPos);
                }

                playerFloorPos = BlockUtils.getPlayerLoc().down();
                pathFinder.addToBlackList(playerFloorPos);
            }

            try {
                switch (pathSetting.getPathMode()) {
                    case MINE:
                        if (pathSetting.isFindWithBlockPos()) {
                            path = pathFinder.getPath(PathMode.MINE, targetBlockPos);
                        } else {
                            path = pathFinder.getPath(PathMode.MINE, pathSetting.isMineWithPreference(), targetBlockType);
                        }
                        break;
                    case GOTO: // can add more options later
                        path = pathFinder.getPath(PathMode.GOTO, targetBlockPos);
                        break;
                }
                state = BaritoneState.EXECUTING;

            } catch (NoBlockException | NoPathException e) {
                Logger.playerLog("Pathfind failed: " + e);
                failBaritone(true);
            }
        }).start();
    }


    private PathFinderBehaviour getPathBehaviour() {
        return new PathFinderBehaviour(
                config.getForbiddenPathfindingBlocks() == null ? null : config.getForbiddenPathfindingBlocks(),
                config.getAllowedPathfindingBlocks() == null ? null : config.getAllowedPathfindingBlocks(),
                config.getMaxY(),
                config.getMinY(),
                config.getMineType() == MiningType.DYNAMIC ? 30 : 4,
                config.getMineType() == MiningType.STATIC
        );
    }


}
