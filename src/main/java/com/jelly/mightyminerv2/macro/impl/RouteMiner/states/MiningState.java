package com.jelly.mightyminerv2.macro.impl.RouteMiner.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.macro.impl.GlacialMacro.GlacialMacro;
import com.jelly.mightyminerv2.macro.impl.RouteMiner.RouteMinerMacro;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.helper.MineableBlock;

/**
 * This state is responsible for starting BlockMiner and detecting when to move to next waypoint
 * before proceeding to the moving state in the Route Miner Macro.
 */
public class MiningState implements RouteMinerMacroState {

    @Override
    public void onStart(RouteMinerMacro macro) {
        log("Entering Mining State");
        InventoryUtil.holdItem(MightyMinerConfig.miningTool);
        startMining(macro);
    }

    @Override
    public RouteMinerMacroState onTick(RouteMinerMacro macro) {
        if (BlockMiner.getInstance().getError() == BlockMiner.BlockMinerError.NOT_ENOUGH_BLOCKS) {
            BlockMiner.getInstance().stop();
            return new MovingState();
        }

        return this;
    }

    private void startMining(RouteMinerMacro macro) {
        MineableBlock[] blocksToMine = macro.getBlocksToMine();

        if (blocksToMine.length == 0) {
            log("No blocks to mine for current commissions.");
            return;
        }

        BlockMiner.getInstance().start(
                blocksToMine,
                macro.getMiningSpeed(),
                macro.getPickaxeAbility(),
                macro.getBlockPriority(),
                MightyMinerConfig.miningTool
        );
    }

    @Override
    public void onEnd(RouteMinerMacro macro) {
        log("Exiting Mining State");
    }

}
