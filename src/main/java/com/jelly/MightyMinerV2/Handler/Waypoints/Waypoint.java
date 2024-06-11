package com.jelly.MightyMinerV2.Handler.Waypoints;

import lombok.Getter;
import net.minecraft.util.Vec3;

public class Waypoint {
    @Getter
    public final Vec3 position;
    @Getter
    public final String name;

    public Waypoint(Vec3 position, String name) {
        this.position = position;
        this.name = name;
    }
}