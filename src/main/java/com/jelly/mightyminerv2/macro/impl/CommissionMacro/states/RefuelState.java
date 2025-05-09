package com.jelly.mightyminerv2.macro.impl.CommissionMacro.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.AutoDrillRefuel.AutoDrillRefuel;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.CommissionMacro;

public class RefuelState implements CommissionMacroState{

    private final AutoDrillRefuel.FuelType[] fuelTypeMap =
            {AutoDrillRefuel.FuelType.VOLTA, AutoDrillRefuel.FuelType.OIL_BARREL};

    @Override
    public void onStart(CommissionMacro macro) {
        log("Starting refuel state");
        AutoDrillRefuel.getInstance().start(MightyMinerConfig.miningTool, fuelTypeMap[MightyMinerConfig.commMachineFuel]);
    }

    @Override
    public CommissionMacroState onTick(CommissionMacro macro) {
        if (AutoDrillRefuel.getInstance().isRunning()) {
            return this;
        }

        switch (AutoDrillRefuel.getInstance().getError()) {
            case NONE:
                log("Done refilling");
                return new StartingState();
            case NO_DRILL:
                macro.disable("No drill found! This should not happen!");
                break;
            case NO_ABIPHONE:
                macro.disable("No abiphone found! This should not happen!");
                break;
            case NO_FUEL:
                macro.disable("No fuel found! Please put the fuel in the inventory, not mining sacks!");
                break;
            case NO_GREATFORGE_CONTACT:
                macro.disable("No Greatforge contact in abiphone!");
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
