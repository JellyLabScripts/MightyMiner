package com.jelly.MightyMiner.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

public class AngleUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static float get360RotationYaw(float yaw) {
        return (yaw % 360 + 360) % 360;
    }

    public static float get360RotationYaw() {
        return get360RotationYaw(mc.thePlayer.rotationYaw);
    }

    public static float clockwiseDifference(float initialYaw360, float targetYaw360) {
        return get360RotationYaw(targetYaw360 - initialYaw360);
    }

    public static float antiClockwiseDifference(float initialYaw360, float targetYaw360) {
        return get360RotationYaw(initialYaw360 - targetYaw360);
    }

    public static float smallestAngleDifference(float initialYaw360, float targetYaw360) {
        return Math.min(clockwiseDifference(initialYaw360, targetYaw360), antiClockwiseDifference(initialYaw360, targetYaw360));
    }

    public static float getClosest() {
        if (get360RotationYaw() < 45 || get360RotationYaw() > 315) {
            return 0f;
        } else if (get360RotationYaw() < 135) {
            return 90f;
        } else if (get360RotationYaw() < 225) {
            return 180f;
        } else {
            return 270f;
        }
    }
    public static float getClosest(float yaw) {
        if (yaw < 45 || yaw > 315) {
            return 0f;
        } else if (yaw < 135) {
            return 90f;
        } else if (yaw < 225) {
            return 180f;
        } else {
            return 270f;
        }
    }
    public static double getRequiredYaw(BlockPos blockLookingAt) {
        double deltaX = blockLookingAt.getX() + 0.5d -  mc.thePlayer.posX;
        double deltaZ = blockLookingAt.getZ() + 0.5d - mc.thePlayer.posZ ;
        return  (Math.atan(-deltaX / deltaZ) * 180 / Math.PI) + ((deltaX > 0 && deltaZ < 0) ? -180 : 0) +
                ((deltaX < 0 && deltaZ < 0) ? 180 : 0);
    }
    public static double getRequiredPitch(BlockPos blockLookingAt) {
        double deltaY = (blockLookingAt.getY() + 0.5d) - (mc.thePlayer.posY + 1.62d);
        double deltaDis = MathUtils.getDistanceBetweenTwoPoints(
                mc.thePlayer.posX, mc.thePlayer.posY + 1.62d, mc.thePlayer.posZ,
                blockLookingAt.getX() + 0.5d, blockLookingAt.getY() + 0.5d, blockLookingAt.getZ() + 0.5d);
        return  -(Math.asin(deltaY / deltaDis) * 180 / Math.PI);
    }
    public static int getRelativeYawFromBlockPos(BlockPos facingBlockPos) {
        if (onTheSameXZ(BlockUtils.getRelativeBlockPos(1, 0), facingBlockPos)) {
            return 90;
        } else if (onTheSameXZ(BlockUtils.getRelativeBlockPos(-1, 0), facingBlockPos)) {
            return -90;
        } else if (onTheSameXZ(BlockUtils.getRelativeBlockPos(0, 1), facingBlockPos)) {
            return 0;
        } else if (onTheSameXZ(BlockUtils.getRelativeBlockPos(0, -1), facingBlockPos)) {
            return 180;
        }
        return -1;

    }
    public static boolean onTheSameXZ (BlockPos b1, BlockPos b2) {
        return b1.getX() == b2.getX() && b1.getZ() == b2.getZ();

    }
}
