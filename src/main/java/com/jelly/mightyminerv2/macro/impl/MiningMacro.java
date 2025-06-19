/**
 * State machine:
 * 1. INITIALIZATION:
 *    - Presets variables.
 *    - If mining stats are available, then transition to MINING
 *    - If mining stats are NOT available, then transition to GETTING_STATS, and initialize it
 * <p>
 * 2. GETTING_STATS:
 *    - Waits for AutoInventory to finish retrieving stats.
 *    - If successful, stores stats and moves on to MINING.
 *    - If NOT successful, retries or disables the macro with error.
 * <p>
 * 3. MINING:
 *    - Starts the mining process using BlockMiner if not already started.
 *    - Verifies mining preconditions (tools, blocks, errors) and handles errors
 *
 */
package com.jelly.mightyminerv2.macro.impl;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.FeatureManager;
import com.jelly.mightyminerv2.feature.impl.AutoInventory;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.macro.AbstractMacro;
import com.jelly.mightyminerv2.util.helper.MineableBlock;
import lombok.Getter;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MiningMacro extends AbstractMacro {
    @Getter
    private static final MiningMacro instance = new MiningMacro();

    private final BlockMiner miner = BlockMiner.getInstance();

    private enum State {
        INITIALIZATION, GETTING_STATS, MINING
    }
    private State currentState = State.INITIALIZATION;

    private final List<String> necessaryItems = new ArrayList<>();
    private int miningSpeed = 0;

    private MineableBlock[] blocksToMine = {};
    private int macroRetries = 0;
    private boolean isMining = false;

    @Override
    public String getName() {
        return "Mining Macro";
    }

    @Override
    public List<String> getNecessaryItems() {
        if (necessaryItems.isEmpty()) {
            necessaryItems.add(MightyMinerConfig.miningTool);
            log("Necessary items initialized: " + necessaryItems);
        }
        return necessaryItems;
    }

    @Override
    public void onEnable() {
        log("Enabling Mining Macro");
        this.currentState = State.INITIALIZATION;
    }

    @Override
    public void onDisable() {
        log("Disabling Mining Macro");
        if (isMining) {
            miner.stop();
            isMining = false;
        }
        resetVariables();
    }

    @Override
    public void onPause() {
        FeatureManager.getInstance().pauseAll();
        log("Mining Macro paused");
    }

    @Override
    public void onResume() {
        FeatureManager.getInstance().resumeAll();
        log("Mining Macro resumed");
    }

    public void onTick(TickEvent.ClientTickEvent event) {
        if (timer.isScheduled() && !timer.passed()) return;

        log("Current state: " + currentState);
        switch (currentState) {
            case INITIALIZATION:
                handleInitializationState();
                break;
            case GETTING_STATS:
                handleGettingStatsState();
                break;
            case MINING:
                handleMiningState();
                break;
        }
    }

    private void handleInitializationState() {
        log("Handling initialization state");
        resetVariables();
        setBlocksToMineBasedOnOreType();
        if (miningSpeed == 0) {
            AutoInventory.getInstance().retrieveSpeedBoost();
            changeState(State.GETTING_STATS);
        } else {
            changeState(State.MINING);
        }
    }

    private void resetVariables() {
        macroRetries = 0;
        miningSpeed = 0;
        necessaryItems.clear();
        isMining = false;
    }

    private void handleGettingStatsState() {
        if (AutoInventory.getInstance().isRunning()) return;

        if (AutoInventory.getInstance().sbSucceeded()) {
            int[] sb = AutoInventory.getInstance().getSpeedBoostValues();
            miningSpeed = sb[0];
            macroRetries = 0;
            log("Retrieved stats - Speed: " + miningSpeed);
            changeState(State.MINING);
        } else {
            handleFailingToGetStats();
        }
    }

    private void handleFailingToGetStats() {
        log("Cannot get stats! (stats for speed boost)");
        switch (AutoInventory.getInstance().getSbError()) {
            case NONE:
                throw new IllegalStateException("AutoInventory#getSbError failed but returned NONE");
            case CANNOT_OPEN_INV:
                if (++macroRetries > 3) {
                    super.disable("Failed to open inventory after 3 attempts");
                } else {
                    changeState(State.INITIALIZATION);
                }
                break;
            case CANNOT_GET_VALUE:
                super.disable("Failed to get value. Contact the developer.");
                break;
        }
    }

    private void handleMiningState() {

        switch(miner.getError()) {
            case NO_POINTS_FOUND:
                log ("Restarting because the block chosen cannot be mined");
                changeState(State.INITIALIZATION);
                break;
            case NO_TARGET_BLOCKS:
                disable("Please set at least one type of target block in configs!");
                break;
            case NOT_ENOUGH_BLOCKS:
                disable("Not enough blocks nearby! Please move to a new vein");
                break;
            case NO_TOOLS_AVAILABLE:
                disable("Cannot find tools in hotbar! Please set it in configs");
                break;
            case NO_PICKAXE_ABILITY:
                disable("Cannot find messages for pickaxe ability! " +
                        "Either enable any pickaxe ability in HOTM or enable chat messages. You can also disable pickaxe ability in configs.");
                break;
        }

        if (!isMining) {
            miner.setWaitThreshold(MightyMinerConfig.oreRespawnWaitThreshold * 1000);
            startMining();
        }
    }

    private void startMining() {
        miner.start(
                blocksToMine,
                miningSpeed,
                determinePriority(),
                MightyMinerConfig.miningTool
        );

        isMining = true;
        log("Started mining with speed: " + miningSpeed);
    }

    private void changeState(State newState) {
        log("Changing state from " + currentState + " to " + newState);
        currentState = newState;
    }

    private void setBlocksToMineBasedOnOreType() {
        log("Setting blocks to mine based on ore type: " + MightyMinerConfig.oreType);
        switch (MightyMinerConfig.oreType) {
            case 0:
                blocksToMine = new MineableBlock[]{
                        MineableBlock.GRAY_MITHRIL,
                        MineableBlock.GREEN_MITHRIL,
                        MineableBlock.BLUE_MITHRIL,
                        MineableBlock.TITANIUM
                };
                break;
            case 1:
                blocksToMine = new MineableBlock[]{MineableBlock.DIAMOND};
                break;
            case 2:
                blocksToMine = new MineableBlock[]{MineableBlock.EMERALD};
                break;
            case 3:
                blocksToMine = new MineableBlock[]{MineableBlock.REDSTONE};
                break;
            case 4:
                blocksToMine = new MineableBlock[]{MineableBlock.LAPIS};
                break;
            case 5:
                blocksToMine = new MineableBlock[]{MineableBlock.GOLD};
                break;
            case 6:
                blocksToMine = new MineableBlock[]{MineableBlock.IRON};
                break;
            case 7:
                blocksToMine = new MineableBlock[]{MineableBlock.COAL};
                break;
            default:
                blocksToMine = new MineableBlock[]{};
                log("Invalid ore type selected");
                break;
        }
        log("Blocks to mine: " + Arrays.toString(blocksToMine));
    }

    private int[] determinePriority() {
        if (MightyMinerConfig.oreType == 0) {
            return new int[]{
                    MightyMinerConfig.mineGrayMithril ? 1 : 0,
                    MightyMinerConfig.mineGreenMithril ? 1 : 0,
                    MightyMinerConfig.mineBlueMithril ? 1 : 0,
                    MightyMinerConfig.mineTitanium ? 10 : 0
            };
        }
        return new int[]{1, 1, 1, 1};
    }

}