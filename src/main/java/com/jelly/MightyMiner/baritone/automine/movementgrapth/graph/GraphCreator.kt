package com.jelly.MightyMiner.baritone.automine.movementgrapth.graph

import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathMode
import com.jelly.MightyMiner.baritone.automine.movementgrapth.debug.Visualiser
import com.jelly.MightyMiner.baritone.automine.movementgrapth.graph.node.GraphNode
import com.jelly.MightyMiner.baritone.automine.movementgrapth.graph.node.impl.EndNode
import com.jelly.MightyMiner.baritone.automine.movementgrapth.graph.node.impl.HeadRotateNode
import com.jelly.MightyMiner.baritone.automine.movementgrapth.graph.node.impl.MoveNode
import com.jelly.MightyMiner.baritone.automine.movementgrapth.graph.node.impl.RootNode
import com.jelly.MightyMiner.baritone.automine.structures.Path
import com.jelly.MightyMiner.utils.MathUtils
import com.jelly.MightyMiner.utils.PlayerUtils
import org.joml.Vector3i
import kotlin.math.atan2
import kotlin.math.sqrt

class GraphCreator {

    /**
     * Creates a non-optimal graph of nodes representing a series of movements that will take the player along a given path.
     * [path] the path to follow
     * @return the root node of the resulting graph of nodes
     */
    private fun createGotoGraph(path: Path): GraphNode {
        // create root node
        var currentNode: GraphNode = RootNode()


        // for every (previous-current-next) block pos we create a non-optimal graph of moves,
        // if for example: the next block pos we need to travel to is to the left, we add a turn node with 90 degrees

        // in the first pass we only create move nodes
        for((index, blcPos) in path.blocksInPath.withIndex()){
            val prev = if(index == 0) null else path.blocksInPath[index - 1]
            val next = if(index == path.blocksInPath.size - 1) null else path.blocksInPath[index + 1]

            // start of the path
            // if there are is no previous block pos (start of path) we use the player pos
            val prevsBlockPos = if(prev == null){
                PlayerUtils.getPosAsVector3i()
            } else {
                MathUtils.BlockPosToVector3i(prev.pos)
            }

            currentNode = currentNode.createChild(MoveNode(prevsBlockPos, MathUtils.BlockPosToVector3i(blcPos.pos)))

        }

        currentNode = currentNode.getRoot()

        // in second pass we add turn nodes
        for(node in currentNode.getAllNodes()){
            val prev = node.parent as MoveNode
            val curr = node as MoveNode
            val next = node.children[0] as MoveNode

            val (yawChangeNeeded, pitchChangeNeeded) = calculateYawAndPitch(prev.to, curr.to, next.to)

            if(yawChangeNeeded == 0f && pitchChangeNeeded == 0f){
                continue
            }

            node.pushDown(HeadRotateNode(yawChangeNeeded, pitchChangeNeeded))

        }

        // finish with end node
        currentNode = currentNode.createChild(EndNode())

        return currentNode
    }

    fun calculateYawAndPitch(prev: Vector3i, curr: Vector3i, next: Vector3i): Pair<Float, Float> {
        val dx1 = curr.x - prev.x
        val dz1 = curr.z - prev.z
        val dy1 = curr.y - prev.y

        val dx2 = next.x - curr.x
        val dz2 = next.z - curr.z
        val dy2 = next.y - curr.y

        val yaw = Math.toDegrees(atan2(dz2.toDouble(), dx2.toDouble())).toFloat() - 90f
        val pitch = Math.toDegrees(atan2((dy2 + dy1).toDouble(), sqrt((dx1 * dx1 + dz1 * dz1).toDouble()))).toFloat()

        return Pair(yaw, pitch)
    }



    private fun createMineGraph(path: Path): GraphNode {
        TODO("Not yet implemented")
    }

    fun createGraph(path: Path): GraphNode {
        val graph = when(path.mode){
            PathMode.GOTO -> {
                createGotoGraph(path)
            }

            PathMode.MINE -> {
                createMineGraph(path)
            }

            else -> {
                throw Exception("Null path mode")
            }

        }

        Visualiser.update(graph)

        return graph
    }


}