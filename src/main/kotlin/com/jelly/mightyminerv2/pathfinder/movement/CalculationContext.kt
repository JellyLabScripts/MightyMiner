package com.jelly.mightyminerv2.pathfinder.movement

import com.jelly.mightyminerv2.MightyMiner
import com.jelly.mightyminerv2.pathfinder.costs.ActionCosts
import com.jelly.mightyminerv2.pathfinder.helper.BlockStateAccessor
import net.minecraft.block.state.IBlockState
import net.minecraft.potion.Potion

class CalculationContext(val mm: MightyMiner, sprintFactor: Double = 0.13, walkFactor: Double = 0.1, sneakFactor: Double = 0.03) {
  constructor(mm: MightyMiner): this(mm, 0.13, 0.1, 0.03)

  val world = mm.playerContext.world
  val player = mm.playerContext.player
  val bsa = BlockStateAccessor(mm)
  val jumpBoostAmplifier = player.getActivePotionEffect(Potion.jump)?.amplifier ?: -1;
  val cost = ActionCosts(sprintFactor, walkFactor, sneakFactor, jumpBoostAmplifier)
  val maxFallHeight = 20

  fun get(x: Int, y: Int, z: Int): IBlockState{
    return bsa.get(x, y, z)
  }
}