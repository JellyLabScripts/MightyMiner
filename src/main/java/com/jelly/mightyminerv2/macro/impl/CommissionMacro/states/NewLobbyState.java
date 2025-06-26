package com.jelly.mightyminerv2.macro.impl.CommissionMacro.states;

import com.jelly.mightyminerv2.feature.impl.AutoWarp;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.CommissionMacro;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.location.Location;
import com.jelly.mightyminerv2.util.helper.location.SubLocation;

public class NewLobbyState implements CommissionMacroState {

    private final AutoWarp autoWarp = AutoWarp.getInstance();
    private final Clock delayClock = new Clock();
    private WarpPhase currentPhase = WarpPhase.TO_HUB;

    private static final long DELAY_MS_MIN = 3500;
    private static final long DELAY_MS_MAX = 5000;
    private long currentDelayDuration;

    @Override
    public void onStart(CommissionMacro macro) {
        log("Starting NewLobbyState. Current phase: " + currentPhase);
        delayClock.reset();
        if (currentPhase == WarpPhase.TO_HUB) {
            autoWarp.start(Location.HUB, null);
        } else if (currentPhase == WarpPhase.TO_FORGE) {
            autoWarp.start(null, SubLocation.THE_FORGE);
        } else if (currentPhase == WarpPhase.DELAY_AFTER_HUB) {
            currentDelayDuration = getRandomDelay();
            delayClock.schedule(currentDelayDuration);
            log("Delaying for " + (currentDelayDuration / 1000.0) + " seconds after Hub warp.");
        }
    }

    @Override
    public CommissionMacroState onTick(CommissionMacro macro) {
        if (currentPhase == WarpPhase.TO_HUB || currentPhase == WarpPhase.TO_FORGE) {
            if (autoWarp.isRunning()) {
                return this;
            }

            if (autoWarp.hasSucceeded()) {
                if (currentPhase == WarpPhase.TO_HUB) {
                    log("Successfully warped to HUB. Proceeding to warp to THE_FORGE.");
                    currentPhase = WarpPhase.TO_FORGE;
                    autoWarp.start(null, SubLocation.THE_FORGE);
                    return this;
                } else if (currentPhase == WarpPhase.TO_FORGE) {
                    log("Successfully warped to THE_FORGE. Resuming macro pathing.");
                    return new StartingState();
                }
            } else {
                switch (autoWarp.getFailReason()) {
                    case NONE:
                        macro.disable("Auto Warp failed during " + currentPhase + " phase (Hub/Forge sequence).");
                        break;
                    case FAILED_TO_WARP:
                        macro.disable("Failed to warp to " + (currentPhase == WarpPhase.TO_HUB ? "Hub" : "Forge") + ". Disabling macro.");
                        break;
                    case NO_SCROLL:
                        macro.disable("You don't have the warp scroll for " + (currentPhase == WarpPhase.TO_HUB ? "Hub" : "The Forge") + ". Disabling macro.");
                        break;
                }
            }
            return null;
        }

        if (currentPhase == WarpPhase.DELAY_AFTER_HUB) {
            if (delayClock.passed()) {
                log("Delay completed. Proceeding to warp to THE_FORGE.");
                delayClock.reset();
                currentPhase = WarpPhase.TO_FORGE;
                autoWarp.start(null, SubLocation.THE_FORGE);
            }
            return this;
        }

        return null;
    }

    @Override
    public void onEnd(CommissionMacro macro) {
        delayClock.reset();
        log("Ending NewLobbyState");
    }

    private long getRandomDelay() {
        return (long) (DELAY_MS_MIN + (Math.random() * (DELAY_MS_MAX - DELAY_MS_MIN)));
    }

    private enum WarpPhase {
        TO_HUB,
        DELAY_AFTER_HUB,
        TO_FORGE
    }
}