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
import com.jelly.MightyMiner.baritone.automine.movementgrapth.Executor;
import com.jelly.MightyMiner.baritone.automine.structures.Path;
import com.jelly.MightyMiner.baritone.automine.structures.SemiPath;
import com.jelly.MightyMiner.events.ChunkLoadEvent;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.utils.BlockUtils;
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

public class AutoMineBaritone {
    org.apache.logging.log4j.Logger logger = LogManager.getLogger("AutoMineBaritone");

    Minecraft mc = Minecraft.getMinecraft();

    public enum BaritoneState {
        PATH_FINDING,
        EXECUTING,
        IDLE,
        FAILED
    }

    @Getter
    volatile BaritoneState state = BaritoneState.IDLE;

    PathFindSetting pathSetting;
    BaritoneConfig config;

    AStarPathFinder pathFinder;
//    PathExecutor executor;
    Executor newExecutor;

    BlockPos playerFloorPos;

    ArrayList<BlockData<EnumDyeColor>> targetBlockType;
    BlockPos targetBlockPos;

    Path path;

    public static class BlockData<T> {
        public Block block;
        public T requiredBlockStateValue;

        public BlockData(Block block, T requiredBlockStateValue) {
            this.block = block;
            this.requiredBlockStateValue = requiredBlockStateValue;
        }
    }


    public AutoMineBaritone(BaritoneConfig config) {
        this.config = config;
//        executor = new PathExecutor();
        newExecutor = new Executor();
        pathFinder = new AStarPathFinder(getPathBehaviour());
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


    private void registerEventListener() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void unregisterEventListeners() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    public void disableBaritone() {
        Logger.playerLog("Disabled baritone");
        state = BaritoneState.IDLE;
        newExecutor.reset();
        terminate();
    }

    // failed = true -> Will actually terminate whole thing, otherwise just restart and pretend nothing has happened...
    private void failBaritone(boolean failed) {

        newExecutor.reset();
        if (path != null && path.getBlocksInPath() != null && !path.getBlocksInPath().isEmpty()) {
            pathFinder.addToBlackList(path.getBlocksInPath().getFirst().getPos());
        }

        if (failed) {
            state = BaritoneState.FAILED;
            terminate();
        } else {
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

        logger.info(state);
        switch (state) {
            case PATH_FINDING:
                KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, config.isShiftWhenMine());
                break;
            case EXECUTING:
                if (newExecutor.hasSuccessfullyFinished()) {
                    // TODO: Fix this
                    if (path instanceof SemiPath) {
                        startSemiPathFinding();
                    } else {
                        disableBaritone();
                    }
                } else if (newExecutor.getHasFailed()) {
                    failBaritone(false);
                } else if (!newExecutor.isExecuting()) {
                    newExecutor.executePath(path, config);
                }

                break;
            default:
                break;
        }
    }

    private void startSemiPathFinding() {
        startPathFinding();
        newExecutor.reset();
    }


    private void startPathFinding() {
        state = BaritoneState.PATH_FINDING;

        KeybindHandler.resetKeybindState();
        KeybindHandler.setKeyBindState(KeybindHandler.keyBindShift, config.isShiftWhenMine());

        MightyMiner.pathfindPool.submit(() -> {
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
        });
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
