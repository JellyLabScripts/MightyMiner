package com.jelly.mightyminerv2.pathfinder.movement.movements

import com.jelly.mightyminerv2.MightyMiner
import com.jelly.mightyminerv2.Util.LogUtil
import com.jelly.mightyminerv2.pathfinder.movement.CalculationContext
import com.jelly.mightyminerv2.pathfinder.movement.Movement
import com.jelly.mightyminerv2.pathfinder.movement.MovementHelper
import com.jelly.mightyminerv2.pathfinder.movement.MovementResult
import net.minecraft.util.BlockPos

class MovementTraverse(mm: MightyMiner, from: BlockPos, to: BlockPos) : Movement(mm, from, to) {

  override fun calculateCost(ctx: CalculationContext, res: MovementResult) {
    calculateCost(ctx, source.x, source.y, source.z, dest.x, dest.z, res)
    costs = res.cost
  }

  companion object {
    fun calculateCost(ctx: CalculationContext, x: Int, y: Int, z: Int, destX: Int, destZ: Int, res: MovementResult) {
      res.set(destX, y, destZ)
      cost(ctx, x, y, z, destX, destZ, res)
    }

    private fun cost(ctx: CalculationContext, x: Int, y: Int, z: Int, destX: Int, destZ: Int, res: MovementResult) {
      if (!MovementHelper.canStandOn(ctx.bsa, destX, y, destZ)) return

      val destUpState = ctx.get(destX, y + 1, destZ)
      if (!MovementHelper.canWalkThrough(ctx.bsa, destX, y + 1, destZ, destUpState)
          || !MovementHelper.canWalkThrough(ctx.bsa, destX, y + 2, destZ))
        return

      val srcUpState = ctx.get(x, y + 1, z)

      val isSourceTopWalkableLadder = MovementHelper.canWalkIntoLadder(srcUpState, x - destX, z - destZ)
      val isDestTopWalkableLadder = MovementHelper.canWalkIntoLadder(destUpState, destX - x, destZ - z)
      res.cost = ctx.cost.ONE_BLOCK_WALK_COST
      if (MovementHelper.isLadder(destUpState) && !isDestTopWalkableLadder) {
        res.cost = ctx.cost.INF_COST
        return
      }
      if (MovementHelper.isLadder(srcUpState) && !isSourceTopWalkableLadder) {
        res.cost = ctx.cost.INF_COST
        return
      }
      res.cost = ctx.cost.ONE_BLOCK_SPRINT_COST
    }
  }
}