package com.jelly.mightyminerv2.macro.impl.RouteMiner.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.Pathfinder;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.handler.RouteHandler;
import com.jelly.mightyminerv2.macro.impl.RouteMiner.RouteMinerMacro;
import com.jelly.mightyminerv2.util.AngleUtil;
import com.jelly.mightyminerv2.util.BlockUtil;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.route.Route;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.util.helper.route.WaypointType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Vec3;

import java.util.List;

/**
 * Moving State is responsible for navigating to the next waypoint
 * and handling the logic for the Route Miner Macro.
 * TODO: Use RouteNavigator (temporary fix bc etherwarp doesn't work properly in RouteNavigator?)
 */
public class MovingState implements RouteMinerMacroState {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private RouteWaypoint routeTarget;
    private final Clock etherWarpDelay = new Clock();
    private boolean hasClicked = false;

    @Override
    public void onStart(RouteMinerMacro macro) {
        log("Entering Moving State");
        Route route = RouteHandler.getInstance().getSelectedRoute();
        routeTarget = route.get(macro.getRouteIndex() + 1);

        if (routeTarget.getTransportMethod() == WaypointType.ETHERWARP) {
            InventoryUtil.holdItem("Aspect of the Void");
            KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, true);
            List<Vec3> points = BlockUtil.bestPointsOnBestSide(routeTarget.toBlockPos());
            Vec3 point = routeTarget.toVec3().addVector(0.5, 0.5, 0.5);

            if (!points.isEmpty()) {
                point = points.get(0);
            }

            RotationHandler.getInstance().easeTo(new RotationConfiguration(
                    AngleUtil.getRotation(point),
                    MightyMinerConfig.delayAutoAotvEtherwarpLookDelay,
                    null
            ));
        } else {
            KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, false);
        }
    }

    @Override
    public RouteMinerMacroState onTick(RouteMinerMacro macro) {
        switch (routeTarget.getTransportMethod()) {
            case ETHERWARP:
                if (RotationHandler.getInstance().isEnabled()) {
                    return this;
                }

                if (!hasClicked) {
                    KeyBindUtil.rightClick();
                    etherWarpDelay.schedule(250);
                    hasClicked = true;
                    return this;
                }

                if (etherWarpDelay.passed()) {
                    macro.setRouteIndex(macro.getRouteIndex() + 1);
                    return new MovingState();
                }

                return this;
            case WALK:
                if (Pathfinder.getInstance().completedPathTo(routeTarget.toBlockPos())) {
                    macro.setRouteIndex(macro.getRouteIndex() + 1);
                    return new MovingState();
                }

                Pathfinder.getInstance().stopAndRequeue(routeTarget.toBlockPos());
                break;
            default:
                return new MiningState();
        }

        return this;
    }

    @Override
    public void onEnd(RouteMinerMacro macro) {
        log("Exiting Moving State");
    }

}