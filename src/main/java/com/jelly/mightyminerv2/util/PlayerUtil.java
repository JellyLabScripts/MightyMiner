package com.jelly.mightyminerv2.util;

import com.jelly.mightyminerv2.mixin.client.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class PlayerUtil {

  private static final Minecraft mc = Minecraft.getMinecraft();

  // requires more testing
  public static BlockPos getBlockStandingOn() {
    // 0.25 = 3 layers of snow
    // if there is more than 3 layers of snow then i should consider that as a full block i guess
    // but there is no snow check in pathfinder so this will probably not work at all in snowy areas
    return new BlockPos(mc.thePlayer.posX, Math.ceil(mc.thePlayer.posY - 0.25) - 1, mc.thePlayer.posZ);
  }

  public static Vec3 getPlayerEyePos() {
    return mc.thePlayer.getPositionEyes(((MinecraftAccessor) mc).getTimer().renderPartialTicks);
  }

  public static BlockPos getBlockStandingOnFloor() {
    return new BlockPos(mc.thePlayer.posX, Math.floor(mc.thePlayer.posY) - 1, mc.thePlayer.posZ);
  }

  public static Vec3 getNextTickPosition() {
    return mc.thePlayer.getPositionVector().addVector(mc.thePlayer.motionX, 0, mc.thePlayer.motionZ);
  }

  public static Vec3 getNextTickPosition(float mult) {
    return mc.thePlayer.getPositionVector().addVector(mc.thePlayer.motionX * mult, 0, mc.thePlayer.motionZ * mult);
  }

  public static Entity getEntityCuttingOtherEntity(Entity e) {
    return getEntityCuttingOtherEntity(e, entity -> true);
  }

  public static Entity getEntityCuttingOtherEntity(Entity e, Predicate<Entity> predicate) {
    List<Entity> possible = mc.theWorld.getEntitiesInAABBexcluding(e, e.getEntityBoundingBox().expand(0.3D, 2.0D, 0.3D), a -> {
      boolean flag1 = (!a.isDead && !a.equals(mc.thePlayer));
      boolean flag2 = !(a instanceof net.minecraft.entity.projectile.EntityFireball);
      boolean flag3 = !(a instanceof net.minecraft.entity.projectile.EntityFishHook);
      boolean flag4 = predicate.test(a);
      return flag1 && flag2 && flag3 && flag4;
    });
    if (!possible.isEmpty()) {
      return Collections.min(possible, Comparator.comparing(e2 -> e2.getDistanceToEntity(e)));
    }
    return null;
  }

  public static boolean isPlayerSuffocating() {
    AxisAlignedBB playerBB = mc.thePlayer.getEntityBoundingBox().expand(-0.15, -0.15, -0.15);
    List<AxisAlignedBB> collidingBoxes = mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, playerBB);
    return !collidingBoxes.isEmpty();
  }

  public static EnumFacing getHorizontalFacing(float yaw) {
    return EnumFacing.getHorizontal(MathHelper.floor_double((double) (yaw * 4.0F / 360.0F) + 0.5) & 3);
  }
}
