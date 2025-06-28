package com.jelly.mightyminerv2.macro.impl.CommissionMacro.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.CommissionMacro;
import com.jelly.mightyminerv2.util.InventoryUtil;

import java.util.Objects;

public class StartingState implements CommissionMacroState{

    @Override
    public void onStart(CommissionMacro macro) {
        log("Entering starting state");
    }

    @Override
    public CommissionMacroState onTick(CommissionMacro macro) {
        if (Objects.equals(MightyMinerConfig.miningTool, "")) {
            macro.disable("Please set a Mining Tool in the config");
            return null;
        }
        if (Objects.equals(MightyMinerConfig.slayerWeapon, "")) {
            macro.disable("Please set a Slayer Weapon in the config");
            return null;
        }
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
