package com.jelly.mightyminerv2.pathfinder.calculate.path

import com.jelly.mightyminerv2.pathfinder.calculate.Path
import com.jelly.mightyminerv2.pathfinder.calculate.PathNode
import com.jelly.mightyminerv2.pathfinder.calculate.openset.BinaryHeapOpenSet
import com.jelly.mightyminerv2.pathfinder.goal.Goal
import com.jelly.mightyminerv2.pathfinder.movement.CalculationContext
import com.jelly.mightyminerv2.pathfinder.movement.MovementResult
import com.jelly.mightyminerv2.pathfinder.movement.Moves
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap

class AStarPathFinder(val startX: Int, val startY: Int, val startZ: Int, val goal: Goal, val ctx: CalculationContext) {
    private val closedSet: Long2ObjectMap<PathNode> = Long2ObjectOpenHashMap()
    private var calculating = false

    fun calculatePath(): Path? {
        calculating = true
        val openSet = BinaryHeapOpenSet()
        val startNode = PathNode(startX, startY, startZ, goal)
        val res = MovementResult()
        val moves = Moves.values()
        startNode.costSoFar = 0.0
        startNode.totalCost = startNode.costToEnd
        openSet.add(startNode)

        while (!openSet.isEmpty() && calculating) {
            val currentNode = openSet.poll()

            if (goal.isAtGoal(currentNode.x, currentNode.y, currentNode.z)) {
                return Path(startNode, currentNode, goal, ctx)
            }

            for (move in moves) {
                res.reset()
                move.calculate(ctx, currentNode.x, currentNode.y, currentNode.z, res)
                val cost = res.cost
                if (cost >= ctx.cost.INF_COST) continue
                val neighbourNode =
                    getNode(res.x, res.y, res.z, PathNode.longHash(res.x, res.y, res.z))
                val neighbourCostSoFar = currentNode.costSoFar + cost

                if (neighbourNode.costSoFar > neighbourCostSoFar) {
                    neighbourNode.parentNode = currentNode
                    neighbourNode.costSoFar = neighbourCostSoFar
                    neighbourNode.totalCost = neighbourCostSoFar + neighbourNode.costToEnd

                    if (neighbourNode.heapPosition == -1) {
                        openSet.add(neighbourNode)
                    } else {
                        openSet.relocate(neighbourNode)
                    }
                }
            }
        }
        calculating = false
        return null
    }

    fun getNode(x: Int, y: Int, z: Int, hash: Long): PathNode {
        var n: PathNode? = closedSet.get(hash)
        if (n == null) {
            n = PathNode(x, y, z, goal)
            closedSet.put(hash, n)
        }
        return n
    }

    fun requestStop() {
        if (!calculating) return
        calculating = false
    }
}