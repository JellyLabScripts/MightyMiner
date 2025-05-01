package com.jelly.mightyminerv2.pathfinder.helper.player

import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.PlayerControllerMP
import net.minecraft.util.BlockPos
import net.minecraft.world.World

interface IPlayerContext {
    val mc: Minecraft
    val player: EntityPlayerSP
    val playerController: PlayerControllerMP
    val world: World
    val playerPosition: BlockPos
}