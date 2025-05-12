package com.jelly.mightyminerv2.macro.impl.CommissionMacro.states;

import cc.polyfrost.oneconfig.libs.checker.units.qual.C;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.RouteNavigator;
import com.jelly.mightyminerv2.handler.GraphHandler;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.CommissionMacro;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.Commission;
import com.jelly.mightyminerv2.util.PlayerUtil;
import com.jelly.mightyminerv2.util.helper.route.Route;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;

import java.util.List;

public class PathingState implements CommissionMacroState{

    private final RouteNavigator routeNavigator = RouteNavigator.getInstance();
    private final String GRAPH_NAME = "Commission Macro";
    private int attempts = 0;

    // Cooldown for "Commission is empty" message
    private static long lastCommissionEmptyMessageTime = 0L;
    private static final long COMMISSION_EMPTY_MESSAGE_COOLDOWN_MS = 5000; // 5 seconds

    @Override
    public void onStart(CommissionMacro macro) {
        log("Starting pathing state");
        Commission commission = macro.getCurrentCommission();

        // When using royal pigeon or refueling using abiphone, no path finding is needed
        if ((commission == Commission.COMMISSION_CLAIM && MightyMinerConfig.commClaimMethod == 1)
                || commission == Commission.REFUEL) {
            return;
        }

        if(commission == null){
            log("Commission is empty!");
            return;
        }

        RouteWaypoint end = commission.getWaypoint();
        List<RouteWaypoint> nodes = GraphHandler.instance.findPathFrom(GRAPH_NAME, PlayerUtil.getBlockStandingOn(), end);

        if (nodes.isEmpty()) {
            logError("Starting block: " + PlayerUtil.getBlockStandingOn() + ", Ending block: " + end);
            macro.disable("Could not find a path to the target block! Please send the logs to the developer.");
            return;
        }

        routeNavigator.start(new Route(nodes));
    }

    @Override
    public CommissionMacroState onTick(CommissionMacro macro) {

        if (macro.getCurrentCommission() == null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastCommissionEmptyMessageTime > COMMISSION_EMPTY_MESSAGE_COOLDOWN_MS) {
                send("Commission is empty! Restarting macro to wait for tab-list updates. " +
                        "Note that this is usually temporary and caused by lags in the server.");
                lastCommissionEmptyMessageTime = currentTime;
            }
            return new StartingState();
        }

        if (macro.getCurrentCommission() == Commission.COMMISSION_CLAIM && MightyMinerConfig.commClaimMethod == 1)
            return new ClaimingCommissionState();

        if (routeNavigator.isRunning()) {
            return this;
        }

        if (routeNavigator.succeeded()) {
            String commName = macro.getCurrentCommission().getName();

            if (commName.contains("Claim")) {
                return new ClaimingCommissionState();
            } else if (commName.contains("Titanium") || commName.contains("Mithril")) {
                return new MiningState();
            } else {
                return new MobKillingState();
            }
        }

        switch (routeNavigator.getNavError()) {
            case NONE :
                macro.disable("Route navigator failed, but no error is detected. Please contact the developer.");
                break;
            case TIME_FAIL: case PATHFIND_FAILED:
                attempts++;
                if(attempts >= 3) {
                    logError("Failed to pathfind. Warping and restarting");
                    return new WarpingState();
                } else {
                    logError("Failed to pathfind. Retrying to pathfind");
                    onStart(macro);
                    return this;
                }
        }
        return null;
    }

    @Override
    public void onEnd(CommissionMacro macro) {
        routeNavigator.stop();
        log("Ending pathing state");
    }
}
