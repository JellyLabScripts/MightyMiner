package com.jelly.mightyminerv2.pathfinder.movement.movements

import com.jelly.mightyminerv2.MightyMiner
import com.jelly.mightyminerv2.pathfinder.movement.CalculationContext
import com.jelly.mightyminerv2.pathfinder.movement.Movement
import com.jelly.mightyminerv2.pathfinder.movement.MovementResult
import net.minecraft.util.BlockPos

class MovementFall(mm: MightyMiner, source: BlockPos, dest: BlockPos) : Movement(mm, source, dest) {
    override fun calculateCost(ctx: CalculationContext, res: MovementResult) {
        calculateCost(ctx, source.x, source.y, source.z, dest.x, dest.z, res)
        costs = res.cost
    }

    companion object {
        fun calculateCost(
            ctx: CalculationContext,
            x: Int,
            y: Int,
            z: Int,
            destX: Int,
            destZ: Int,
            res: MovementResult
        ) {
            res.set(destX, y - 1, destZ)
            MovementDescend.calculateCost(ctx, x, y, z, destX, destZ, res)
        }
    }
}
