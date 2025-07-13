package com.jelly.mightyminerv2.macro.impl.RouteMiner.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.RouteNavigator;
import com.jelly.mightyminerv2.handler.RouteHandler;
import com.jelly.mightyminerv2.macro.impl.RouteMiner.RouteMinerMacro;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.PlayerUtil;
import com.jelly.mightyminerv2.util.helper.route.Route;
import com.jelly.mightyminerv2.util.helper.route.TransportMethod;

/**
 * Moving State is responsible for navigating to the next waypoint
 * and handling the logic for the Route Miner Macro.
 */
public class MovingState implements RouteMinerMacroState {

    @Override
    public void onStart(RouteMinerMacro macro) {
        log("Entering Moving State");
        Route route = RouteHandler.getInstance().getSelectedRoute();
        RouteNavigator navigator = RouteNavigator.getInstance();
        navigator.queueRoute(route);

        int nextIndex = navigator.getCurrentIndex(PlayerUtil.getBlockStandingOn()) + 1;

        if (route.get(nextIndex).getTransportMethod() == TransportMethod.ETHERWARP) {
            InventoryUtil.holdItem("Aspect of the Void");
        }

        navigator.goTo(nextIndex);
    }

    @Override
    public RouteMinerMacroState onTick(RouteMinerMacro macro) {
        if (RouteNavigator.getInstance().isRunning()) {
            return this;
        }

        return new MiningState();
    }

    @Override
    public void onEnd(RouteMinerMacro macro) {
        log("Exiting Moving State");
    }

}
