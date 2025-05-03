package com.jelly.mightyminerv2.macro.impl;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.AutoInventory;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.feature.impl.Pathfinder;
import com.jelly.mightyminerv2.feature.impl.RouteNavigator;
import com.jelly.mightyminerv2.handler.RouteHandler;
import com.jelly.mightyminerv2.macro.AbstractMacro;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.helper.MineableBlock;
import lombok.Getter;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class RouteMinerMacro extends AbstractMacro {

    @Getter
    public static RouteMinerMacro instance = new RouteMinerMacro();
    public final BlockMiner miner = BlockMiner.getInstance();
    private MainState mainState = MainState.INITIALIZATION;
    private InitializeState initializeState = InitializeState.STARTING;
    private State state = State.STARTING;

    private int macroRetries = 0;
    public int miningSpeed = 200;

    @Override
    public String getName() {
        return "Route Miner Macro";
    }

    @Override
    public void onEnable() {
        mainState = MainState.INITIALIZATION;
        state = State.STARTING;
        miner.setWait_threshold(0);
        RouteNavigator.getInstance().queueRoute(RouteHandler.getInstance().getSelectedRoute());
        super.onEnable();
    }

    @Override
    public void onDisable() {
        miner.stop();
        Pathfinder.getInstance().stop();
        RouteNavigator.getInstance().stop();
        super.onDisable();
    }

    @Override
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!this.isEnabled()) {
            return;
        }

        if (this.isTimerRunning()) {
            return;
        }

        switch (mainState) {
            case NONE:
                MacroManager.getInstance().disable();
                return;
            case INITIALIZATION:
                if (MightyMinerConfig.routeType) {
                    mainState = MainState.MACRO;
                    state = State.STARTING;
                    return;
                }

                handleInitializeState();
                return;
            case MACRO:
                handleMacroState();
        }
    }

    private void handleInitializeState() {
        switch (initializeState) {
            case STARTING:
                initializeState = InitializeState.CHECKING_STATS;
                break;
            case CHECKING_STATS:
                AutoInventory.getInstance().retrieveSpeedBoost();
                this.initializeState = InitializeState.GETTING_STATS;
                break;
            case GETTING_STATS:
                if (AutoInventory.getInstance().isRunning()) {
                    return;
                }

                if (AutoInventory.getInstance().sbSucceeded()) {
                    int[] sb = AutoInventory.getInstance().getSpeedBoostValues();
                    this.miningSpeed = sb[0];
                    this.macroRetries = 0;
                    this.mainState = MainState.MACRO;
                    this.state = State.STARTING;
                    log("MiningSpeed: " + miningSpeed);
                    return;
                }

                switch (AutoInventory.getInstance().getSbError()) {
                    case NONE:
                        throw new IllegalStateException("AutoInventory#getSbError failed but returned NONE");
                    case CANNOT_OPEN_INV:
                        if (++this.macroRetries > 3) {
                            this.mainState = MainState.NONE;
                            error("Tried 3 times to open inv but failed. Stopping");
                        } else {
                            this.initializeState = InitializeState.STARTING;
                            log("Failed to open inventory. Retrying");
                        }
                        break;
                    case CANNOT_GET_VALUE:
                        this.mainState = MainState.NONE;
                        error("Failed To Get Value. Follow Previous Instruction (If Any) or contact the developer.");
                        break;
                }

                mainState = MainState.MACRO;
                state = State.STARTING;
                break;
        }
    }

    private void handleMacroState() {
        switch (state) {
            case STARTING:
                state = State.NEXT_WAYPOINT;
                return;
            case NEXT_WAYPOINT:
                if (!mc.gameSettings.keyBindAttack.isKeyDown()) {
                    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, true);
                }

                RouteNavigator.getInstance().gotoNext();
                state = State.MINE;
                return;
            case MINE:
                if (RouteNavigator.getInstance().isRunning() || Pathfinder.getInstance().isRunning()) {
                    return;
                }

                if (MightyMinerConfig.routeType) {
                    state = State.NEXT_WAYPOINT;
                    return;
                }

                if (miner.getError() == BlockMiner.BlockMinerError.NOT_ENOUGH_BLOCKS) {
                    miner.stop();
                    state = State.NEXT_WAYPOINT;
                    return;
                }

                if (!miner.isRunning()) {
                    miner.start(getBlocksToMine(), miningSpeed, getBlockPriority(), MightyMinerConfig.miningTool);
                }
        }
    }

    public MineableBlock[] getBlocksToMine() {
        MineableBlock[] blocksToMine = new MineableBlock[]{};
        List<MineableBlock> blocksList = new ArrayList<>();

        switch (MightyMinerConfig.routeTarget) {
            case 0:
                blocksList.add(MineableBlock.IRON);
            case 1:
                blocksList.add(MineableBlock.REDSTONE);
            case 2:
                blocksList.add(MineableBlock.COAL);
            case 3:
                blocksList.add(MineableBlock.GOLD);
            case 4:
                blocksList.add(MineableBlock.LAPIS);
            case 5:
                blocksList.add(MineableBlock.DIAMOND);
            case 6:
                blocksList.add(MineableBlock.EMERALD);
            case 7:
                blocksList.add(MineableBlock.QUARTZ);
            case 8:
                blocksList.add(MineableBlock.RUBY);
            case 9:
                blocksList.add(MineableBlock.SAPPHIRE);
            case 10:
                blocksList.add(MineableBlock.JADE);
            case 11:
                blocksList.add(MineableBlock.AMETHYST);
            case 12:
                blocksList.add(MineableBlock.TOPAZ);
            case 13:
                blocksList.add(MineableBlock.ONYX);
            case 14:
                blocksList.add(MineableBlock.AQUAMARINE);
            case 15:
                blocksList.add(MineableBlock.CITRINE);
            case 16:
                blocksList.add(MineableBlock.PERIDOT);
            case 17:
                blocksList.add(MineableBlock.JASPER);
            case 18:
                blocksList.add(MineableBlock.GLACITE);
            case 19:
                blocksList.add(MineableBlock.UMBER);
            case 20:
                blocksList.add(MineableBlock.TUNGSTEN);
        }

        return blocksList.toArray(blocksToMine);
    }

    public int[] getBlockPriority() {
        return new int[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    }

    @Override
    public List<String> getNecessaryItems() {
        List<String> items = new ArrayList<>();

        if (MightyMinerConfig.drillSwap) {
            items.add(MightyMinerConfig.altMiningTool);
        }

        if (MightyMinerConfig.commDrillRefuel) {
            items.add("Abiphone");
        }

        return items;
    }

    private enum MainState {
        NONE, INITIALIZATION, MACRO
    }

    enum InitializeState {
        STARTING, CHECKING_STATS, GETTING_STATS
    }

    enum State {
        STARTING, NEXT_WAYPOINT, MINE
    }

}