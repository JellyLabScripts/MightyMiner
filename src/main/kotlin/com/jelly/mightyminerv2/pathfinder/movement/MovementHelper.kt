package com.jelly.mightyminerv2.pathfinder.movement

import com.jelly.mightyminerv2.pathfinder.helper.BlockStateAccessor
import net.minecraft.block.*
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import kotlin.math.abs

object MovementHelper {

    fun canWalkThrough(
        bsa: BlockStateAccessor,
        x: Int,
        y: Int,
        z: Int,
        state: IBlockState = bsa.get(x, y, z)
    ): Boolean {
        val canWalk = canWalkThroughBlockState(state)
        if (canWalk != null) {
            return canWalk
        }
        return canWalkThroughPosition(bsa, x, y, z, state)
    }

    fun canWalkThroughBlockState(state: IBlockState): Boolean? {
        val block = state.block
        return when {
            block == Blocks.air -> true
            block == Blocks.fire || block == Blocks.tripwire || block == Blocks.web || block == Blocks.end_portal || block == Blocks.cocoa || block is BlockSkull || block is BlockTrapDoor -> false
            block is BlockDoor || block is BlockFenceGate -> {
                // TODO this assumes that all doors in all mods are openable
                if (block == Blocks.iron_door) {
                    false
                } else {
                    true
                }
            }

            block == Blocks.carpet -> null
            block is BlockSnow -> null
            block is BlockLiquid -> {
                if (state.getValue(BlockLiquid.LEVEL) != 0) {
                    false
                } else {
                    null
                }
            }

            block is BlockCauldron -> false
            block == Blocks.ladder -> false
            else -> {
                try {
                    block.isPassable(null, null)
                } catch (exception: Throwable) {
                    println("The block ${state.block.localizedName} requires a special case due to the exception ${exception.message}")
                    null
                }
            }
        }
    }

    fun canWalkThroughPosition(
        bsa: BlockStateAccessor,
        x: Int,
        y: Int,
        z: Int,
        state: IBlockState
    ): Boolean {
        val block = state.block

        if (block == Blocks.carpet) {
            return canStandOn(bsa, x, y - 1, z)
        }

        if (block is BlockSnow) {
            if (!bsa.isBlockInLoadedChunks(x, z)) {
                return true
            }
            if (state.getValue(BlockSnow.LAYERS) >= 1) {
                return false
            }
            return canStandOn(bsa, x, y - 1, z)
        }

        if (block is BlockLiquid) {
            if (isFlowing(x, y, z, state, bsa)) {
                return false
            }

            val up = bsa.get(x, y + 1, z)
            if (up.block is BlockLiquid || up.block is BlockLilyPad) {
                return false
            }
            return block == Blocks.water || block == Blocks.flowing_water
        }

        return block.isPassable(bsa.access, bsa.isPassableBlockPos.set(x, y, z))
    }

    fun canStandOn(bsa: BlockStateAccessor, x: Int, y: Int, z: Int, state: IBlockState = bsa.get(x, y, z)): Boolean {
        val block = state.block
        return when {
            block.isNormalCube -> true
            block == Blocks.ladder -> true
            block == Blocks.farmland || block == Blocks.grass -> true
            block == Blocks.ender_chest || block == Blocks.chest || block == Blocks.trapped_chest -> true
            block == Blocks.glass || block == Blocks.stained_glass -> true
            block is BlockStairs -> true
            block == Blocks.sea_lantern -> true
            isWotah(state) -> {
                val up = bsa.get(x, y + 1, z).block
                up == Blocks.waterlily || up == Blocks.carpet
            }

            isLava(state) -> false
            block is BlockSlab -> true
            block is BlockSnow -> true
            else -> false
        }
    }

    fun possiblyFlowing(state: IBlockState): Boolean {
        // Will be IFluidState in 1.13
        return state.block is BlockLiquid && state.getValue(BlockLiquid.LEVEL) != 0
    }

    fun isFlowing(x: Int, y: Int, z: Int, state: IBlockState, bsa: BlockStateAccessor): Boolean {
        if (state.block !is BlockLiquid) {
            return false
        }
        if (state.getValue(BlockLiquid.LEVEL) != 0) {
            return true
        }
        return possiblyFlowing(bsa.get(x + 1, y, z)) ||
                possiblyFlowing(bsa.get(x - 1, y, z)) ||
                possiblyFlowing(bsa.get(x, y, z + 1)) ||
                possiblyFlowing(bsa.get(x, y, z - 1))
    }


    fun isWotah(state: IBlockState): Boolean {
        val block = state.block
        return block == Blocks.water || block == Blocks.flowing_water
    }

    fun isLava(state: IBlockState): Boolean {
        val block = state.block
        return block == Blocks.lava || block == Blocks.flowing_lava
    }

    fun isBottomSlab(state: IBlockState): Boolean {
        return state.block is BlockSlab && !(state.block as BlockSlab).isDouble && state.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.BOTTOM
    }

    fun isValidStair(state: IBlockState, dx: Int, dz: Int): Boolean {
        if (dx == dz) return false
        if (state.block !is BlockStairs) return false
        if (state.getValue(BlockStairs.HALF) != BlockStairs.EnumHalf.BOTTOM) return false

        val stairFacing = state.getValue(BlockStairs.FACING)

        return when {
            dz == -1 -> stairFacing == EnumFacing.NORTH
            dz == 1 -> stairFacing == EnumFacing.SOUTH
            dx == -1 -> stairFacing == EnumFacing.WEST
            dx == 1 -> stairFacing == EnumFacing.EAST
            else -> false
        }
    }

    fun isValidReversedStair(state: IBlockState, dx: Int, dz: Int): Boolean {
        if (dx == dz) return false
        if (state.block !is BlockStairs) return false
        if (state.getValue(BlockStairs.HALF) != BlockStairs.EnumHalf.BOTTOM) return false

        val stairFacing = state.getValue(BlockStairs.FACING)

        return when {
            dz == 1 -> stairFacing == EnumFacing.NORTH
            dz == -1 -> stairFacing == EnumFacing.SOUTH
            dx == 1 -> stairFacing == EnumFacing.WEST
            dx == -1 -> stairFacing == EnumFacing.EAST
            else -> false
        }
    }

    fun hasTop(state: IBlockState, dX: Int, dZ: Int): Boolean {
        return !(isBottomSlab(state) || isValidStair(state, dX, dZ))
    }

    fun avoidWalkingInto(state: IBlockState): Boolean {
        val block = state.block
        return block is BlockLiquid || block is BlockFire || block == Blocks.cactus || block == Blocks.end_portal || block == Blocks.web
    }

    fun getFacing(dx: Int, dz: Int): EnumFacing {
        return if (dx == 0 && dz == 0) EnumFacing.UP else EnumFacing.HORIZONTALS[abs(dx) * (2 + dx) + abs(
            dz
        ) * (1 - dz)]
    }

    fun isLadder(state: IBlockState): Boolean {
        return state.block == Blocks.ladder
    }

    fun canWalkIntoLadder(ladderState: IBlockState, dx: Int, dz: Int): Boolean {
        return isLadder(ladderState) && ladderState.getValue(BlockLadder.FACING) != getFacing(
            dx,
            dz
        )
    }
}
