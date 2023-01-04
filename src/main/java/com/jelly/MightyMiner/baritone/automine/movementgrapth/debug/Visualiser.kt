package com.jelly.MightyMiner.baritone.automine.movementgrapth.debug

import com.jelly.MightyMiner.MightyMiner
import com.jelly.MightyMiner.baritone.automine.movementgrapth.graph.node.GraphNode
import com.jelly.MightyMiner.baritone.automine.movementgrapth.graph.node.impl.MoveNode
import com.jelly.MightyMiner.config.Config
import com.jelly.MightyMiner.render.BlockRenderer
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object Visualiser {

    private val blockRenderer = BlockRenderer()

    private var expireAt = 0L

    fun update(node: GraphNode) {
        expireAt = System.currentTimeMillis() + 4000

        node.getRoot().getAllNodes()
            .filterIsInstance<MoveNode>()
            .forEach {
                blockRenderer.renderMap[BlockPos(it.to.x, it.to.y, it.to.z)] = Color.ORANGE
            }
    }

    @SubscribeEvent
    fun onRenderEvent(event: RenderWorldLastEvent?) {
        if (!MightyMiner.config.debugVisuliseGraph) return

        if (System.currentTimeMillis() >= expireAt) {
            blockRenderer.renderMap.clear()
            return
        }

        blockRenderer.renderAABB(event)
    }

}