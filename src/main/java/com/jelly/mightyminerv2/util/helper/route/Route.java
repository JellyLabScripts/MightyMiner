package com.jelly.mightyminerv2.util.helper.route;

import com.google.gson.annotations.Expose;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.util.RenderUtil;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Route {

    @Expose
    private final List<RouteWaypoint> waypoints = new ArrayList<>();

    public Route() {
    }

    public Route(List<RouteWaypoint> routes) {
        this.waypoints.addAll(routes);
    }

    public void insert(final RouteWaypoint waypoint) {
        this.insert(this.waypoints.size(), waypoint);
    }

    public void insert(final int index, final RouteWaypoint waypoint) {
        this.waypoints.add(index, waypoint);
    }

    public void remove(final RouteWaypoint waypoint) {
        final int index = this.waypoints.indexOf(waypoint);
        if (index == -1) {
            return;
        }
        this.remove(index);
    }

    public void remove(final int index) {
        this.waypoints.remove(index);
    }

    public RouteWaypoint get(final int index) {
        return this.waypoints.get((index + this.waypoints.size()) % this.waypoints.size());
    }

    public Optional<RouteWaypoint> getClosest(final BlockPos pos) {
        return this.waypoints.stream().min(Comparator.comparingDouble(wp -> wp.toVec3().squareDistanceTo(new Vec3(pos))));
    }

    public int indexOf(final RouteWaypoint waypoint) {
        return this.waypoints.indexOf(waypoint);
    }

    public void replace(final int index, final RouteWaypoint waypoint) {
        this.waypoints.set(index, waypoint);
    }

    public boolean isEnd(final int index) {
        return index + 1 == this.waypoints.size();
    }

    public boolean isEmpty() {
        return this.waypoints.isEmpty();
    }

    public void drawRoute() {
        for (int i = 0; i < this.waypoints.size(); i++) {
            RouteWaypoint currWaypoint = this.get(i);
            String name = currWaypoint.getTransportMethod().name();
            RenderUtil.drawBlock(currWaypoint.toBlockPos(), MightyMinerConfig.routeBuilderNodeColor.toJavaColor());
            RenderUtil.drawText((i + 1) + ". " + name.charAt(0) + name.substring(1).toLowerCase(), currWaypoint.getX() + 0.5, currWaypoint.getY() + 1, currWaypoint.getZ() + 0.5, 0.8F);
            if (this.waypoints.size() == 1) {
                continue;
            }
            RouteWaypoint prevWaypoint = this.get(i - 1);
            RenderUtil.drawLine(
                    prevWaypoint.toVec3().addVector(0.5, 0.5, 0.5),
                    currWaypoint.toVec3().addVector(0.5, 0.5, 0.5),
                    MightyMinerConfig.routeBuilderTracerColor.toJavaColor()
            );
        }
    }

    public int size() {
        return this.waypoints.size();
    }
}
