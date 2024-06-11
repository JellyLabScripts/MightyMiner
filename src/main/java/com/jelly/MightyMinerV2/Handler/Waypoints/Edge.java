package com.jelly.MightyMinerV2.Handler.Waypoints;

import lombok.Getter;

public class Edge {
    @Getter
    public final Waypoint source;
    @Getter
    public final Waypoint target;
    @Getter
    public final double weight;

    public Edge(Waypoint source, Waypoint target, double weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }
}