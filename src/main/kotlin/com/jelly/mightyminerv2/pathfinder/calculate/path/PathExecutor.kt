package com.jelly.mightyminerv2.pathfinder.calculate.path

import com.jelly.mightyminerv2.Handler.RotationHandler
import com.jelly.mightyminerv2.Util.AngleUtil
import com.jelly.mightyminerv2.Util.KeyBindUtil
import com.jelly.mightyminerv2.Util.LogUtil
import com.jelly.mightyminerv2.Util.RenderUtil
import com.jelly.mightyminerv2.Util.StrafeUtil
import com.jelly.mightyminerv2.Util.helper.Angle
import com.jelly.mightyminerv2.Util.helper.RotationConfiguration
import com.jelly.mightyminerv2.pathfinder.calculate.Path
import com.jelly.mightyminerv2.pathfinder.util.gameSettings
import com.jelly.mightyminerv2.pathfinder.util.mc
import com.jelly.mightyminerv2.pathfinder.util.player
import com.jelly.mightyminerv2.pathfinder.util.world
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.awt.Color
import kotlin.math.floor

object PathExecutor {
    var enabled = false
    val path = mutableListOf<BlockPos>()
    val hashedPath =
        linkedMapOf<Pair<Int, Int>, Int>() // to avoid looping over the whole list every time since im a schizophrenic fuck. id take speed over memory efficiency any day
    var targetIndex = 0
    var lastIndex = -1

    fun start(path: Path) {
        if (path.path.isEmpty()) {
            LogUtil.send("Path is empty")
            return
        }
        this.enabled = true;
        val smoothPath = path.getSmoothedPath()
        this.path.addAll(path.getSmoothedPath());
        for (i in smoothPath.indices) {
            hashedPath.put(Pair(smoothPath[i].x, smoothPath[i].z), i)
        }
    }

    fun stop() {
        this.enabled = false;
        this.path.clear();
        this.hashedPath.clear();
        this.targetIndex = 0;
        this.lastIndex = -1;
        StrafeUtil.enabled = false;

        KeyBindUtil.releaseAllExcept()
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (player == null || world == null || !this.enabled || mc.currentScreen != null) return

        val currentIndex = hashedPath[Pair(floor(player.posX).toInt(), floor(player.posZ).toInt())]
        if (currentIndex != null) {
            this.lastIndex = this.targetIndex
            this.targetIndex = currentIndex + 1
            RotationHandler.getInstance().reset()
            LogUtil.log("Position Updated. LastPos: ${this.lastIndex}, CurrentPos: ${this.targetIndex}")
            if (this.targetIndex == path.size) {
                LogUtil.send("Path Traversed. Disabling")
                this.stop()
                return
            }
        }

        var target = path[this.targetIndex]
        val yaw = AngleUtil.get360RotationYaw(AngleUtil.getRotation(target).yaw)
        val yawDiff = Math.abs(AngleUtil.get360RotationYaw() - yaw);

        if (yawDiff > 10 && !RotationHandler.getInstance().isEnabled()) {
            RotationHandler.getInstance().easeTo(RotationConfiguration(Angle(yaw, 20f), 250, null))
        }

        StrafeUtil.enabled = true;
        StrafeUtil.yaw = yaw;

        val shoudJump = player.onGround
                && Math.hypot(player.posX - (target.x + 0.5), player.posZ - (target.z + 0.5)) <= 1.0
                && target.y >= player.posY
                && Math.abs(target.y - player.posY) < 0.1
        KeyBindUtil.setKeyBindState(gameSettings.keyBindForward, true)
        KeyBindUtil.setKeyBindState(
            gameSettings.keyBindSprint,
            yawDiff < 40 && !shoudJump && player.onGround
        )
        if (shoudJump) {
            player.jump()
        }
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (!this.enabled) return;
        this.path.forEach { RenderUtil.drawBlockBox(it, Color(0, 255, 255, 50)) }
        RenderUtil.drawBlockBox(this.path[this.targetIndex], Color(255, 0, 0, 100))

        // for debugging
        val vec =
            mc.thePlayer.lookVec.add(AngleUtil.calcVec3FromRotation(Angle(StrafeUtil.yaw, 0f)))
        RenderUtil.drawPoint(Vec3(vec.xCoord * 2, vec.yCoord * 2, vec.zCoord * 2), Color.RED)
    }
}