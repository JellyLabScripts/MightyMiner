package com.jelly.MightyMiner.player;

import com.jelly.MightyMiner.utils.AngleUtils;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.tuple.MutablePair;

public class ContinuousRotator {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public boolean canceled = false;

    MutablePair<Float, Float> target = new MutablePair<>(0f, 0f);
    private long goalChangeTimeStamp = 0;

    private void easeTo(float yaw, float pitch) {
        this.goalChangeTimeStamp = System.currentTimeMillis();
        target.setLeft(AngleUtils.getActualYawFrom360(yaw));
        target.setRight(pitch);
    }

    public static float lerpAngle(float fromRadians, float toRadians, float elapsedTime, float duration) {
        // Calculate the interpolation progress based on the elapsed time and the duration
        float progress = elapsedTime / duration;
        // Clamp the progress to the range [0, 1] to ensure that it doesn't exceed the bounds of the interpolation
        progress = Math.min(Math.max(progress, 0), 1);
        // Use linear interpolation to calculate the interpolated angle
        float interpolatedAngle = fromRadians + (toRadians - fromRadians) * progress;
        // Return the interpolated angle, wrapped around to the range [0, 360)
        return interpolatedAngle % 360.0F;
    }


    public static float lerpAngleEaseIn(float fromRadians, float toRadians, float elapsedTime, float duration) {
        // Calculate the interpolation progress based on the elapsed time and the duration
        float progress = elapsedTime / duration;
        // Modify the progress using an easing function
        progress = easeOut(progress);
        float f = ((toRadians - fromRadians) % 360.0F + 540.0F) % 360.0F - 180.0F;
        return (fromRadians + f * progress % 360.0F);
    }


    // Easing function that applies an ease-out effect to the progress
    public static float easeOut(float progress) {
        return (float)(1 - Math.pow(1 - progress, 2));
    }

    public static float lerpAngleSin(float fromRadians, float toRadians, float progress) {
        // Calculate the difference between the two angles
        float delta = toRadians - fromRadians;
        // Use the sin function to interpolate between the two angles
        float interpolatedAngle = fromRadians + delta * (float)Math.sin(progress * Math.PI / 2);
        // Return the interpolated angle, wrapped around to the range [0, 360)
        return interpolatedAngle % 360.0F;
    }


    public void changeGoal(float yaw, float pitch) {
        float playerYaw = (float) Math.floor(mc.thePlayer.rotationYaw);
        float playerPitch = (float) Math.floor(mc.thePlayer.rotationPitch);

        float targetYaw = (float) Math.floor(yaw);
        float targetPitch = (float) Math.floor(pitch);

        // "real" means that its in 360 format instead of -180 to 180
        float realPlayerYaw = AngleUtils.get360RotationYaw(playerYaw); // TODO:fix this
        float realPlayerPitch = AngleUtils.get360RotationYaw(playerPitch);
        float realTargetYaw = AngleUtils.get360RotationYaw(targetYaw);
        float realTargetPitch = AngleUtils.get360RotationYaw(targetPitch);

        if (realPlayerYaw != realTargetYaw || realTargetPitch != realPlayerPitch) {
            easeTo(yaw, pitch);
        }
    }

    public void update() {

        if(canceled) return;

        if(Math.abs(target.left - AngleUtils.getActualYawFrom360(mc.thePlayer.rotationYaw)) < 1){
            return;
        }

        long timeSinceGoalChange = System.currentTimeMillis() - goalChangeTimeStamp;

        mc.thePlayer.rotationYaw = lerpAngle(mc.thePlayer.rotationYaw, target.left, timeSinceGoalChange, 50);
        mc.thePlayer.rotationPitch = lerpAngle(mc.thePlayer.rotationPitch, target.right, timeSinceGoalChange, 50);
    }


}
