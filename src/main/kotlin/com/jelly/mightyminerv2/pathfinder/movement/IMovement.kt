package com.jelly.mightyminerv2.pathfinder.movement

import com.jelly.mightyminerv2.MightyMiner
import net.minecraft.util.BlockPos

interface IMovement {
    val mm: MightyMiner
    val source: BlockPos
    val dest: BlockPos
    val costs: Double // plural cuz kotlin gae

    fun getCost(): Double
    fun calculateCost(ctx: CalculationContext, res: MovementResult)
}