package com.jelly.mightyminerv2.pathfinder.movement

import com.jelly.mightyminerv2.pathfinder.costs.ActionCosts
import com.jelly.mightyminerv2.pathfinder.helper.BlockStateAccessor
import com.jelly.mightyminerv2.pathfinder.helper.player.PlayerContext
import net.minecraft.block.state.IBlockState
import net.minecraft.client.Minecraft
import net.minecraft.potion.Potion

class CalculationContext(sprintFactor: Double = 0.13, walkFactor: Double = 0.1, sneakFactor: Double = 0.03) {
    val playerContext = PlayerContext(Minecraft.getMinecraft())
    val world = playerContext.world
    val player = playerContext.player
    val bsa = BlockStateAccessor(world)
    val jumpBoostAmplifier = player.getActivePotionEffect(Potion.jump)?.amplifier ?: -1
    val cost = ActionCosts(sprintFactor, walkFactor, sneakFactor, jumpBoostAmplifier)
    val maxFallHeight = 20

    fun get(x: Int, y: Int, z: Int): IBlockState {
        return bsa.get(x, y, z)
    }
}