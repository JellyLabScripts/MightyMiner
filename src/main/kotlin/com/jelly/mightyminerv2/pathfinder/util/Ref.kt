package com.jelly.mightyminerv2.pathfinder.util

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator

val mc
    get() = Minecraft.getMinecraft()
val player
    get() = mc.thePlayer
val world
    get() = mc.theWorld
val tessellator
    get() = Tessellator.getInstance()
val gameSettings
    get() = mc.gameSettings