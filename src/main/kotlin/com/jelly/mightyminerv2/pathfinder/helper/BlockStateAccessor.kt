package com.jelly.mightyminerv2.pathfinder.helper

import com.jelly.mightyminerv2.util.IChunkProviderClient
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.world.IBlockAccess
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk

class BlockStateAccessor(private val world: World) {
    private val loadedChunks: Long2ObjectMap<Chunk> = Long2ObjectOpenHashMap()
    private var cached: Chunk? = null
    var access: IBlockAccess
    val isPassableBlockPos: BlockPos.MutableBlockPos

    init {
        val loadedWorld: List<Chunk> = (this.world.chunkProvider as IChunkProviderClient).chunkListing()

        for (chunk in loadedWorld) {
            this.loadedChunks[this.getKey(chunk.xPosition, chunk.zPosition)] = chunk
        }

        isPassableBlockPos = BlockPos.MutableBlockPos()
        access = BlockStateInterfaceAccessWrapper(this, world)
    }

    fun get(x: Int, y: Int, z: Int): IBlockState {
        var current = this.cached
        if (current != null && current.xPosition == x shr 4 && current.zPosition == z shr 4) {
            return current.getBlockState(BlockPos(x, y, z))
        }

        current = this.loadedChunks[this.getKey(x shr 4, z shr 4)]

        if (current != null && current.isLoaded) {
            this.cached = current
            return current.getBlockState(BlockPos(x, y, z))
        }
        return Blocks.air.defaultState
    }

    fun isBlockInLoadedChunks(blockX: Int, blockZ: Int): Boolean {
        return this.loadedChunks.containsKey(getKey(blockX shr 4, blockZ shr 4))
    }

    private fun getKey(x: Int, z: Int): Long {
        return (x.toLong() and 4294967295L) or ((z.toLong() and 4294967295L) shl 32)
    }
}
