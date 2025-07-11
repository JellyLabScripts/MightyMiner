package com.jelly.mightyminerv2.macro.impl.GlacialMacro.states;

import com.jelly.mightyminerv2.feature.impl.AutoWarp;
import com.jelly.mightyminerv2.macro.impl.GlacialMacro.GlacialMacro;
import com.jelly.mightyminerv2.util.helper.location.Location;
import com.jelly.mightyminerv2.util.helper.location.SubLocation;

public class NewLobbyState implements GlacialMacroState {

    private final AutoWarp autoWarp = AutoWarp.getInstance();
    private WarpPhase currentPhase = WarpPhase.TO_HUB;

    @Override
    public void onStart(GlacialMacro macro) {
        log("Starting NewLobbyState");

        log("Clearing previously visited veins blacklist to prepare for new lobby.");
        macro.getPreviousVeins().clear();

        if (currentPhase == WarpPhase.TO_HUB) {
            autoWarp.start(Location.HUB, null);
        }
    }

    @Override
    public GlacialMacroState onTick(GlacialMacro macro) {
        if (autoWarp.isRunning()) {
            return this;
        }

        if (autoWarp.hasSucceeded()) {
            if (currentPhase == WarpPhase.TO_HUB) {
                log("Successfully warped to Hub. Warping back to Dwarven Base Camp.");
                currentPhase = WarpPhase.TO_BASE;

                autoWarp.start(null, SubLocation.DWARVEN_BASE_CAMP);
                return this;
            } else if (currentPhase == WarpPhase.TO_BASE) {
                log("Successfully returned to Dwarven Base Camp. Resuming pathfinding.");

                return new PathfindingState();
            }
        } else {
            logError("AutoWarp failed during NewLobby sequence. Reason: " + autoWarp.getFailReason());
            return new ErrorHandlingState("Failed to execute new lobby sequence with reason: " + autoWarp.getFailReason());
        }

        return this;
    }

    @Override
    public void onEnd(GlacialMacro macro) {
        if (autoWarp.isRunning()) {
            autoWarp.stop();
        }
        log("Ending NewLobbyState");
    }

    private enum WarpPhase {
        TO_HUB,
        TO_BASE
    }
}