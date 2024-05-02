package com.jelly.MightyMinerV2.Util.helper.route;

import com.google.gson.annotations.Expose;
import com.jelly.MightyMinerV2.Config.MightyMinerConfig;
import com.jelly.MightyMinerV2.Util.RenderUtil;
import net.minecraft.util.Vec3;

import java.util.LinkedList;
import java.util.List;

public class Route {
    @Expose
    private final List<RouteWaypoint> waypoints = new LinkedList<>();

    public void insert(final RouteWaypoint waypoint) {
        this.insert(this.waypoints.size(), waypoint);
    }

    public void insert(final int index, final RouteWaypoint waypoint) {
        waypoint.routeIndex = index;
        this.waypoints.add(index, waypoint);
        this.updateIndex(index + 1, this.waypoints.size(), 1);
    }

    public void remove(final RouteWaypoint waypoint) {
        final int index = this.waypoints.indexOf(waypoint);
        if (index == -1) return;
        this.remove(index);
    }

    public void remove(final int index) {
        this.waypoints.remove(index);
        this.updateIndex(index, this.waypoints.size(), -1);
    }

    public RouteWaypoint get(final int index) {
        return this.waypoints.get((index + this.waypoints.size()) % this.waypoints.size());
    }

    public int indexOf(final RouteWaypoint waypoint) {
        return this.waypoints.indexOf(waypoint);
    }

    public void replace(final int index, final RouteWaypoint waypoint) {
        waypoint.routeIndex = index;
        this.waypoints.set(index, waypoint);
    }

    public boolean isEnd(final int index) {
        return index - 1 == this.waypoints.size();
    }

    private void updateIndex(final int startIndex, final int endIndex, final int amount) {
        for (int i = startIndex; i < endIndex; i++) {
            this.waypoints.get(i).routeIndex += amount;
        }
    }

    public void drawRoute() {
        for (int i = 0; i < this.waypoints.size(); i++) {
            RouteWaypoint currWaypoint = this.get(i);
            RouteWaypoint prevWaypoint = this.get(i - 1);
            currWaypoint.draw();
            if (this.waypoints.size() == 1) continue;
            RenderUtil.drawTracer(
                    new Vec3(prevWaypoint.getX() + 0.5, prevWaypoint.getY() + 0.5, prevWaypoint.getZ() + 0.5),
                    new Vec3(currWaypoint.getX() + 0.5, currWaypoint.getY() + 0.5, currWaypoint.getZ() + 0.5),
                    MightyMinerConfig.routeBuilderTracerColor.toJavaColor()
            );
        }
    }
}
