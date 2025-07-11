package com.jelly.mightyminerv2.macro.impl.GlacialMacro.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.AutoGetStats;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.impl.MiningSpeedRetrievalTask;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.impl.PickaxeAbilityRetrievalTask;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.macro.impl.GlacialMacro.GlacialMacro;

/**
 * This state is responsible for retrieving the mining speed and pickaxe ability
 * before proceeding to the pathfinding state in the Glacial Macro.
 */
public class GettingStatsState implements GlacialMacroState {

    private final AutoGetStats autoInventory = AutoGetStats.getInstance();
    private MiningSpeedRetrievalTask miningSpeedTask;
    private PickaxeAbilityRetrievalTask pickaxeAbilityTask;

    @Override
    public void onStart(GlacialMacro macro) {
        log("Entering getting stats state");
        miningSpeedTask = new MiningSpeedRetrievalTask();
        pickaxeAbilityTask = new PickaxeAbilityRetrievalTask();
        AutoGetStats.getInstance().startTask(pickaxeAbilityTask);
        AutoGetStats.getInstance().startTask(miningSpeedTask);
    }

    @Override
    public GlacialMacroState onTick(GlacialMacro macro) {
        if (!AutoGetStats.getInstance().hasFinishedAllTasks()) {
            return this;
        }

        if (miningSpeedTask.getError() != null) {
            if (miningSpeedTask.getError().equals("Failed to parse mining speed in GUI")) {
                macro.transitionTo(new NewLobbyState());
            } else {
                macro.disable("Failed to get stats with the following error: "
                        + miningSpeedTask.getError());
            }
            return null;
        }

        if (pickaxeAbilityTask.getError() != null) {
            macro.disable("Failed to get pickaxe ability with the following error: "
                    + pickaxeAbilityTask.getError());
            return null;
        }

        macro.setMiningSpeed(miningSpeedTask.getResult());
        log("MiningSpeed: " + macro.getMiningSpeed());
        macro.setPickaxeAbility(MightyMinerConfig.usePickaxeAbility ?
                pickaxeAbilityTask.getResult() : BlockMiner.PickaxeAbility.NONE);
        return new PathfindingState();
    }

    @Override
    public void onEnd(GlacialMacro macro) {
        autoInventory.stop();
        log("Exiting getting stats state");
    }
}