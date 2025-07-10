package com.jelly.mightyminerv2.macro.impl.GlacialMacro.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.handler.GameStateHandler;
import com.jelly.mightyminerv2.macro.impl.GlacialMacro.GlacialMacro;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.helper.location.SubLocation;

import java.util.Objects;

/**
 * The initial state of the Glacial Macro.
 * This state checks if the player is in the correct location to start the macro.
 * If not, it will attempt to teleport the player to the Dwarven Base Camp.
 */
public class StartingState implements GlacialMacroState {
    @Override
    public void onStart(GlacialMacro macro) {
        log("Entering starting state");

    }

    @Override
    public GlacialMacroState onTick(GlacialMacro macro) {

        if (Objects.equals(MightyMinerConfig.miningTool, "")) {
            macro.disable("Mining tool is not set in the Mighty Miner config");
            return null;
        }

        SubLocation subLocation = GameStateHandler.getInstance().getCurrentSubLocation();
        if (subLocation == SubLocation.GLACITE_TUNNELS || subLocation == SubLocation.DWARVEN_BASE_CAMP) {

            if (!InventoryUtil.areItemsInHotbar(macro.getNecessaryItems())) {
                macro.disable("Please put the following items in hotbar: " + InventoryUtil.getMissingItemsInHotbar(macro.getNecessaryItems()));
                return null;
            }
            
            log("Player is in a valid location. Initialising stats");
            return new GettingStatsState();
        } else {
            log("Player is not in the Glacite Tunnels. Teleporting...");
            return new TeleportingState(new StartingState());
        }
    }

    @Override
    public void onEnd(GlacialMacro macro) {
        log("Exiting starting state");
    }
}
