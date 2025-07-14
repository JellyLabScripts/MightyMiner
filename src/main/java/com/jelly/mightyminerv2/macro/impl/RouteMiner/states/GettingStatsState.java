package com.jelly.mightyminerv2.macro.impl.RouteMiner.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.AutoGetStats;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.impl.MiningSpeedRetrievalTask;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.impl.PickaxeAbilityRetrievalTask;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.macro.impl.GlacialMacro.GlacialMacro;
import com.jelly.mightyminerv2.macro.impl.GlacialMacro.states.GlacialMacroState;
import com.jelly.mightyminerv2.macro.impl.GlacialMacro.states.NewLobbyState;
import com.jelly.mightyminerv2.macro.impl.GlacialMacro.states.PathfindingState;
import com.jelly.mightyminerv2.macro.impl.RouteMiner.RouteMinerMacro;

/**
 * This state is responsible for retrieving the mining speed and pickaxe ability
 * before proceeding to the mining state in the Route Miner Macro.
 */
public class GettingStatsState implements RouteMinerMacroState {

    private final AutoGetStats autoInventory = AutoGetStats.getInstance();
    private MiningSpeedRetrievalTask miningSpeedTask;
    private PickaxeAbilityRetrievalTask pickaxeAbilityTask;

    @Override
    public void onStart(RouteMinerMacro macro) {
        log("Entering getting stats state");
        miningSpeedTask = new MiningSpeedRetrievalTask();
        pickaxeAbilityTask = new PickaxeAbilityRetrievalTask();
        AutoGetStats.getInstance().startTask(pickaxeAbilityTask);
        AutoGetStats.getInstance().startTask(miningSpeedTask);
    }

    @Override
    public RouteMinerMacroState onTick(RouteMinerMacro macro) {
        if (!AutoGetStats.getInstance().hasFinishedAllTasks()) {
            return this;
        }

        if (miningSpeedTask.getError() != null) {
            macro.disable("Failed to get stats with the following error: " + miningSpeedTask.getError());
            return null;
        }

        if (pickaxeAbilityTask.getError() != null) {
            macro.disable("Failed to get pickaxe ability with the following error: " + pickaxeAbilityTask.getError());
            return null;
        }

        macro.setMiningSpeed(miningSpeedTask.getResult());
        macro.setPickaxeAbility(MightyMinerConfig.usePickaxeAbility ? pickaxeAbilityTask.getResult() : BlockMiner.PickaxeAbility.NONE);
        return new MiningState();
    }

    @Override
    public void onEnd(RouteMinerMacro macro) {
        autoInventory.stop();
        log("Exiting getting stats state");
    }

}
