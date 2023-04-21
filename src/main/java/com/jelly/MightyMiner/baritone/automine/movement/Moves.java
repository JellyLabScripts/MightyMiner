package com.jelly.MightyMiner.baritone.automine.movement;
// inspired by @leijurv
public enum Moves {

    TRANSVERSE_NORTH(0, 0, -1, 1),
    TRANSVERSE_SOUTH(0, 0, 1, 1),
    TRANSVERSE_WEST(-1, 0, 0, 1),
    TRANSVERSE_EAST(1, 0, 0, 1),

    ASCEND_NORTH(0, 1, -1, 2),
    ASCEND_SOUTH(0, 1, 1, 2),
    ASCEND_WEST(-1, 1, 0, 2),
    ASCEND_EAST(1, 1, 0, 2),

    DESCEND_NORTH(0, -1, -1, 2),
    DESCEND_SOUTH(0, -1, 1, 2),
    DESCEND_WEST(-1, -1, 0, 2),
    DESCEND_EAST(1, -1, 0, 2),

    DIAGONAL_NORTHEAST(1, 0, -1, 1.4d),
    DIAGONAL_SOUTHEAST(1, 0, 1, 1.4d),
    DIAGONAL_SOUTHWEST(-1, 0, 1, 1.4d),
    DIAGONAL_NORTHWEST(-1, 0, -1, 1.4d),

    DOWN(0, -1, 0, 1);



    public final int dx;
    public final int dy;
    public final int dz;
    public final double cost;

    Moves(int dx, int dy, int dz, double cost) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
        this.cost = cost;
    }


}
