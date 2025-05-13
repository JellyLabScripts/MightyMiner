package com.jelly.mightyminerv2.macro.impl.CommissionMacro.states;

import com.jelly.mightyminerv2.feature.impl.AutoCommissionClaim;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.Commission;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.CommissionMacro;

import java.util.List;

public class ClaimingCommissionState implements CommissionMacroState{
    @Override
    public void onStart(CommissionMacro macro) {
        log("Starting claiming commission state");
        AutoCommissionClaim.getInstance().start();
    }

    @Override
    public CommissionMacroState onTick(CommissionMacro macro) {

        if (AutoCommissionClaim.getInstance().isRunning()) {
            return this;
        }

        if (AutoCommissionClaim.getInstance().succeeded()) {
            return new StartingState();
        }

        switch (AutoCommissionClaim.getInstance().claimError()) {
            case NONE:
                macro.disable("Auto commission claiming failed, but no error is detected. Please contact the developer.");
                break;
            case INACCESSIBLE_NPC:
                log("The NPC was inaccessible while claiming commission");
                return new WarpingState();
            case TIMEOUT:
                log("Timeout in auto commission claiming");
                return new StartingState();
            case NO_ITEMS:
                macro.disable("No royal pigeons found, but this shouldn't happen. Please contact the developer.");
                break;
            case NPC_NOT_UNLOCKED:
                macro.disable("You have not unlocked Emissaries at Commission Milestone 1. Please post mc logs in #bug-report if this is a mistake.");
                break;
        }
        return null;
    }

    @Override
    public void onEnd(CommissionMacro macro) {
        AutoCommissionClaim.getInstance().stop();
        log("Ending claiming commission state");
    }
}
