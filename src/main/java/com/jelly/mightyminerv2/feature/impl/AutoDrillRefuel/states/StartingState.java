package com.jelly.mightyminerv2.feature.impl.AutoDrillRefuel.states;

import com.jelly.mightyminerv2.feature.impl.AutoDrillRefuel.AutoDrillRefuel;
import com.jelly.mightyminerv2.util.InventoryUtil;

public class StartingState implements AutoDrillRefuelState{
    @Override
    public void onStart(AutoDrillRefuel refueler) {
        log("Entering starting state");
    }

    @Override
    public AutoDrillRefuelState onTick(AutoDrillRefuel refueler) {
        if (InventoryUtil.getSlotIdOfItemInContainer(refueler.getDrillName()) == -1) {
            logError("No drill found!");
            refueler.stop();
            refueler.setError(AutoDrillRefuel.AutoDrillRefuelError.NO_DRILL);
            return null;
        }

        if (InventoryUtil.getSlotIdOfItemInContainer(refueler.getFuelType().getName()) == -1) {
            logError("No fuel found!");
            refueler.stop();
            refueler.setError(AutoDrillRefuel.AutoDrillRefuelError.NO_FUEL);
            return null;
        }
        return new AbiphoneState();
    }

    @Override
    public void onEnd(AutoDrillRefuel refueler) {
        log("Ending starting state");
    }
}
