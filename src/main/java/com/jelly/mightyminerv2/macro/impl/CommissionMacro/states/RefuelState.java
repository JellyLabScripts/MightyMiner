package com.jelly.mightyminerv2.macro.impl.CommissionMacro.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.AutoDrillRefuel;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.CommissionMacro;

public class RefuelState implements CommissionMacroState{
    @Override
    public void onStart(CommissionMacro macro) {
        log("Starting refuel state");
        AutoDrillRefuel.getInstance().start(MightyMinerConfig.miningTool, MightyMinerConfig.commMachineFuel, false, false);
    }

    @Override
    public CommissionMacroState onTick(CommissionMacro macro) {
        if (AutoDrillRefuel.getInstance().isRunning()) {
            return this;
        }

        if (AutoDrillRefuel.getInstance().hasSucceeded()) {
            log("Done refilling");
            return new StartingState();
        }

        switch (AutoDrillRefuel.getInstance().getFailReason()) {
            case NONE:
                macro.disable("Auto Drill Refuel failed, but no error is detected. Please contact the developer.");
                break;
            case INACCESSIBLE_MECHANIC:
                log("The NPC was inaccessible while refueling");
                return new StartingState();
            case FAILED_REFILL:
                macro.disable("Failed auto drill refill");
                break;
        }

        return null;
    }

    @Override
    public void onEnd(CommissionMacro macro) {
        AutoDrillRefuel.getInstance().stop();
        log("Ending refuel state");
    }
}
