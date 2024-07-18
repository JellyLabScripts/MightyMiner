package com.jelly.MightyMinerV2.Pathfinder

import net.minecraft.util.BlockPos
import net.minecraft.world.World
import java.util.*
import com.jelly.MightyMinerV2.Pathfinder.Node;

class Pathfinding {

    fun findPath(world: World, start: BlockPos, end: BlockPos): List<BlockPos>? {
        val openList = PriorityQueue<Node>(compareBy { it.fCost })
        val closedList = mutableSetOf<BlockPos>()

        val startNode = Node(start, null, 0.0, getHeuristic(start, end))
        openList.add(startNode)

        while (openList.isNotEmpty()) {
            val currentNode = openList.poll()
            if (currentNode.pos == end) {
                return reconstructPath(currentNode)
            }

            closedList.add(currentNode.pos)

            for (neighborPos in getNeighbors(currentNode.pos)) {
                if (neighborPos in closedList) continue

                val tentativeGCost = currentNode.gCost + getMovementCost(currentNode.pos, neighborPos)
                val neighborNode = Node(neighborPos, currentNode, tentativeGCost, getHeuristic(neighborPos, end))

                openList.add(neighborNode)
            }
        }

        return null // No path found
    }

    private fun reconstructPath(endNode: Node): List<BlockPos> {
        val path = mutableListOf<BlockPos>()
        var currentNode: Node? = endNode
        while (currentNode != null) {
            path.add(currentNode.pos)
            currentNode = currentNode.parent
        }
        return path.reversed()
    }

    private fun getNeighbors(pos: BlockPos): List<BlockPos> {
        // Add logic to return valid neighboring positions (e.g., checking for walkable blocks)
        return listOf(
            pos.north(), pos.south(), pos.east(), pos.west(),
            pos.up(), pos.down()
        )
    }

    private fun getMovementCost(from: BlockPos, to: BlockPos): Double {
        // Define movement cost, typically 1 for adjacent blocks
        return 1.0
    }

    private fun getHeuristic(start: BlockPos, end: BlockPos): Double {
        return start.distanceSq(end).toDouble()
    }
}