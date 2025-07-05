package com.jelly.mightyminerv2.macro.impl.CommissionMacro.states;

import com.jelly.mightyminerv2.feature.impl.AutoGetStats.AutoGetStats;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.impl.MiningSpeedRetrievalTask;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.impl.PickaxeAbilityRetrievalTask;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.CommissionMacro;

public class GettingStatsState implements CommissionMacroState{

    private final AutoGetStats autoInventory = AutoGetStats.getInstance();
    private MiningSpeedRetrievalTask miningSpeedRetrievalTask;
    private PickaxeAbilityRetrievalTask pickaxeAbilityRetrievalTask;

    @Override
    public void onStart(CommissionMacro macro) {
        log("Entering getting stats state");
        miningSpeedRetrievalTask = new MiningSpeedRetrievalTask();
        pickaxeAbilityRetrievalTask = new PickaxeAbilityRetrievalTask();
        AutoGetStats.getInstance().startTask(miningSpeedRetrievalTask);
        AutoGetStats.getInstance().startTask(pickaxeAbilityRetrievalTask);
    }

    @Override
    public CommissionMacroState onTick(CommissionMacro macro) {
        if (!AutoGetStats.getInstance().hasFinishedAllTasks())
            return this ;

        if (miningSpeedRetrievalTask.getError() != null) {
            macro.disable("Failed to get stats with the following error: "
                    + miningSpeedRetrievalTask.getError());
            return null;
        }

        macro.setMiningSpeed(miningSpeedRetrievalTask.getResult());
        macro.setPickaxeAbility(pickaxeAbilityRetrievalTask.getResult());
        return new StartingState();
    }

    @Override
    public void onEnd(CommissionMacro macro) {
        autoInventory.stop();
        log("Exiting getting stats state");
    }
}
