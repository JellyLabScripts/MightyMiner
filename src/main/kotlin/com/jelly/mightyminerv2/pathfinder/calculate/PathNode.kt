package com.jelly.mightyminerv2.pathfinder.calculate

import com.jelly.mightyminerv2.pathfinder.goal.Goal
import net.minecraft.util.BlockPos

class PathNode(var x: Int, var y: Int, var z: Int, val goal: Goal) {
    var costSoFar: Double = 1e6                             // gCost - INF_COST
    var costToEnd: Double = goal.heuristic(x, y, z)         // hCost / Heuristic
    var totalCost: Double = 1.0                            // INF_COST
    var heapPosition = -1                                  // Smart
    var parentNode: PathNode? = null

    override fun equals(other: Any?): Boolean {
        val otter =
            other as PathNode         // otter just means other bt written weird to remove warning
        return otter.x == this.x && otter.y == this.y && otter.z == this.z
    }

    override fun hashCode(): Int {
        return longHash(this.x, this.y, this.z).toInt()
    }

    fun getBlock(): BlockPos {
        return BlockPos(x, y, z)
    }

    override fun toString(): String {
        return "PathNode(x: $x, y: $y, z: $z, costSoFar: $costSoFar, costToEnd: $costToEnd totalCost: $totalCost)"
    }

    companion object {
        fun longHash(x: Int, y: Int, z: Int): Long {
            var hash = 3241L
            hash = 3457689L * hash + x
            hash = 8734625L * hash + y
            hash = 2873465L * hash + z
            return hash
        }
    }
}