package com.jelly.mightyminerv2.pathfinder.movement

import com.jelly.mightyminerv2.MightyMiner
import net.minecraft.util.BlockPos

abstract class Movement(override val mm: MightyMiner, override val source: BlockPos, override val dest: BlockPos) :
    IMovement {

    override var costs: Double = 1e6
    override fun getCost() = costs
}