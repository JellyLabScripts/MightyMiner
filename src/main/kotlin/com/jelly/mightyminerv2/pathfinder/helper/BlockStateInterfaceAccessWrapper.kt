package com.jelly.mightyminerv2.pathfinder.helper

import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.world.IBlockAccess
import net.minecraft.world.WorldType
import net.minecraft.world.biome.BiomeGenBase

class BlockStateInterfaceAccessWrapper(private val bsi: BlockStateAccessor, private val world: IBlockAccess) :
    IBlockAccess {

    override fun getTileEntity(pos: BlockPos): TileEntity? {
        return null
    }

    override fun getCombinedLight(pos: BlockPos, lightValue: Int): Int {
        return 0
    }

    override fun getBlockState(pos: BlockPos): IBlockState {
        // BlockStateInterface#get0(BlockPos) btfo!
        return bsi.get(pos.x, pos.y, pos.z)
    }

    override fun isAirBlock(pos: BlockPos): Boolean {
        return bsi.get(pos.x, pos.y, pos.z).block == Blocks.air
    }

    override fun getBiomeGenForCoords(pos: BlockPos): BiomeGenBase? {
        return null
    }

    override fun extendedLevelsInChunkCache(): Boolean {
        return false
    }

    // Uncomment and implement if needed
    // override fun getBiome(pos: BlockPos): Biome {
    //     return Biomes.FOREST
    // }

    override fun getStrongPower(pos: BlockPos, direction: EnumFacing): Int {
        return 0
    }

    override fun getWorldType(): WorldType {
        return world.worldType
    }

    override fun isSideSolid(pos: BlockPos, side: EnumFacing, _default: Boolean): Boolean {
        return false
    }
}
