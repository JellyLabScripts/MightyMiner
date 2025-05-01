package com.jelly.mightyminerv2.pathfinder.movement.movements

import com.jelly.mightyminerv2.MightyMiner
import com.jelly.mightyminerv2.pathfinder.movement.CalculationContext
import com.jelly.mightyminerv2.pathfinder.movement.Movement
import com.jelly.mightyminerv2.pathfinder.movement.MovementHelper
import com.jelly.mightyminerv2.pathfinder.movement.MovementResult
import net.minecraft.block.state.IBlockState
import net.minecraft.util.BlockPos

class MovementDescend(mm: MightyMiner, from: BlockPos, to: BlockPos) : Movement(mm, from, to) {
    override fun calculateCost(ctx: CalculationContext, res: MovementResult) {
        calculateCost(ctx, source.x, source.y, source.z, dest.x, dest.z, res)
        costs = res.cost
    }


    companion object {
        fun calculateCost(
            ctx: CalculationContext,
            x: Int,
            y: Int,
            z: Int,
            destX: Int,
            destZ: Int,
            res: MovementResult
        ) {
            res.set(destX, y - 1, destZ)
            cost(ctx, x, y, z, destX, destZ, res)
        }

        private fun cost(
            ctx: CalculationContext,
            x: Int,
            y: Int,
            z: Int,
            destX: Int,
            destZ: Int,
            res: MovementResult
        ) {
            val destUpState = ctx.get(destX, y, destZ)
            if (!MovementHelper.canWalkThrough(ctx.bsa, destX, y + 2, destZ)
                || !MovementHelper.canWalkThrough(ctx.bsa, destX, y + 1, destZ)
                || !MovementHelper.canWalkThrough(ctx.bsa, destX, y, destZ, destUpState)
            ) {
                return
            }
            val sourceState = ctx.get(x, y, z)
            if (MovementHelper.isLadder(sourceState) || MovementHelper.isLadder(destUpState)) { // Cannot descend from ladder it'll be MovementDownward
                return
            }
            val destState = ctx.get(destX, y - 1, destZ)
            if (!MovementHelper.canStandOn(
                    ctx.bsa,
                    destX,
                    y - 1,
                    destZ,
                    destState
                ) || MovementHelper.isLadder(destState)
            ) {
                freeFallCost(ctx, x, y, z, destX, destZ, destState, res)
                return
            }

            val sourceHeight =
                sourceState.block.getCollisionBoundingBox(ctx.world, BlockPos(x, y, z), sourceState)?.maxY ?: return
            val destHeight =
                destState.block.getCollisionBoundingBox(ctx.world, BlockPos(destX, y - 1, destZ), destState)?.maxY
                    ?: return
            val diff = sourceHeight - destHeight
//            println("SourceHeight: $sourceHeight, DestHeight: $destHeight, Diff: $diff")
            res.cost = when {
                diff <= 0.5 -> ctx.cost.ONE_BLOCK_WALK_COST
                diff <= 1.125 -> ctx.cost.WALK_OFF_ONE_BLOCK_COST * ctx.cost.SPRINT_MULTIPLIER + ctx.cost.N_BLOCK_FALL_COST[1]
                else -> ctx.cost.INF_COST
            }
            // small = half block / stair - in this case stair should be facing the player otherwise its descend instead of a walk
            // big = fill block

//            val srcSmall = MovementHelper.isBottomSlab(sourceState);
//            val destSmall = MovementHelper.isBottomSlab(destState);
//
//            val srcSmallStair =
//                MovementHelper.isValidReversedStair(sourceState, destX - x, destZ - z);
//            val destSmallStair =
//                MovementHelper.isValidReversedStair(destState, destX - x, destZ - z);
//
//             Todo: this can ** probably ** be simplified
//            if (!(srcSmall || srcSmallStair) == !(destSmall || destSmallStair)) {
//                if (destSmallStair) {
//                    res.cost = ctx.cost.ONE_BLOCK_SPRINT_COST;
//                } else {
//                    res.cost =
//                        ctx.cost.WALK_OFF_ONE_BLOCK_COST * ctx.cost.SPRINT_MULTIPLIER + ctx.cost.N_BLOCK_FALL_COST[1]
//                }
//            } else if (!(destSmall || destSmallStair)) {
//                res.cost = ctx.cost.ONE_BLOCK_SPRINT_COST;
//            } else if (!(srcSmall || srcSmallStair)) {
//                res.cost =
//                    ctx.cost.WALK_OFF_ONE_BLOCK_COST * ctx.cost.SPRINT_MULTIPLIER + ctx.cost.N_BLOCK_FALL_COST[1]
//            }

//            if (srcSmall == destSmall && srcSmallStair == destSmallStair) {
//                if (destSmallStair) {
//                    res.cost = ctx.cost.ONE_BLOCK_SPRINT_COST;
//                } else {
//                    ctx.cost.WALK_OFF_ONE_BLOCK_COST * ctx.cost.SPRINT_MULTIPLIER + ctx.cost.N_BLOCK_FALL_COST[1]
//                }
//            } else {
//                res.cost =
//                    ctx.cost.WALK_OFF_ONE_BLOCK_COST * ctx.cost.SPRINT_MULTIPLIER + ctx.cost.N_BLOCK_FALL_COST[1]
//            }
            // forgot to add slab will add later
//            res.cost =
//                ctx.cost.WALK_OFF_ONE_BLOCK_COST * ctx.cost.SPRINT_MULTIPLIER + ctx.cost.N_BLOCK_FALL_COST[1]
        }

        fun freeFallCost(
            ctx: CalculationContext,
            x: Int,
            y: Int,
            z: Int,
            destX: Int,
            destZ: Int,
            destState: IBlockState,
            res: MovementResult
        ) {
            // im starting from 2 because I work with the blocks itself. x, y, z aren't for sourceBlock.up() like its in baritone its sourceBlock
            if (!MovementHelper.canWalkThrough(ctx.bsa, destX, y - 1, destZ, destState)) {
                return
            }

            var effStartHeight = y // for ladder
            var cost = 0.0
            for (fellSoFar in 2..Int.MAX_VALUE) {
                val newY = y - fellSoFar
                if (newY < 0) return

                val blockOnto = ctx.get(destX, newY, destZ)
                val unprotectedFallHeight = fellSoFar - (y - effStartHeight) // basic math
                val costUpUntilThisBlock =
                    ctx.cost.WALK_OFF_ONE_BLOCK_COST + ctx.cost.N_BLOCK_FALL_COST[unprotectedFallHeight] + cost

                // This is probably a massive monkeypatch. Can't wait to suffer
                if (!MovementHelper.canStandOn(ctx.bsa, destX, newY, destZ, blockOnto)) {
                    if (MovementHelper.isWotah(blockOnto)) {
                        if (MovementHelper.canStandOn(ctx.bsa, destX, newY - 1, destZ)) {
                            res.y = newY - 1
                            res.cost = costUpUntilThisBlock
                            return
                        }
                        return
                    }

                    if (!MovementHelper.canWalkThrough(ctx.bsa, destX, newY, destZ, blockOnto)) {
                        return
                    }
                    continue
                }
                if (unprotectedFallHeight <= 11 && MovementHelper.isLadder(blockOnto)) {
                    // very cool logic baritone is built by smart ppl ong
                    cost += ctx.cost.N_BLOCK_FALL_COST[unprotectedFallHeight - 1] + ctx.cost.ONE_DOWN_LADDER_COST
                    effStartHeight = newY
                    continue
                }
                if (fellSoFar <= ctx.maxFallHeight) {
                    res.y = newY
                    res.cost = costUpUntilThisBlock
                    return
                }
                return
            }
        }
    }
}