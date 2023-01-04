package com.jelly.MightyMiner.baritone.automine.movementgrapth.graph.optimers

import com.jelly.MightyMiner.baritone.automine.movementgrapth.graph.node.GraphNode

class OptimiserGraph {
    private val optimisers: MutableList<IOptimiser> = mutableListOf()

    fun addOptimiser(optimiser: IOptimiser) {
        optimisers.add(optimiser)
    }

    fun optimise(node: GraphNode): GraphNode {
        var currentNode = node
        for (optimiser in optimisers) {
            currentNode = optimiser.optimise(currentNode)
        }
        return currentNode
    }
}