package com.jelly.mightyminerv2.macro.impl.GlacialMacro.states;

import akka.japi.Pair;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.RouteNavigator;
import com.jelly.mightyminerv2.handler.GraphHandler;
import com.jelly.mightyminerv2.macro.impl.GlacialMacro.GlacialMacro;
import com.jelly.mightyminerv2.macro.impl.GlacialMacro.GlaciteVeins;
import com.jelly.mightyminerv2.util.*;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.route.Route;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;

import java.util.List;

/**
 * PathfindingState is responsible for navigating to the best available vein
 * and handling the pathfinding logic for the Glacial Macro.
 */
public class PathfindingState implements GlacialMacroState {

    private boolean isNavigating = false;
    private final Clock findVeinClock = new Clock();
    private final Clock commissionCheckClock = new Clock();

    @Override
    public void onStart(GlacialMacro macro) {
        log("Starting pathing state");
        InventoryUtil.holdItem("Aspect of the Void");
        RouteNavigator.getInstance().stop(); // Ensure pathfinding is stopped
        macro.updateMiningTasks(); // Update tasks at the beginning of each pathfinding cycle
    }

    @Override
    public GlacialMacroState onTick(GlacialMacro macro) {
        if (ScoreboardUtil.cold >= MightyMinerConfig.coldThreshold) {
            send("Player is too cold. Warping to base to reset.");
            return new TeleportingState(new PathfindingState());
        }

        if (!commissionCheckClock.isScheduled() || commissionCheckClock.passed()) {
            boolean hasCompletedComm = TablistUtil.getGlaciteComs().values().stream().anyMatch(v -> v >= 100.0);
            if (hasCompletedComm) {
                log("Completed commission detected. Claiming...");
                return new ClaimingCommissionState();
            }
            commissionCheckClock.schedule(5000); // Check every 5 seconds
        }

        if (isNavigating) {
            if (RouteNavigator.getInstance().isRunning()) {
                return this; // Stay in this state, we are on our way.
            }

            // Navigation has just finished, check the result
            isNavigating = false; // Reset flag for the next cycle

            if (RouteNavigator.getInstance().succeeded()) {
                log("Successfully reached the destination vein.");
                return new MiningState();
            } else {
                Pair<GlaciteVeins, RouteWaypoint> failedVein = macro.getCurrentVein();
                logError("RouteNavigator failed to reach destination: " + (failedVein != null ? failedVein.first() : "Unknown"));

                if (failedVein != null) {
                    log("Blacklisting the unreachable vein for 5 minutes.");
                    macro.getPreviousVeins().put(failedVein, System.currentTimeMillis());
                }

                // Reset the clock to immediately try finding a NEW vein on the next tick
                findVeinClock.reset();

                // Stay in this state to find a new target
                return this;
            }
        }

        // If not navigating, find a new path
        if (findVeinClock.passed()) {
            Pair<GlaciteVeins, RouteWaypoint> bestVein = macro.findBestVein();
            if (bestVein == null) {
                log("No suitable veins found. Retrying in 10 seconds...");
                findVeinClock.schedule(10000);
                return this;
            }
            macro.setCurrentVein(bestVein);

            // Check if we are already at the destination
            if (bestVein.second().isWithinRange(PlayerUtil.getBlockStandingOn(), 2)) {
                log("Already at the destination. Starting to mine.");
                return new MiningState();
            }

            // Calculate the path
            List<RouteWaypoint> path = GraphHandler.instance.findPathFrom(macro.getName(), PlayerUtil.getBlockStandingOn(), bestVein.second());

            if (path == null || path.isEmpty()) {
                logError("Could not find a path to " + bestVein.second().toBlockPos() + ". Blacklisting and retrying.");
                macro.getPreviousVeins().put(bestVein, System.currentTimeMillis());
                return this; // Try to find a new vein on the next tick
            }

            // Start navigation
            log("Starting navigation to vein: " + bestVein.first());
            RouteNavigator.getInstance().start(new Route(path));
            isNavigating = true;
        }

        return this; // Stay in this state while waiting for clocks or pathfinding
    }

    @Override
    public void onEnd(GlacialMacro macro) {
        RouteNavigator.getInstance().stop();
        log("Exiting pathfinding state.");
    }
}