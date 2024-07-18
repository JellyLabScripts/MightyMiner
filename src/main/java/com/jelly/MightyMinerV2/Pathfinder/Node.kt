package com.jelly.MightyMinerV2.Pathfinder

import net.minecraft.util.BlockPos

data class Node(
    val pos: BlockPos,
    val parent: Node?,
    val gCost: Double, // Cost from start to this node
    val hCost: Double  // Heuristic cost to the end node
) {
    val fCost: Double get() = gCost + hCost
}

