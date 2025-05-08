package com.jelly.mightyminerv2.macro.impl.CommissionMacro.states;

import com.jelly.mightyminerv2.macro.impl.CommissionMacro.CommissionMacro;
import com.jelly.mightyminerv2.util.InventoryUtil;

public class StartingState implements CommissionMacroState{

    @Override
    public void onStart(CommissionMacro macro) {
        log("Entering starting state");
    }

    @Override
    public CommissionMacroState onTick(CommissionMacro macro) {
        if (!InventoryUtil.areItemsInHotbar(macro.getNecessaryItems())) {
            macro.disable("Please put the following items in hotbar: " + InventoryUtil.getMissingItemsInHotbar(macro.getNecessaryItems()));
            return null;
        }
        return macro.getMiningSpeed() == 0 ? new GettingStatsState() : new PathingState();
    }

    @Override
    public void onEnd(CommissionMacro macro) {
        log("Exiting starting state");
    }
}
