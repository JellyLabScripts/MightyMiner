package com.jelly.MightyMinerV2.Handler.Waypoints;

import lombok.Getter;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    @Getter
    private final List<Waypoint> waypoints = new ArrayList<>();
    private final Map<Waypoint, List<Edge>> edges = new HashMap<>();

    public void addWaypoint(Waypoint waypoint) {
        waypoints.add(waypoint);
        edges.put(waypoint, new ArrayList<>());
    }

    public void addEdge(Waypoint source, Waypoint target) {
        double weight = source.getPosition().distanceTo(target.getPosition());
        Edge edge = new Edge(source, target, weight);
        edges.get(source).add(edge);
        edges.get(target).add(new Edge(target, source, weight)); // add the reverse edge as well
    }

    public List<Edge> getEdges(Waypoint source) {
        return edges.get(source);
    }
}
