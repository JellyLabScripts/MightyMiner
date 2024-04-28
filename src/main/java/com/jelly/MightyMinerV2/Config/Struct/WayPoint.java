package com.jelly.MightyMinerV2.Config.Struct;

import lombok.Data;

import java.awt.*;

@Data
public class WayPoint {
    private final Color WayPointColor;
    private final String name;
    private final int x;
    private final int y;
    private final int z;
    private final int pos_x;
    private final int pos_y;
    private final int pos_z;

    public WayPoint(Color wayPointColor, String name, int x, int y, int z, int pos_x, int pos_y, int pos_z) {
        WayPointColor = wayPointColor;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pos_x = pos_x;
        this.pos_y = pos_y;
        this.pos_z = pos_z;
    }
}
