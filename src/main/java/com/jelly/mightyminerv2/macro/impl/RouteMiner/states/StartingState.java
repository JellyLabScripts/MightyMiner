package com.jelly.mightyminerv2.macro.impl.RouteMiner.states;

import com.jelly.mightyminerv2.macro.impl.RouteMiner.RouteMinerMacro;
import com.jelly.mightyminerv2.util.InventoryUtil;

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

        log("Player is in a valid location. Initialising stats");
        return new GettingStatsState();
    }

    @Override
    public void onEnd(RouteMinerMacro macro) {
        log("Exiting Starting State");
    }

}
