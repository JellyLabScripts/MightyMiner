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


    public void updateInCircle(float radius, int blocksInFront, float rotationYawAxis, int rate){

       // if(mc.thePlayer.lastTickPosY - mc.thePlayer.posY == 0) { // it will glitch if the player is falling / dropping

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

        float a = 1;
        float b = 0.35f;
        float k = radius + 0.5f; // scale factor which scales up the whole loop (+0.5f is just an approximation)
        float c = -1.35f; // translates the whole graph c units right (negative -> left)

        float p = (float) (k * (a/2.0 + b * Math.cos(phase) + a/2.0 * Math.cos(2 * phase)) + c);
        float q = (float) (k * (b * Math.sin(phase) + a/2.0 * Math.sin(2 * phase)));

        //just need to input the corresponding parametric equations :)

        //Dürer folium also works
        //double p = k * (Math.cos(phase) + Math.cos(3*phase));
        //double q = k / 1.5f * (Math.sin(phase) + Math.sin(3*phase));

        float yaw;
        float pitch;
        if (rotationYawAxis % 180 == 0) {
            yaw = getRequiredYaw(p, blocksInFront * BlockUtils.getUnitZ(rotationYawAxis));
            pitch = getRequiredPitch(p, q, blocksInFront * BlockUtils.getUnitZ(rotationYawAxis));
        } else {
            yaw = getRequiredYaw(blocksInFront * BlockUtils.getUnitX(rotationYawAxis), p);
            pitch = getRequiredPitch(blocksInFront * BlockUtils.getUnitX(rotationYawAxis), q, p);
        }

        if(getAngleDifference(yaw, getActualRotationYaw()) > 15 || Math.abs(pitch - mc.thePlayer.rotationPitch) > 15){
            easeTo(yaw, pitch, 200);
            return;
        }
        rotateInstantlyTo(yaw, pitch);


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
