package com.jelly.mightyminerv2.pathfinder.movement

import net.minecraft.util.BlockPos

class MovementResult {
    var x: Int = 0
    var y: Int = 0
    var z: Int = 0
    var cost: Double = 1e6

    fun set(x: Int, y: Int, z: Int) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun reset() {
        x = 0
        y = 0
        z = 0
        cost = 1e6
    }

    fun getDest(): BlockPos {
        return BlockPos(x, y, z)
    }
}
