package com.jelly.mightyminerv2.pathfinder.util

import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import kotlin.math.ceil
import kotlin.math.floor

fun EntityLivingBase.getStandingOnCeil() = BlockPos(posX, ceil(posY) - 1, posZ)
fun EntityLivingBase.getStandingOnFloor() = BlockPos(posX, floor(posY) - 1, posZ)
fun KeyBinding.setPressed(pressed: Boolean) = KeyBinding.setKeyBindState(keyCode, pressed)
fun BlockPos.toVec3() = Vec3(x.toDouble() + 0.5, y.toDouble() + 0.5, z.toDouble() + 0.5)
fun BlockPos.toVec3Top(): Vec3 = toVec3().addVector(0.0, 0.5, 0.0)
fun Vec3.toBlockPos(): BlockPos = BlockPos(xCoord, yCoord, zCoord)

fun EntityLivingBase.lastTickPositionCeil() = BlockPos(lastTickPosX, ceil(lastTickPosY) - 1, lastTickPosZ)