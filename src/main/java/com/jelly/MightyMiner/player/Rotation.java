package com.jelly.MightyMiner.player;

import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Tuple;
import net.minecraft.util.Vec3;
import org.apache.commons.lang3.tuple.MutablePair;
import static com.jelly.MightyMiner.utils.AngleUtils.*;

public class Rotation {
    private final static Minecraft mc = Minecraft.getMinecraft();
    public boolean rotating;
    public boolean completed;

    private long startTime; // all in ms
    private long endYawTime;
    private long endPitchTime;

    private double previousTime;

    private float phase;

    MutablePair<Float, Float> start = new MutablePair<>(0f, 0f);
    MutablePair<Float, Float> target = new MutablePair<>(0f, 0f);
    MutablePair<Float, Float> difference = new MutablePair<>(0f, 0f);

    public void easeTo(float yaw, long yawTime, float pitch, long pitchTime) {
        completed = false;
        rotating = true;
        startTime = System.currentTimeMillis();
        endYawTime = startTime + yawTime;
        endPitchTime = startTime + pitchTime;
        start.setLeft(mc.thePlayer.rotationYaw);
        start.setRight(mc.thePlayer.rotationPitch);
        target.setLeft(AngleUtils.getActualYawFrom360(yaw));
        target.setRight(pitch);
        getDifference();
    }

    public void easeTo(float yaw, float pitch, long time) {
        easeTo(yaw, time, pitch, time);
    }


    public void initAngleLock(BlockPos block, int time) {
        Tuple<Float, Float> angles = AngleUtils.getRotation(new Vec3(block.getX() + 0.5, block.getY() + 0.5, block.getZ() + 0.5));
        initAngleLock(angles.getFirst(), angles.getSecond(), time);
    }

    public void initAngleLock(Vec3 target, int time) {
        Tuple<Float, Float> angles = AngleUtils.getRotation(target);
        initAngleLock(angles.getFirst(), angles.getSecond(), time);
    }

    public void initAngleLock(float yaw, long yawTime, float pitch, long pitchTime) {
        float playerYaw = (float) Math.floor(mc.thePlayer.rotationYaw);
        float playerPitch = (float) Math.floor(mc.thePlayer.rotationPitch);
        float targetYaw = (float) Math.floor(yaw);
        float targetPitch = (float) Math.floor(pitch);

        // "real" means that its in 360 format instead of -180 to 180
        float realPlayerYaw = AngleUtils.get360RotationYaw(playerYaw);
        float realPlayerPitch = AngleUtils.get360RotationYaw(targetPitch);
        float realTargetYaw = AngleUtils.get360RotationYaw(targetYaw);
        float realTargetPitch = AngleUtils.get360RotationYaw(playerPitch);

        if (realPlayerYaw != realTargetYaw || realTargetPitch != realPlayerPitch) {
            if (!rotating) {
                easeTo(yaw, yawTime, pitch, pitchTime);
            }
        }
    }

    public void initAngleLock(float yaw, float pitch, int time) {
        initAngleLock(yaw, time, pitch, time);
    }


    public void updateInEllipse(float semi_minor_axis, float semi_major_axis, int blocksInFront, float rotationYawAxis, int rate){
        if(rotating) return;

        double deltaTime = (System.currentTimeMillis() - previousTime) / 1000.0f;
        previousTime = System.currentTimeMillis();

        if(deltaTime > 0.1)
            return;

        phase += ((rate + Math.random() * rate / 2.0f) * deltaTime);
        phase %= (2 * Math.PI);

        float x = (float) (semi_major_axis * Math.sin(phase));
        float y = (float) (semi_minor_axis * Math.cos(phase)) - 0.5f;

        float yaw = getYawFromParametricEquation(x, blocksInFront, rotationYawAxis);
        float pitch = getPitchFromParametricEquation(x, y, blocksInFront, rotationYawAxis);

        if(getAngleDifference(yaw, getActualRotationYaw()) > 15 || Math.abs(pitch - mc.thePlayer.rotationPitch) > 15){
            easeTo(yaw, pitch,
                    Math.max(getYawRotationTime(yaw, 45, 200, 300), getPitchRotationTime(pitch, 30, 200, 300)));
            return;
        }

        rotateInstantlyTo(yaw, pitch);
    }


    public void updateInLimacon(float radius, int blocksInFront, float rotationYawAxis, int rate){
        if(rotating) return;

        double deltaTime = (System.currentTimeMillis() - previousTime) / 1000.0f;
        previousTime = System.currentTimeMillis();

        if(deltaTime > 0.1)
            return;

        phase += (rate / 2.0f * deltaTime);
        phase %= (2 * Math.PI);


        // here we use parametric equations (We use a Limaçon here (https://en.wikipedia.org/wiki/Lima%C3%A7on))
        // E.g. circle -> x = rcost, y = rsint, t = phase

        // Limaçon polar form ->  r = b cos a
        // cartesian form -> (x^2 + y^2 - ax)^2 = b^2 (x^2 + y^2)
        // parametric form -> x = a/2 + b cos t + a/2 cos 2t, y = b sin t + a/2 sin 2t
        // one which works is b = 0.3, a = 1

        float x, y;

        float a = 1;
        float b = 0.35f;
        float k = radius + 0.5f; // scale factor which scales up the whole loop (+0.5f is just an approximation)
        float c = -1.35f; // translates the whole graph c units right (negative -> left)

        x = (float) (k * (a / 2.0 + b * Math.cos(phase) + a / 2.0 * Math.cos(2 * phase)) + c);
        y = (float) (k * (b * Math.sin(phase) + a / 2.0 * Math.sin(2 * phase)));

        //just need to input the corresponding parametric equations :)

        float yaw = getYawFromParametricEquation(x, blocksInFront, rotationYawAxis);
        float pitch = getPitchFromParametricEquation(x, y, blocksInFront, rotationYawAxis);

        if(getAngleDifference(yaw, getActualRotationYaw()) > 15 || Math.abs(pitch - mc.thePlayer.rotationPitch) > 15){
            easeTo(yaw, pitch,
                    Math.max(getYawRotationTime(yaw, 45, 200, 300), getPitchRotationTime(pitch, 30, 200, 300)));
            return;
        }
        rotateInstantlyTo(yaw, pitch);


    }

    public float getYawFromParametricEquation(float x, int blocksInFront, float rotationYawAxis) {
        return rotationYawAxis % 180 == 0 ?
                getRequiredYaw(x, blocksInFront * BlockUtils.getUnitZ(rotationYawAxis)) :
                getRequiredYaw(blocksInFront * BlockUtils.getUnitX(rotationYawAxis), x);
    }

    public float getPitchFromParametricEquation(float x, float y, int blocksInFront, float rotationYawAxis){
        return rotationYawAxis % 180 == 0 ?
                getRequiredPitch(x, y, blocksInFront * BlockUtils.getUnitZ(rotationYawAxis)) :
                getRequiredPitch(blocksInFront * BlockUtils.getUnitX(rotationYawAxis), y, x);
    }

    public void rotateInstantlyTo(float yaw, float pitch){
        float prevYaw = mc.thePlayer.rotationYaw;
        if(shouldRotateClockwise(prevYaw, yaw)){
            mc.thePlayer.rotationYaw += smallestAngleDifference(prevYaw, yaw);
        } else {
            mc.thePlayer.rotationYaw -= smallestAngleDifference(prevYaw, yaw);
        }
        mc.thePlayer.rotationPitch = pitch;

    }

    public void update() {
        if (System.currentTimeMillis() <= endYawTime) {
            mc.thePlayer.rotationYaw = shouldRotateClockwise(start.left, target.left)
                    ? start.left + interpolateYaw(difference.left) : start.left - interpolateYaw(difference.left);
        } else if(!completed){
            mc.thePlayer.rotationYaw = target.left;
            completed = true;
            rotating = false;
        }

        if(System.currentTimeMillis() <= endPitchTime){
            mc.thePlayer.rotationPitch = start.right + interpolatePitch(difference.right);
        } else if(!completed){
            mc.thePlayer.rotationPitch = start.right + difference.right;
            completed = true;
            rotating = false;
        }
    }



    public void reset() {
        completed = false;
        rotating = false;
    }


    private void getDifference() {
        difference.setLeft(AngleUtils.smallestAngleDifference(AngleUtils.get360RotationYaw(), target.left));
        difference.setRight(target.right - start.right);
    }

    private float interpolateYaw(float difference) {
        final float spentMillis = System.currentTimeMillis() - startTime;
        final float relativeProgress = spentMillis / (endYawTime - startTime);
        return (difference) * easeOutSine(relativeProgress);
    }
    private float interpolatePitch(float difference) {
        final float spentMillis = System.currentTimeMillis() - startTime;
        final float relativeProgress = spentMillis / (endPitchTime - startTime);
        return (difference) * easeOutSine(relativeProgress);
    }

    private float easeOutCubic(double number) {
        return (float)(1.0 - Math.pow(1.0 - number, 3.0));
    }

    private float easeOutSine(double number) {
        return (float) Math.sin((number * Math.PI) / 2);
    }
}
