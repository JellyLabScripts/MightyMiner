package com.jelly.MightyMinerV2.Handler.Waypoints;

import net.minecraft.util.Vec3;
import java.util.HashMap;
import java.util.*;

public class BaritoneWaypointHandler {
    private final Map<String, Graph> graphs = new HashMap<>();

    public BaritoneWaypointHandler() {
        // Create a new graph for the first route
        Graph graph1 = new Graph();
        Waypoint waypoint1 = new Waypoint(new Vec3(80, 72, 235), "Start");
        Waypoint waypoint2 = new Waypoint(new Vec3(92, 70, 221), "Middle");
        Waypoint waypoint3 = new Waypoint(new Vec3(116, 71, 218), "Middle2");
        Waypoint waypoint8 = new Waypoint(new Vec3(85, 70, 208), "End");
        graph1.addWaypoint(waypoint1);
        graph1.addWaypoint(waypoint2);
        graph1.addWaypoint(waypoint3);
        graph1.addWaypoint(waypoint8);
        graph1.addEdge(waypoint1, waypoint2);
        graph1.addEdge(waypoint2, waypoint3);
        graph1.addEdge(waypoint3, waypoint8);

        // Add the first graph to the map of graphs
        graphs.put("Example", graph1);

        // Create a new graph for the second route
        Graph graph2 = new Graph();
        Waypoint waypoint4 = new Waypoint(new Vec3(20, 0, 0), "Right");
        Waypoint waypoint5 = new Waypoint(new Vec3(30, 40, 0), "Right2");
        Waypoint waypoint6 = new Waypoint(new Vec3(21, 10, 0), "Left");
        Waypoint waypoint7 = new Waypoint(new Vec3(20, 0, 34), "Left2");
        graph2.addWaypoint(waypoint4);
        graph2.addWaypoint(waypoint5);
        graph2.addWaypoint(waypoint6);
        graph2.addWaypoint(waypoint7);
        graph2.addEdge(waypoint4, waypoint5);
        graph2.addEdge(waypoint6, waypoint7);
        graph2.addEdge(waypoint7, waypoint5);

        // Add the second graph to the map of graphs
        graphs.put("walking", graph2);
    }

    public Graph getGraph(String name) {
        return graphs.get(name);
    }
    public Set<String> getGraphNames() {
        return graphs.keySet();
    }
}
