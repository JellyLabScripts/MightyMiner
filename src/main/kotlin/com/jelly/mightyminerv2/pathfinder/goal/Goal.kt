package com.jelly.mightyminerv2.pathfinder.goal

import com.jelly.mightyminerv2.pathfinder.movement.CalculationContext
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

class Goal(val goalX: Int, val goalY: Int, val goalZ: Int, val ctx: CalculationContext) : IGoal {
    private val SQRT_2 = sqrt(2.0)

    override fun isAtGoal(x: Int, y: Int, z: Int): Boolean {
        return goalX == x && goalY == y && goalZ == z
    }

    override fun heuristic(x: Int, y: Int, z: Int): Double {
        val dx = abs(goalX - x)
        val dz = abs(goalZ - z)
        val straight = abs(dx - dz).toDouble()
        var vertical = abs(goalY - y).toDouble()
        val diagonal = min(dx, dz).toDouble()

        if (goalY > y) {
            vertical *= 6.234399666206506
//            vertical *= ctx.cost.JUMP_ONE_BLOCK_COST
        } else {
            vertical *= ctx.cost.N_BLOCK_FALL_COST[2] / 2.0
        }

        return (straight + diagonal * SQRT_2) * ctx.cost.ONE_BLOCK_SPRINT_COST + vertical
    }

    override fun toString(): String {
        return "x: $goalX, y: $goalY, z: $goalZ"
    }
}