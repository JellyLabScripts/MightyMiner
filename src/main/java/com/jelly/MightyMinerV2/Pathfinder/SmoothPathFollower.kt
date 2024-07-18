package com.jelly.MightyMinerV2.Pathfinder

import com.jelly.MightyMinerV2.Util.KeyBindUtil
import com.jelly.MightyMinerV2.Util.LogUtil
import com.jelly.MightyMinerV2.Util.AngleUtil
import net.minecraft.client.Minecraft
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraft.world.World
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.sign

class SmoothPathFollower(
    private val speed: Double // Movement speed (blocks per tick)
) {
    private val mc: Minecraft = Minecraft.getMinecraft()
    private var path: List<BlockPos> = emptyList()
    private var currentStep = 0
    private var stepProgress = 0

    private var targetYaw: Float = 0.0f
    private var targetPitch: Float = 0.0f

    fun followPath(world: World?, path: List<BlockPos>) {
        if (path.isEmpty()) {
            LogUtil.send("Path list is empty.", LogUtil.ELogType.ERROR)
            return
        }
        this.path = path
        this.currentStep = 0
        this.stepProgress = 0
        MinecraftForge.EVENT_BUS.register(this)  // Register the event listener
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (path.isEmpty() || currentStep >= path.size) {
            MinecraftForge.EVENT_BUS.unregister(this)  // Unregister the event listener
            KeyBindUtil.stopMovement()
            return
        }

        val start = mc.thePlayer.positionVector
        val end = Vec3(path[currentStep].x.toDouble() + 0.5, path[currentStep].y.toDouble(), path[currentStep].z.toDouble() + 0.5)
        moveToPosition(start, end)
    }

    private fun moveToPosition(start: Vec3, end: Vec3) {
        val direction = end.subtract(start).normalize()
        val distance = start.distanceTo(end)
        val steps = ceil(distance / speed).toInt()

        LogUtil.send(
            "Moving from (${start.xCoord}, ${start.yCoord}, ${start.zCoord}) to (${end.xCoord}, ${end.yCoord}, ${end.zCoord}) in $steps steps.",
            LogUtil.ELogType.DEBUG
        )

        if (start.distanceTo(end) < 0.1) {
            stepProgress = 0
            currentStep++
            KeyBindUtil.stopMovement()
            return
        }

        val nextPos = start.add(
            Vec3(direction.xCoord * speed, direction.yCoord * speed, direction.zCoord * speed)
        )

        // Get the required key presses for the current movement
        val requiredKeys = KeyBindUtil.getNeededKeyPresses(mc.thePlayer.positionVector, nextPos)
        KeyBindUtil.holdThese(*requiredKeys.toTypedArray())

        // Ensure the player is looking in the right direction
        val targetAngle = AngleUtil.getRotation(nextPos)
        targetYaw = targetAngle.yaw
        targetPitch = targetAngle.pitch

        // Smoothly adjust the player's yaw and pitch
        mc.thePlayer.rotationYaw = smoothAdjustAngle(mc.thePlayer.rotationYaw, targetYaw, 2.0f)
        mc.thePlayer.rotationPitch = smoothAdjustAngle(mc.thePlayer.rotationPitch, targetPitch, 2.0f)

        // Update step progress
        stepProgress++
    }

    private fun smoothAdjustAngle(current: Float, target: Float, maxChange: Float): Float {
        val delta = AngleUtil.normalizeAngle(target - current)
        val angleChange = maxChange * sign(delta)
        return if (abs(delta) <= maxChange) {
            target
        } else {
            current + angleChange
        }
    }
}
