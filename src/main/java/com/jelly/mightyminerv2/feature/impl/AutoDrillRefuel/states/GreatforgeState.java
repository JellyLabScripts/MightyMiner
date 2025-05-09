package com.jelly.mightyminerv2.feature.impl.AutoDrillRefuel.states;

import com.jelly.mightyminerv2.feature.impl.AutoDrillRefuel.AutoDrillRefuel;
import com.jelly.mightyminerv2.util.InventoryUtil;

public class GreatforgeState implements AutoDrillRefuelState{

    private int tickCounter;

    @Override
    public void onStart(AutoDrillRefuel refueler) {
        tickCounter = 0;
        log("Entering Greatforge state");
    }

    @Override
    public AutoDrillRefuelState onTick(AutoDrillRefuel refueler) {

        if (InventoryUtil.getInventoryName().contains("Drill Anvil") && InventoryUtil.isInventoryLoaded()) {
            log("Opened anvil GUI and tick counter: " + tickCounter);
            tickCounter ++;

            // Click the drill after 10 ticks (0.5 seconds)
            if(tickCounter == 10) {
                int slotID = InventoryUtil.getSlotIdOfItemInContainer(refueler.getDrillName());
                if(slotID == -1) {
                    logError("No drill found! In theory this should NEVER happen!!! Please contact the developer");
                    refueler.stop();
                    refueler.setError(AutoDrillRefuel.AutoDrillRefuelError.NO_DRILL);
                    return null;
                }
                InventoryUtil.clickContainerSlot(
                        slotID,
                        0,
                        InventoryUtil.ClickMode.QUICK_MOVE);
            }

            // Click the fuel after 20 ticks (1 second)
            if(tickCounter == 20) {
                int slotID = InventoryUtil.getSlotIdOfItemInContainer(refueler.getFuelType().getName());
                if(slotID == -1) {
                    logError("No fuel found! In theory this should NEVER happen!!! Please contact the developer");
                    refueler.stop();
                    refueler.setError(AutoDrillRefuel.AutoDrillRefuelError.NO_FUEL);
                    return null;
                }
                InventoryUtil.clickContainerSlot(
                        slotID,
                        0,
                        InventoryUtil.ClickMode.QUICK_MOVE);
            }

            // Click the confirm refuel button after 30 ticks (1.5 seconds)
            if(tickCounter == 30) {
                InventoryUtil.clickContainerSlot(
                        InventoryUtil.getSlotIdOfItemInContainer("Drill Anvil"),
                        0,
                        InventoryUtil.ClickMode.PICKUP);
            }

            // Move the drill back to the hotbar after 40 ticks (2 seconds)
            if(tickCounter == 40){
                int slotID = InventoryUtil.getSlotIdOfItemInContainer(refueler.getDrillName());
                if(slotID == -1) {
                    logError("No drill found! In theory this should NEVER happen!!! Please contact the developer");
                    refueler.stop();
                    refueler.setError(AutoDrillRefuel.AutoDrillRefuelError.NO_DRILL);
                    return null;
                }
                InventoryUtil.clickContainerSlot(
                        slotID,
                        0,
                        InventoryUtil.ClickMode.QUICK_MOVE);
            }

            // End auto refuel after 50 ticks (2.5 seconds)
            if(tickCounter == 50) {
                log("Auto refuel finished");
                refueler.stop();
                return null;
            }
        }
        return this;
    }

    @Override
    public void onEnd(AutoDrillRefuel refueler) {
        InventoryUtil.closeScreen();
        log("Ending Greatforge state");
    }
}
