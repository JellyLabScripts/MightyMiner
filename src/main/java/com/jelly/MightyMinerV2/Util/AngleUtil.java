package com.jelly.MightyMinerV2.Util;

import com.jelly.MightyMinerV2.Mixin.Client.MinecraftAccessor;
import com.jelly.MightyMinerV2.Util.helper.Angle;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class AngleUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static Angle getPlayerAngle() {
        return new Angle(get360RotationYaw(), mc.thePlayer.rotationPitch);
    }

    public static float get360RotationYaw(float yaw) {
        return (yaw % 360 + 360) % 360;
    }

    // This is MathHelper::wrapAngleTo180_float
    public static float normalizeAngle(float yaw) {
        float newYaw = yaw % 360F;
        if (newYaw < -180F) {
            newYaw += 360F;
        }
        if (newYaw > 180F) {
            newYaw -= 360F;
        }
        return newYaw;
    }

    public static float get360RotationYaw() {
        if (mc.thePlayer == null)
            return 0;
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

    public static Vec3 getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - 3.1415927F);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - 3.1415927F);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3(f1 * f2, f3, f * f2);
    }

    public static float getClosestDiagonal() {
        return getClosestDiagonal(get360RotationYaw());
    }

    public static float getClosestDiagonal(float yaw) {
        return ((int) get360RotationYaw(yaw) / 90) * 90 + 45;
    }

    public static float getClosest30() {
        if (get360RotationYaw() < 45) {
            return 30f;
        } else if (get360RotationYaw() < 90) {
            return 60f;
        } else if (get360RotationYaw() < 135) {
            return 120f;
        } else if (get360RotationYaw() < 180) {
            return 150f;
        } else if (get360RotationYaw() < 225) {
            return 210f;
        } else if (get360RotationYaw() < 270) {
            return 240f;
        } else if (get360RotationYaw() < 315) {
            return 300f;
        } else {
            return 330f;
        }
    }

    public static float getClosest45(float inputAngle) {
        return (int) (get360RotationYaw(inputAngle + 22.5f) / 45) * 45;
    }

    public static float getClosest() {
        return getClosest(get360RotationYaw());
    }

    public static float getClosest(float yaw) {
        return ((int) (get360RotationYaw(yaw + 45) / 90)) * 90;
    }

    private static final double randomAddition = (Math.random() * 0.3 - 0.15);

    public static Angle getRotation(Vec3 to) {
        return getRotation(mc.thePlayer.getPositionEyes(((MinecraftAccessor) mc).getTimer().renderPartialTicks), to, false);
    }

    public static Angle getRotation(Vec3 to, boolean randomness) {
        return getRotation(mc.thePlayer.getPositionEyes(((MinecraftAccessor) mc).getTimer().renderPartialTicks), to, randomness);
    }

    public static Angle getRotation(Entity to) {
        return getRotation(mc.thePlayer.getPositionEyes(((MinecraftAccessor) mc).getTimer().renderPartialTicks), to.getPositionVector().addVector(0, Math.min(((to.height * 0.85) + randomAddition), 1.7), 0), false);
    }

    public static Angle getRotation(BlockPos pos) {
        return getRotation(mc.thePlayer.getPositionEyes(((MinecraftAccessor) mc).getTimer().renderPartialTicks), new Vec3(pos).addVector(0.5, 0.5, 0.5), false);
    }

    public static Angle getRotation(BlockPos pos, boolean randomness) {
        return getRotation(mc.thePlayer.getPositionEyes(((MinecraftAccessor) mc).getTimer().renderPartialTicks), new Vec3(pos).addVector(0.5, 0.5, 0.5), randomness);
    }

    public static Angle getRotation(Vec3 from, Vec3 to, boolean randomness) {
        double xDiff = to.xCoord - from.xCoord;
        double yDiff = to.yCoord - from.yCoord;
        double zDiff = to.zCoord - from.zCoord;

        double dist = Math.sqrt(xDiff * xDiff + zDiff * zDiff);

        float yaw = (float) Math.toDegrees(Math.atan2(zDiff, xDiff)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(yDiff, dist));

        if (randomness) {
            yaw += (float) ((Math.random() - 1) * 4);
            pitch += (float) ((Math.random() - 1) * 4);
        }

        return new Angle(yaw, pitch);
    }

    // Todo: More testing
    public static Angle getNeededChange(Angle startAngle, Angle endAngle) {
        float yawChange = normalizeAngle(normalizeAngle(endAngle.getYaw()) - normalizeAngle(startAngle.getYaw()));
        return new Angle(yawChange, endAngle.getPitch() - startAngle.getPitch());
    }

    public static boolean isLookingAt(Vec3 vec, float distance) {
        Angle change = getNeededChange(getPlayerAngle(), getRotation(vec));
        return Math.abs(change.getYaw()) <= distance && Math.abs(change.getPitch()) <= distance;
    }
}
