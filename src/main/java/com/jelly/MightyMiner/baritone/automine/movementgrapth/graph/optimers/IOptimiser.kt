package com.jelly.MightyMiner.baritone.automine.movementgrapth.graph.optimers

import com.jelly.MightyMiner.baritone.automine.movementgrapth.graph.node.GraphNode

fun interface IOptimiser {
    fun optimise(node: GraphNode): GraphNode
}