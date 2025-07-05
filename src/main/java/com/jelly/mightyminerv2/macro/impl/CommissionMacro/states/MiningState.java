package com.jelly.mightyminerv2.macro.impl.CommissionMacro.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.Commission;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.CommissionMacro;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.helper.MineableBlock;

public class MiningState implements CommissionMacroState{


    private final BlockMiner miner = BlockMiner.getInstance();
    private final MineableBlock[] blocksToMine = {MineableBlock.GRAY_MITHRIL, MineableBlock.GREEN_MITHRIL, MineableBlock.BLUE_MITHRIL,
            MineableBlock.TITANIUM};

    private final int[] mithrilPriority = {10, 6, 3, 1};
    private final int[] titaniumPriority = {3, 2, 1, 20};

    @Override
    public void onStart(CommissionMacro macro) {
        log("Starting mining state");
        miner.start(
                blocksToMine,
                macro.getMiningSpeed(),
                CommissionMacro.getInstance().getPickaxeAbility(),
                macro.getCurrentCommission().getName().contains("Titanium") ? titaniumPriority : mithrilPriority,
                MightyMinerConfig.miningTool
        );
    }

    @Override
    public CommissionMacroState onTick(CommissionMacro macro) {

        String miningTool = MightyMinerConfig.miningTool;
        if (miningTool.toLowerCase().contains("drill") || InventoryUtil.getFullName(miningTool).contains("Drill")) {
            //log("Fuel detected: " + InventoryUtil.getDrillRemainingFuel(miningTool));
            if (InventoryUtil.getDrillRemainingFuel(miningTool) <= 100) {
                log("Less than 100 fuel left in drill. Starting to refuel");
                if(MightyMinerConfig.drillRefuel)
                    return new RefuelState();
                else {
                    macro.disable("Very little fuel left in drill");
                    return null;
                }
            }
        }

        if (macro.getCurrentCommission() == Commission.COMMISSION_CLAIM){
            return new PathingState();
        }

        if (miner.isRunning()) {
            return this;
        }

        // TODO: Pathfind to a new vein when not enough blocks nearby
        switch(miner.getError()) {
            case NONE:
                break;
            case NO_POINTS_FOUND:
                log ("Restarting because the block chosen cannot be mined");
                return new MiningState();
            case NOT_ENOUGH_BLOCKS:
                log ("Not enough blocks nearby! Restarting macro");
                return new StartingState();
            case NO_PICKAXE_ABILITY:
                macro.disable("Cannot find messages for pickaxe ability! " +
                        "Either enable any pickaxe ability in HOTM or enable chat messages. You can also disable pickaxe ability in configs.");
                break;
            default:
                logError("Block miner error: " + miner.getError().name());
                macro.disable("Block miner failed unexpectedly! Please send the logs to the developer");
                break;
        }
        return null;
    }

    @Override
    public void onEnd(CommissionMacro macro) {
        miner.stop();
        log("Ending mining state");
    }
}
