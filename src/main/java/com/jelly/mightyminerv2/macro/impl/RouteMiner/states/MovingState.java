package com.jelly.mightyminerv2.macro.impl.RouteMiner.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.Pathfinder;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.handler.RouteHandler;
import com.jelly.mightyminerv2.macro.impl.RouteMiner.RouteMinerMacro;
import com.jelly.mightyminerv2.util.*;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.route.Route;
import com.jelly.mightyminerv2.util.helper.route.RouteWaypoint;
import com.jelly.mightyminerv2.util.helper.route.WaypointType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.List;

/**
 * Moving State is responsible for navigating to the next waypoint
 * and handling the logic for the Route Miner Macro.
 * TODO: Use RouteNavigator (temporary fix because etherwarp doesn't work properly in RouteNavigator?)
 */
public class MovingState implements RouteMinerMacroState {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private RouteWaypoint routeTarget;

    private final Clock etherWarpDelay = new Clock();
    private boolean rotating = true;
    private boolean hasClicked = false;
    private BlockPos lastPosition = null;

    private boolean isWalking = false;

    @Override
    public void onStart(RouteMinerMacro macro) {
        log("Entering Moving State");
        Route route = RouteHandler.getInstance().getSelectedRoute();
        routeTarget = route.get(macro.getRouteIndex() + 1);

        if (routeTarget.getTransportMethod() == WaypointType.ETHERWARP) {
            InventoryUtil.holdItem("Aspect of the Void");
            KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, true);
            Vec3 point = getPoint(routeTarget);

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
                if (rotating) {
                    if (!RotationHandler.getInstance().isEnabled()) {
                        rotating = false;
                    }

                    return this;
                }

                if (!hasClicked) {
                    lastPosition = mc.thePlayer.getPosition();
                    KeyBindUtil.rightClick();
                    etherWarpDelay.schedule(250);
                    hasClicked = true;
                    return this;
                }

                if (lastPosition != null && etherWarpDelay.passed()) {
                    Route route = RouteHandler.getInstance().getSelectedRoute();
                    RouteWaypoint nearestWaypoint = nearestWaypoint(route);

                    if (
                        lastPosition.equals(mc.thePlayer.getPosition())
                        || nearestWaypoint != routeTarget
                    ) {
                        return new MovingState();
                    }

                    macro.setRouteIndex(macro.getRouteIndex() + 1);
                    return new MovingState();
                }

                return this;
            case WALK:
                if (isWalking) {
                    if (
                        Pathfinder.getInstance().completedPathTo(routeTarget.toBlockPos()) ||
                        (!Pathfinder.getInstance().isRunning() && Pathfinder.getInstance().succeeded()) ||
                        PlayerUtil.getBlockStandingOn().equals(routeTarget.toBlockPos())
                    ) {
                        macro.setRouteIndex(macro.getRouteIndex() + 1);
                        return new MovingState();
                    }

                    if (Pathfinder.getInstance().failed()) {
                        macro.disable("Pathfinding failed");
                        return null;
                    }

                    return this;
                }

                Pathfinder.getInstance().queue(routeTarget.toBlockPos());
                Pathfinder.getInstance().start();
                isWalking = true;

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

    private RouteWaypoint nearestWaypoint(Route route) {
        int size = route.size();
        BlockPos playerPos = mc.thePlayer.getPosition();
        RouteWaypoint closestWaypoint = route.get(0);

        for (int i = 1; i < size; i++) {
            RouteWaypoint waypoint = route.get(i);

            if (
                waypoint.getTransportMethod() != WaypointType.MINE &&
                playerPos.distanceSq(waypoint.toBlockPos()) < playerPos.distanceSq(closestWaypoint.toBlockPos())
            ) {
                closestWaypoint = waypoint;
            }
        }

        return closestWaypoint;
    }

    private Vec3 getPoint(RouteWaypoint routeTarget) {
        List<Vec3> points = BlockUtil.bestPointsOnBestSide(routeTarget.toBlockPos());
        Vec3 point = routeTarget.toVec3().addVector(0.5, 0.5, 0.5);

        if (!points.isEmpty()) {
            point = points.get(0);
        }

        return point;
    }

}