package com.jelly.mightyminerv2.macro.impl.RouteMiner.states;

import com.jelly.mightyminerv2.macro.impl.RouteMiner.RouteMinerMacro;
import com.jelly.mightyminerv2.util.InventoryUtil;

/**
 * The initial state of the Route Miner Macro.
 * This state checks if the player has the proper items to start macro.
 * If not, it will disable itself.
 */
public class StartingState implements RouteMinerMacroState {

    @Override
    public void onStart(RouteMinerMacro macro) {
        log("Entering Starting State");
    }

    @Override
    public RouteMinerMacroState onTick(RouteMinerMacro macro) {
        if (!InventoryUtil.areItemsInHotbar(macro.getNecessaryItems())) {
            macro.disable("Please put the following items in hotbar: " + InventoryUtil.getMissingItemsInHotbar(macro.getNecessaryItems()));
            return null;
        }

        return new GettingStatsState();
    }

    @Override
    public void onEnd(RouteMinerMacro macro) {
        log("Exiting Starting State");
    }

}
