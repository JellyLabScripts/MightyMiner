package com.jelly.mightyminerv2.macro.impl.MiningMacro;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.FeatureManager;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.AutoGetStats;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.impl.MiningSpeedRetrievalTask;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.impl.PickaxeAbilityRetrievalTask;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.macro.AbstractMacro;
import com.jelly.mightyminerv2.util.helper.MineableBlock;
import lombok.Getter;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>This macro retrieves the player's mining speed before starting the mining loop.
 * It determines which blocks to mine based on MightyMiner configs, and coordinates
 * with the {@link BlockMiner} to perform mining actions.</p>
 */
public class MiningMacro extends AbstractMacro {
    @Getter
    private static final MiningMacro instance = new MiningMacro();


    private final BlockMiner miner = BlockMiner.getInstance();
    private final List<String> necessaryItems = new ArrayList<>();

    private MiningSpeedRetrievalTask miningSpeedRetrievalTask;
    private PickaxeAbilityRetrievalTask pickaxeAbilityRetrievalTask;
    private int miningSpeed = 0;
    private BlockMiner.PickaxeAbility pickaxeAbility = BlockMiner.PickaxeAbility.NONE;

    private MineableBlock[] blocksToMine = {};
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
        resetVariables();
        setBlocksToMineBasedOnOreType();

        if (miningSpeed == 0) {
            miningSpeedRetrievalTask = new MiningSpeedRetrievalTask();
            pickaxeAbilityRetrievalTask = new PickaxeAbilityRetrievalTask();
            AutoGetStats.getInstance().startTask(miningSpeedRetrievalTask);
            AutoGetStats.getInstance().startTask(pickaxeAbilityRetrievalTask);
        }
    }

    @Override
    public void onDisable() {
        log("Disabling Mining Macro");
        miner.stop();
        isMining = false;
        resetVariables();
    }

    private void resetVariables() {
        miningSpeed = 0;
        necessaryItems.clear();
        isMining = false;
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
        if (miningSpeed == 0) {
            handleGettingStats();
            return;
        }

        if (!isMining) {
            miner.setWaitThreshold(MightyMinerConfig.oreRespawnWaitThreshold * 1000);
            miner.start(
                    blocksToMine,
                    miningSpeed,
                    pickaxeAbility,
                    determinePriority(),
                    MightyMinerConfig.miningTool
            );

            isMining = true;
            log("Started mining with speed: " + miningSpeed);
            log("Started mining with pickaxe ability: " + pickaxeAbility.name());
        }

        handleMining();
    }

    private void handleGettingStats() {
        if (!AutoGetStats.getInstance().hasFinishedAllTasks())
            return;

        if (miningSpeedRetrievalTask.getError() != null) {
            super.disable("Failed to get stats with the following error: "
                    + miningSpeedRetrievalTask.getError());
            return;
        }

        if (pickaxeAbilityRetrievalTask.getError() != null) {
            super.disable("Failed to get pickaxe ability with the following error: "
                    + pickaxeAbilityRetrievalTask.getError());
            return;
        }

        miningSpeed = miningSpeedRetrievalTask.getResult();
        pickaxeAbility = MightyMinerConfig.usePickaxeAbility ?
                pickaxeAbilityRetrievalTask.getResult() : BlockMiner.PickaxeAbility.NONE;
    }

    private void handleMining() {
        switch(miner.getError()) {
            case NO_POINTS_FOUND:
                log("Restarting because the block chosen cannot be mined");
                isMining = false;
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