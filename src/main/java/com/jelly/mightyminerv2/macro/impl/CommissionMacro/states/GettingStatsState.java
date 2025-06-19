package com.jelly.mightyminerv2.macro.impl.CommissionMacro.states;

import com.jelly.mightyminerv2.feature.impl.AutoCommissionClaim;
import com.jelly.mightyminerv2.feature.impl.AutoInventory;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.CommissionMacro;

public class GettingStatsState implements CommissionMacroState{

    private final AutoInventory autoInventory = AutoInventory.getInstance();

    @Override
    public void onStart(CommissionMacro macro) {
        log("Entering getting stats state");
        autoInventory.retrieveSpeedBoost();
    }

    @Override
    public CommissionMacroState onTick(CommissionMacro macro) {
        if (autoInventory.isRunning()) {
            return this;
        }

        if (autoInventory.sbSucceeded()) {
            int[] sb = autoInventory.getSpeedBoostValues();
            macro.setMiningSpeed(sb[0]);
            return new StartingState();
        }

        switch (autoInventory.getSbError()) {
            case NONE:
                throw new IllegalStateException("AutoInventory failed but no error is detected! Please contact the developer");
            case CANNOT_OPEN_INV:
                macro.disable("Cannot open player's inventory to get statistics!");
                break;
            case CANNOT_GET_VALUE:
                macro.disable("Cannot get the value of statistics!");
                break;
        }
        return null;
    }

    @Override
    public void onEnd(CommissionMacro macro) {
        autoInventory.stop();
        log("Exiting getting stats state");
    }
}
