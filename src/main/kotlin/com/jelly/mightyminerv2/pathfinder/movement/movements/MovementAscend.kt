package com.jelly.mightyminerv2.pathfinder.movement.movements

import com.jelly.mightyminerv2.MightyMiner
import com.jelly.mightyminerv2.pathfinder.movement.CalculationContext
import com.jelly.mightyminerv2.pathfinder.movement.Movement
import com.jelly.mightyminerv2.pathfinder.movement.MovementHelper
import com.jelly.mightyminerv2.pathfinder.movement.MovementResult
import net.minecraft.util.BlockPos

class MovementAscend(mm: MightyMiner, from: BlockPos, to: BlockPos) : Movement(mm, from, to) {
  override fun calculateCost(ctx: CalculationContext, res: MovementResult) {
    calculateCost(ctx, source.x, source.y, source.z, dest.x, dest.z, res)
    costs = res.cost
  }

  companion object {
    fun calculateCost(ctx: CalculationContext, x: Int, y: Int, z: Int, destX: Int, destZ: Int, res: MovementResult) {
      res.set(destX, y + 1, destZ)
      cost(ctx, x, y, z, destX, destZ, res)
    }

    private fun cost(ctx: CalculationContext, x: Int, y: Int, z: Int, destX: Int, destZ: Int, res: MovementResult) {
      val destState = ctx.get(destX, y + 1, destZ)
      if (!MovementHelper.canStandOn(ctx.bsa, destX, y + 1, destZ, destState)) return
      if (!MovementHelper.canWalkThrough(ctx.bsa, destX, y + 3, destZ)) return
      if (!MovementHelper.canWalkThrough(ctx.bsa, destX, y + 2, destZ)) return
      if (!MovementHelper.canWalkThrough(ctx.bsa, x, y + 3, z)) return

      val sourceState = ctx.get(x, y, z)
      if (MovementHelper.isLadder(sourceState)) return
      if (MovementHelper.isLadder(destState) && !MovementHelper.canWalkIntoLadder(destState, destX - x, destZ - z)) return

      val sourceMaxY = sourceState.block.getCollisionBoundingBox(ctx.world, BlockPos(x, y, z), sourceState)?.maxY ?: y.toDouble()
      val destMaxY = destState.block.getCollisionBoundingBox(ctx.world, BlockPos(destX, y + 1, destZ), destState)?.maxY ?: (y + 1.0)

      res.cost = when {
        destMaxY - sourceMaxY <= 0.5 -> ctx.cost.ONE_BLOCK_SPRINT_COST
        destMaxY - sourceMaxY <= 1.125 -> ctx.cost.JUMP_ONE_BLOCK_COST
        else -> ctx.cost.INF_COST                                           // Should never trigger. Maybe throw exception in case it triggers?
      }
    }
  }
}