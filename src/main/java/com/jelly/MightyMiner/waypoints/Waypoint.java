package com.jelly.MightyMiner.waypoints;

import lombok.Data;
import net.minecraft.util.BlockPos;

import java.awt.*;

@Data
public class Waypoint {
    private final Color waypointColor;
    private final String name;
    private final int x;
    private final int y;
    private final int z;

    private final String dimension;
}
