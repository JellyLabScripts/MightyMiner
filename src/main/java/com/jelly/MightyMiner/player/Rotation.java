package com.jelly.MightyMiner.player;

import com.jelly.MightyMiner.utils.AngleUtils;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.tuple.MutablePair;

public class Rotation {
    private final static Minecraft mc = Minecraft.getMinecraft();
    public boolean rotating;
    public boolean completed;

    private long startTime;
    private long endTime;

    MutablePair<Float, Float> start = new MutablePair<>(0f, 0f);
    MutablePair<Float, Float> target = new MutablePair<>(0f, 0f);
    MutablePair<Float, Float> difference = new MutablePair<>(0f, 0f);

    public void easeTo(float yaw, float pitch, long time) {
        completed = false;
        rotating = true;
        startTime = System.currentTimeMillis();
        endTime = System.currentTimeMillis() + time;
        start.setLeft(mc.thePlayer.rotationYaw);
        start.setRight(mc.thePlayer.rotationPitch);
        target.setLeft(AngleUtils.getActualYawFrom360(yaw));
        target.setRight(pitch);
        getDifference();
    }

    public void intLockAngle(float yaw, float pitch, int time) {
        if ((AngleUtils.get360RotationYaw((float) Math.floor(mc.thePlayer.rotationYaw)) != AngleUtils.get360RotationYaw((float) Math.floor(yaw))
                ||AngleUtils.get360RotationYaw((float) Math.floor(mc.thePlayer.rotationPitch)) != AngleUtils.get360RotationYaw((float) Math.floor(pitch))) && !rotating)
            easeTo(yaw, pitch, time);
    }

    public void update() {
        if (System.currentTimeMillis() <= endTime) {
            if (shouldRotateClockwise()) {
                mc.thePlayer.rotationYaw = start.left + interpolate(difference.left);
            } else {
                mc.thePlayer.rotationYaw = start.left - interpolate(difference.left);
            }
            mc.thePlayer.rotationPitch = start.right + interpolate(difference.right);
        }
        else if (!completed) {
            mc.thePlayer.rotationYaw = target.left;
            mc.thePlayer.rotationPitch = start.right + difference.right;
            completed = true;
            rotating = false;
        }
    }

    private boolean shouldRotateClockwise() {
        return AngleUtils.clockwiseDifference(AngleUtils.get360RotationYaw(start.left), target.left) < 180;
    }

    public void reset() {
        completed = false;
        rotating = false;
    }


    private void getDifference() {
        difference.setLeft(AngleUtils.smallestAngleDifference(AngleUtils.get360RotationYaw(), target.left));
        difference.setRight(target.right - start.right);
    }

    private float interpolate(float difference) {
        final float spentMillis = System.currentTimeMillis() - startTime;
        final float relativeProgress = spentMillis / (endTime - startTime);
        return (difference) * easeOutSine(relativeProgress);
    }

    private float easeOutCubic(double number) {
        return (float)(1.0 - Math.pow(1.0 - number, 3.0));
    }

    private float easeOutSine(double number) {
        return (float) Math.sin((number * Math.PI) / 2);
    }
}
