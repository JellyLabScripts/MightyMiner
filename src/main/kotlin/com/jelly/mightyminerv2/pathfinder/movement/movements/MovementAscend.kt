package com.jelly.mightyminerv2.pathfinder.movement.movements

import com.jelly.mightyminerv2.MightyMiner
import com.jelly.mightyminerv2.pathfinder.movement.CalculationContext
import com.jelly.mightyminerv2.pathfinder.movement.Movement
import com.jelly.mightyminerv2.pathfinder.movement.MovementHelper
import com.jelly.mightyminerv2.pathfinder.movement.MovementResult
import net.minecraft.block.BlockSnow
import net.minecraft.util.BlockPos

class MovementAscend(mm: MightyMiner, from: BlockPos, to: BlockPos) : Movement(mm, from, to) {
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
      res.set(destX, y + 1, destZ)
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
      val destState = ctx.get(destX, y + 1, destZ)
      if (!MovementHelper.canStandOn(ctx.bsa, destX, y + 1, destZ, destState)) return
      if (!MovementHelper.canWalkThrough(ctx.bsa, destX, y + 3, destZ)) return
      if (!MovementHelper.canWalkThrough(ctx.bsa, destX, y + 2, destZ)) return
      if (!MovementHelper.canWalkThrough(ctx.bsa, x, y + 3, z)) return

      val sourceState = ctx.get(x, y, z)
      if (MovementHelper.isLadder(sourceState)) return
      if (MovementHelper.isLadder(destState) && !MovementHelper.canWalkIntoLadder(
          destState,
          destX - x,
          destZ - z
        )
      ) return

      // small = half block / stair - in this case it doesnt matter which way the source stair(if it is a stair) is facing
      // big = fill block
      // this logic is actually fucking sick ngl

      var sourceHeight = -1.0
      var destHeight = -1.0
      var snow = false
      if (sourceState.block is BlockSnow) {
        sourceHeight = (sourceState.getValue(BlockSnow.LAYERS) - 1) * 0.125
        snow = true;
      }
      if (destState.block is BlockSnow) {
        destHeight = (destState.getValue(BlockSnow.LAYERS) - 1) * 0.125
        snow = true
      }
      if (!snow) {
        val srcSmall = MovementHelper.isBottomSlab(sourceState);
        val destSmall = MovementHelper.isBottomSlab(destState);

        val destSmallStair = MovementHelper.isValidStair(destState, destX - x, destZ - z);

        if (!srcSmall == !(destSmall || destSmallStair)) {
          res.cost = ctx.cost.JUMP_ONE_BLOCK_COST
        } else if (!srcSmall) {
          res.cost = ctx.cost.ONE_BLOCK_SPRINT_COST;
        }
      } else {
        if (destHeight == -1.0) {
          destHeight = destState.block.blockBoundsMaxY
        }

        if (sourceHeight == -1.0) {
          sourceHeight = sourceState.block.blockBoundsMaxY
        }
        val diff = destHeight - sourceHeight
        res.cost = when {
          diff <= -0.5 -> ctx.cost.ONE_BLOCK_SPRINT_COST
          diff <= 0.125 -> ctx.cost.JUMP_ONE_BLOCK_COST
          else -> ctx.cost.INF_COST
        }
      }
    }
  }
}