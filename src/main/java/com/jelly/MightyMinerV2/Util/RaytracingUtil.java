package com.jelly.MightyMinerV2.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import java.util.List;

public class RaytracingUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean canSeePoint(Vec3 point) {
        final MovingObjectPosition result = raytrace(mc.thePlayer.getPositionEyes(1), point);
        if (result == null || result.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return false;
        final Vec3 r = result.hitVec;
        return Math.abs(r.xCoord - point.xCoord) < 0.1f && Math.abs(r.yCoord - point.yCoord) < 0.1f && Math.abs(r.zCoord - point.zCoord) < 0.1f;
    }

    // Credits to GumTuneClient <3
    public static MovingObjectPosition raytrace(Vec3 v1, Vec3 v2) {
        final Vec3 v3 = v2.subtract(v1);
        final List<Entity> entities = mc.theWorld.getEntitiesInAABBexcluding(
                mc.thePlayer,
                mc.thePlayer.getEntityBoundingBox().addCoord(v3.xCoord, v3.yCoord, v3.zCoord).expand(1.0, 1.0, 1.0)
                , it -> it.isEntityAlive() && it.canBeCollidedWith());
        for (Entity entity : entities) {
            final MovingObjectPosition intercept = entity.getEntityBoundingBox().expand(0.5, 0.5, 0.5).calculateIntercept(v1, v2);
            if (intercept != null) {
                return new MovingObjectPosition(entity, intercept.hitVec);
            }
        }
        return mc.theWorld.rayTraceBlocks(v1, v2, false, true, false);
    }
}
