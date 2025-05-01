package com.jelly.mightyminerv2.pathfinder.util

import com.jelly.mightyminerv2.pathfinder.movement.CalculationContext
import com.jelly.mightyminerv2.pathfinder.movement.MovementHelper
import net.minecraft.block.BlockSnow
import net.minecraft.block.BlockStairs
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.abs

object BlockUtil {
    fun canWalkOnBlock(pos: BlockPos): Boolean {
        val block = world.getBlockState(pos.add(0, 0, 0)).block
        val blockAbove = world.getBlockState(pos.up()).block

        val material: Material = block.material
        val materialAbove: Material = blockAbove.material

        return material.isSolid && !material.isLiquid && materialAbove == Material.air
    }

    fun neighbourGenerator(
        mainBlock: BlockPos,
        xD1: Int,
        xD2: Int,
        yD1: Int,
        yD2: Int,
        zD1: Int,
        zD2: Int
    ): List<BlockPos> {
        val neighbours: MutableList<BlockPos> = ArrayList()
        for (x in xD1..xD2) {
            for (y in yD1..yD2) {
                for (z in zD1..zD2) {
                    neighbours.add(BlockPos(mainBlock.x + x, mainBlock.y + y, mainBlock.z + z))
                }
            }
        }
        return neighbours
    }

    fun isStairSlab(block: BlockPos): Boolean {
        return world.getBlockState(block).block is BlockStairs ||
                world.getBlockState(block).block is BlockStairs
    }

//    fun blocksBetweenValid(ctx: CalculationContext = CalculationContext(MightyMiner.instance), startPoss: BlockPos, endPoss: BlockPos): Boolean {
//        val blocksBetween = bresenham(ctx, startPoss.toVec3(), endPoss.toVec3())
//        if (blocksBetween.isEmpty()) {
//            return false
//        }
//        for (i in blocksBetween.indices) {
//            val curr = blocksBetween[i]
//            if (!MovementHelper.canStandOn(ctx.bsa, curr.x, curr.y, curr.z)
//                || !MovementHelper.canWalkThrough(ctx.bsa, curr.x, curr.y + 1, curr.z)
//                || !MovementHelper.canWalkThrough(ctx.bsa, curr.x, curr.y + 2, curr.z)) {
//                return false
//            }
//            if (i == 0) continue
//            if (!canWalkOn(ctx, blocksBetween[i - 1], curr)) {
//                return false
//            }
//        }
//        return true
//    }

    fun getDirectionToWalkOnStairs(state: IBlockState): EnumFacing {
        return when (state.block.getMetaFromState(state)) {
            0 -> {
                EnumFacing.EAST
            }

            1 -> {
                EnumFacing.WEST
            }

            2 -> {
                EnumFacing.SOUTH
            }

            3 -> {
                EnumFacing.NORTH
            }

            4 -> {
                EnumFacing.DOWN
            }

            else -> EnumFacing.UP
        }
    }

    fun getPlayerDirectionToBeAbleToWalkOnBlock(startPos: BlockPos, endPoss: BlockPos): EnumFacing {
        val deltaX: Int = endPoss.x - startPos.x
        val deltaZ: Int = endPoss.z - startPos.z

        return if (abs(deltaX) > abs(deltaZ)) {
            if (deltaX > 0) EnumFacing.EAST else EnumFacing.WEST
        } else {
            if (deltaZ > 0) EnumFacing.SOUTH else EnumFacing.NORTH
        }
    }

    fun canWalkOn(ctx: CalculationContext, startPos: BlockPos, endPos: BlockPos): Boolean {
        val startState = ctx.bsa.get(startPos.x, startPos.y, startPos.z)
        val endState = ctx.bsa.get(endPos.x, endPos.y, endPos.z)
        if (!endState.block.material.isSolid) {
            return endPos.y - startPos.y <= 1
        }
        val sourceMaxY =
            startState.block.getCollisionBoundingBox(ctx.world, startPos, startState)?.maxY
                ?: startPos.y.toDouble()
        val destMaxY = endState.block.getCollisionBoundingBox(ctx.world, endPos, endState)?.maxY
            ?: (startPos.y + 1.0)
        if (endState.block is BlockStairs && destMaxY - sourceMaxY > 1.0) {
            return MovementHelper.isValidStair(
                endState,
                endPos.x - startPos.x,
                endPos.z - startPos.z
            )
        }
        return destMaxY - sourceMaxY <= .5
    }

    fun bresenham(ctx: CalculationContext, start: BlockPos, end: BlockPos): Boolean {
        return bresenham(ctx, Vec3(start).addVector(0.5, 0.5, 0.5), Vec3(end).addVector(0.5, 0.5, 0.5))
    }

    fun bresenham(ctx: CalculationContext, start: Vec3, end: Vec3): Boolean {
        var start0 = start
        val bsa = ctx.bsa

        val x1 = MathHelper.floor_double(end.xCoord)
        val y1 = MathHelper.floor_double(end.yCoord)
        val z1 = MathHelper.floor_double(end.zCoord)
        var x0 = MathHelper.floor_double(start0.xCoord)
        var y0 = MathHelper.floor_double(start0.yCoord)
        var z0 = MathHelper.floor_double(start0.zCoord)

        var lastState = bsa.get(x0, y0, z0)
        var lastPos = BlockPos(start)

        var iterations = 200
        while (iterations-- >= 0) {
            if (x0 == x1 && y0 == y1 && z0 == z1) {
                return true
            }
            var hasNewX = true
            var hasNewY = true
            var hasNewZ = true
            var newX = 999.0
            var newY = 999.0
            var newZ = 999.0
            if (x1 > x0) {
                newX = x0.toDouble() + 1.0
            } else if (x1 < x0) {
                newX = x0.toDouble() + 0.0
            } else {
                hasNewX = false
            }
            if (y1 > y0) {
                newY = y0.toDouble() + 1.0
            } else if (y1 < y0) {
                newY = y0.toDouble() + 0.0
            } else {
                hasNewY = false
            }
            if (z1 > z0) {
                newZ = z0.toDouble() + 1.0
            } else if (z1 < z0) {
                newZ = z0.toDouble() + 0.0
            } else {
                hasNewZ = false
            }
            var stepX = 999.0
            var stepY = 999.0
            var stepZ = 999.0
            val dx = end.xCoord - start0.xCoord
            val dy = end.yCoord - start0.yCoord
            val dz = end.zCoord - start0.zCoord
            if (hasNewX) stepX = (newX - start0.xCoord) / dx
            if (hasNewY) stepY = (newY - start0.yCoord) / dy
            if (hasNewZ) stepZ = (newZ - start0.zCoord) / dz
            if (stepX == -0.0) stepX = -1.0E-4
            if (stepY == -0.0) stepY = -1.0E-4
            if (stepZ == -0.0) stepZ = -1.0E-4
            var enumfacing: EnumFacing
            if (stepX < stepY && stepX < stepZ) {
                enumfacing = if (x1 > x0) EnumFacing.WEST else EnumFacing.EAST
                start0 = Vec3(newX, start0.yCoord + dy * stepX, start0.zCoord + dz * stepX)
            } else if (stepY < stepZ) {
                enumfacing = if (y1 > y0) EnumFacing.DOWN else EnumFacing.UP
                start0 = Vec3(start0.xCoord + dx * stepY, newY, start0.zCoord + dz * stepY)
            } else {
                enumfacing = if (z1 > z0) EnumFacing.NORTH else EnumFacing.SOUTH
                start0 = Vec3(start0.xCoord + dx * stepZ, start0.yCoord + dy * stepZ, newZ)
            }
            x0 = MathHelper.floor_double(start0.xCoord) - if (enumfacing == EnumFacing.EAST) 1 else 0
            y0 = MathHelper.floor_double(start0.yCoord) - if (enumfacing == EnumFacing.UP) 1 else 0
            z0 = MathHelper.floor_double(start0.zCoord) - if (enumfacing == EnumFacing.SOUTH) 1 else 0

            var currState = ctx.bsa.get(x0, y0, z0)
            var i = 0
            if (!MovementHelper.canStandOn(bsa, x0, y0, z0, currState) || !MovementHelper.canWalkThrough(
                    bsa,
                    x0,
                    y0 + 1,
                    z0
                ) || !MovementHelper.canWalkThrough(bsa, x0, y0 + 2, z0)
            ) {
                i = -3
                var foundValidBlock = false
                while (++i <= 3) {
                    if (i == 0) continue
                    currState = bsa.get(x0, y0 + i, z0)
                    if (!MovementHelper.canStandOn(ctx.bsa, x0, y0 + i, z0, currState)) {
                        continue
                    }
                    if (!MovementHelper.canWalkThrough(bsa, x0, y0 + i + 1, z0)) {
                        continue
                    }
                    if (!MovementHelper.canWalkThrough(bsa, x0, y0 + i + 2, z0)) {
                        continue
                    }
                    foundValidBlock = true
                    break
                }
                if (!foundValidBlock) {
                    return false
                }
            }

            val delta = (y0 + i) - lastPos.y
            if (delta > 0) {
                if (delta > 1) {
                    return false
                }
                var sourceHeight = -1.0
                var destHeight = -1.0
                var snow = false
                if (lastState.block is BlockSnow) {
                    sourceHeight = (lastState.getValue(BlockSnow.LAYERS) - 1) * 0.125
                    snow = true
                }

                if (currState.block is BlockSnow) {
                    destHeight = (currState.getValue(BlockSnow.LAYERS) - 1) * 0.125
                    snow = true
                }
                if (!snow) {
                    val srcSmall = MovementHelper.isBottomSlab(lastState)
                    val destSmall = MovementHelper.isBottomSlab(currState)

                    val destSmallStair =
                        MovementHelper.isValidStair(currState, x0 - lastPos.x, z0 - lastPos.z)

                    if (!srcSmall == !(destSmall || destSmallStair)) {
                        return false
                    } else if (srcSmall) {
                        return false
                    }
                } else {
                    if (sourceHeight == -1.0) {
                        sourceHeight = lastState.block.blockBoundsMaxY
                    }
                    if (destHeight == -1.0) {
                        destHeight = currState.block.blockBoundsMaxY
                    }
                    if (destHeight - sourceHeight > -0.5) {
                        return false
                    }
                }
            }

            lastState = currState
            lastPos = BlockPos(x0, y0 + i, z0)
        }
//        println("shit")
        return false
    }
}
