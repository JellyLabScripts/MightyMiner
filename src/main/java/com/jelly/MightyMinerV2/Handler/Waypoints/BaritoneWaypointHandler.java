package com.jelly.MightyMinerV2.Handler.Waypoints;

import net.minecraft.util.Vec3;
import java.util.*;

public class BaritoneWaypointHandler {
    private final Map<String, Graph> graphs = new HashMap<>();

    public BaritoneWaypointHandler() {
        // Create and initialize the first graph
        Graph graph1 = new Graph();
        Waypoint[] waypoints1 = {
                new Waypoint(new Vec3(80, 72, 235), "Start"),
                new Waypoint(new Vec3(92, 70, 221), "Middle"),
                new Waypoint(new Vec3(116, 71, 218), "Middle2"),
                new Waypoint(new Vec3(85, 70, 208), "End")
        };
        for (Waypoint waypoint : waypoints1) {
            graph1.addWaypoint(waypoint);
        }
        graph1.addEdge(waypoints1[0], waypoints1[1]);
        graph1.addEdge(waypoints1[1], waypoints1[2]);
        graph1.addEdge(waypoints1[2], waypoints1[3]);

        // Add the first graph to the map of graphs
        graphs.put("Example", graph1);

        // Create and initialize the second graph
        Graph graph2 = new Graph();
        Waypoint[] waypoints2 = {
                new Waypoint(new Vec3(-48.5, 200, -121.5), "Dwarven_Village_1"),
                new Waypoint(new Vec3(6.5, 200, -121.5), "Dwarven_Village_2"),
                new Waypoint(new Vec3(78.5, 198, -96.5), "Dwarven_Village_3"),
                new Waypoint(new Vec3(64.5, 190, -54.5), "Dwarven_Village_4"),
                new Waypoint(new Vec3(-24.5, 175, -54.5), "Ramparts_Quarry_1"),
                new Waypoint(new Vec3(-48.5, 167, -49.5), "Ramparts_Quarry_2"),
                new Waypoint(new Vec3(-63.5, 162, -38.5), "Ramparts_Quarry_3"),
                new Waypoint(new Vec3(-81.5, 156, -38.5), "Ramparts_Quarry_4"),
                new Waypoint(new Vec3(-105.5, 150, -17.5), "Ramparts_Quarry_5"),
                new Waypoint(new Vec3(-105.5, 148, 3.5), "Ramparts_Quarry_6"),
                new Waypoint(new Vec3(-60.5, 139, 12.5), "Ramparts_Quarry_7"),
                new Waypoint(new Vec3(-54.5, 138, 12.5), "Ramparts_Quarry_8"),
                new Waypoint(new Vec3(-40.5, 134, 24.5), "Ramparts_Quarry_9"),
                new Waypoint(new Vec3(-20.5, 128, 43.5), "Cliffside_Veins_1"),
                new Waypoint(new Vec3(0.5, 128, 53.5), "Cliffside_Veins_2"),
                new Waypoint(new Vec3(13.5, 128, 47.5), "Cliffside_Veins_3"),
                new Waypoint(new Vec3(40.5, 128, 40.5), "Cliffside_Veins_4"),
                new Waypoint(new Vec3(66.5, 128, 34.5), "Cliffside_Veins_5"),
                new Waypoint(new Vec3(72.5, 137, 37.5), "Cliffside_Veins_6"),
                new Waypoint(new Vec3(81.5, 141, 39.5), "Cliffside_Veins_7"),
                new Waypoint(new Vec3(93.5, 147, 38.5), "Cliffside_Veins_8"),
                new Waypoint(new Vec3(110.5, 154, 42.5), "Royal_Mines_1"),
                new Waypoint(new Vec3(127.5, 155, 31.5), "Royal_Mines_2"),
                new Waypoint(new Vec3(145.5, 152, 34.5), "Royal_Mines_3"),
                new Waypoint(new Vec3(170.5, 150, 35.5), "Royal_Mines_4"),
                new Waypoint(new Vec3(0.5, 128, 144.5), "Great_Ice_Wall_1"),
                new Waypoint(new Vec3(-37.5, 138, -7.5), "Forge_1"),
                new Waypoint(new Vec3(-17.5, 145, -17.5), "Forge_2"),
                new Waypoint(new Vec3(-1.5, 147, -28.5), "Forge_3"),
                new Waypoint(new Vec3(2.5, 147, -28.5), "Forge_4"),
                new Waypoint(new Vec3(14.5, 145, -10.5), "Forge_5"),
                new Waypoint(new Vec3(23.5, 144, -3.5), "Forge_6"),
                new Waypoint(new Vec3(39.5, 136, 15.5), "Forge_7"),
                new Waypoint(new Vec3(58.5, 136, 27.5), "Forge_8"),
                new Waypoint(new Vec3(-96.5, 161, -58.5), "Upper_Mines_1"),
                new Waypoint(new Vec3(-109.5, 167, -68.5), "Upper_Mines_2"),
                new Waypoint(new Vec3(-119.5, 171, -71.5), "Upper_Mines_3"),
                new Waypoint(new Vec3(-31.5, 173, -63.5), "Upper_Mines_4"),
                new Waypoint(new Vec3(-129.5, 174, -47.5), "Upper_Mines_5"),
                new Waypoint(new Vec3(-114.5, 181, -61.5), "Upper_Mines_6"),
                new Waypoint(new Vec3(-104.5, 186, -61.5), "Upper_Mines_7"),
                new Waypoint(new Vec3(-99.5, 188, -54.5), "Upper_Mines_8"),
                new Waypoint(new Vec3(-108.5, 194, -42.5), "Upper_Mines_9"),
                new Waypoint(new Vec3(-124.5, 200, -34.5), "Upper_Mines_10"),
                new Waypoint(new Vec3(-122.5, 203, -46.5), "Upper_Mines_11"),
                new Waypoint(new Vec3(-102.5, 206, -61.5), "Upper_Mines_12")
        };

        // Adding the waypoints to the second graph
        for (Waypoint waypoint : waypoints2) {
            graph2.addWaypoint(waypoint);
        }

        // Adding the edges to the second graph
        graph2.addEdge(waypoints2[0], waypoints2[1]);
        graph2.addEdge(waypoints2[1], waypoints2[2]);
        graph2.addEdge(waypoints2[2], waypoints2[3]);
        graph2.addEdge(waypoints2[3], waypoints2[4]);
        graph2.addEdge(waypoints2[4], waypoints2[5]);
        graph2.addEdge(waypoints2[5], waypoints2[6]);
        graph2.addEdge(waypoints2[6], waypoints2[7]);
        graph2.addEdge(waypoints2[7], waypoints2[8]);
        graph2.addEdge(waypoints2[8], waypoints2[9]);
        graph2.addEdge(waypoints2[9], waypoints2[10]);
        graph2.addEdge(waypoints2[10], waypoints2[11]);
        graph2.addEdge(waypoints2[11], waypoints2[12]);
        graph2.addEdge(waypoints2[12], waypoints2[13]);
        graph2.addEdge(waypoints2[13], waypoints2[14]);
        graph2.addEdge(waypoints2[14], waypoints2[15]);
        graph2.addEdge(waypoints2[15], waypoints2[16]);
        graph2.addEdge(waypoints2[16], waypoints2[17]);
        graph2.addEdge(waypoints2[17], waypoints2[18]);
        graph2.addEdge(waypoints2[18], waypoints2[19]);
        graph2.addEdge(waypoints2[19], waypoints2[20]);
        graph2.addEdge(waypoints2[20], waypoints2[21]);
        graph2.addEdge(waypoints2[21], waypoints2[22]);
        graph2.addEdge(waypoints2[22], waypoints2[23]);
        graph2.addEdge(waypoints2[23], waypoints2[24]);
        //Upper mine
        graph2.addEdge(waypoints2[7], waypoints2[34]);
        graph2.addEdge(waypoints2[34], waypoints2[35]);
        graph2.addEdge(waypoints2[35], waypoints2[36]);
        graph2.addEdge(waypoints2[36], waypoints2[37]);
        graph2.addEdge(waypoints2[37], waypoints2[38]);
        graph2.addEdge(waypoints2[38], waypoints2[39]);
        graph2.addEdge(waypoints2[39], waypoints2[40]);
        graph2.addEdge(waypoints2[40], waypoints2[41]);
        graph2.addEdge(waypoints2[41], waypoints2[42]);
        graph2.addEdge(waypoints2[42], waypoints2[43]);
        graph2.addEdge(waypoints2[43], waypoints2[44]);
        graph2.addEdge(waypoints2[44], waypoints2[45]);

        //Ice wall
        graph2.addEdge(waypoints2[14], waypoints2[25]);
        //Forge
        graph2.addEdge(waypoints2[11], waypoints2[26]);
        graph2.addEdge(waypoints2[26], waypoints2[27]);
        graph2.addEdge(waypoints2[27], waypoints2[28]);
        graph2.addEdge(waypoints2[28], waypoints2[29]);
        graph2.addEdge(waypoints2[29], waypoints2[30]);
        graph2.addEdge(waypoints2[30], waypoints2[31]);
        graph2.addEdge(waypoints2[31], waypoints2[32]);
        graph2.addEdge(waypoints2[32], waypoints2[33]);
        graph2.addEdge(waypoints2[33], waypoints2[18]);
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
