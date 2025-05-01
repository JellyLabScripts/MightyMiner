package com.jelly.mightyminerv2.util;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.pathfinder.helper.BlockStateAccessor;
import com.jelly.mightyminerv2.pathfinder.movement.MovementHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.*;
import java.util.stream.Collectors;

public class EntityUtil {

    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean isNpc(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (!(entity instanceof EntityOtherPlayerMP)) {
            return false;
        }
        return !TablistUtil.getTabListPlayersSkyblock().contains(entity.getName());
    }

    public static BlockPos getBlockStandingOn(Entity entity) {
        return new BlockPos(entity.posX, Math.ceil(entity.posY - 0.25) - 1, entity.posZ);
    }

    public static Optional<Entity> getEntityLookingAt() {
        return Optional.ofNullable(mc.objectMouseOver.entityHit);
    }

    public static boolean isStandDead(String name) {
        return getHealthFromStandName(name) == 0;
    }

    public static int getHealthFromStandName(String name) {
        int health = 0;
        try {
            String[] arr = name.split(" ");
            health = Integer.parseInt(arr[arr.length - 1].split("/")[0].replace(",", ""));
        } catch (Exception ignored) {
        }
        return health;
    }

    public static Entity getEntityCuttingOtherEntity(Entity e, Class<?> entityType) {
        List<Entity> possible = mc.theWorld.getEntitiesInAABBexcluding(e, e.getEntityBoundingBox().expand(0.3D, 2.0D, 0.3D), a -> {
            boolean flag1 = (!a.isDead && !a.equals(mc.thePlayer));
            boolean flag2 = !(a instanceof EntityArmorStand);
            boolean flag3 = !(a instanceof net.minecraft.entity.projectile.EntityFireball);
            boolean flag4 = !(a instanceof net.minecraft.entity.projectile.EntityFishHook);
            boolean flag5 = (entityType == null || entityType.isInstance(a));
            return flag1 && flag2 && flag3 && flag4 && flag5;
        });
        if (!possible.isEmpty()) return Collections.min(possible, Comparator.comparing(e2 -> e2.getDistanceToEntity(e)));
        return null;
    }

    public static List<EntityLivingBase> getEntities(Set<String> entityNames, Set<EntityLivingBase> entitiesToIgnore) {
        List<EntityLivingBase> entities = new ArrayList<>();
        mc.theWorld.loadedEntityList.stream()
            .filter(entity -> entity instanceof EntityArmorStand)
            .filter((v) ->
                    !v.getName().contains(mc.thePlayer.getName()) && !v.isDead &&
                    entityNames.stream().anyMatch((a) -> v.getCustomNameTag().contains(a)) &&
                    ((EntityLivingBase) v).getHealth() > 0)
            .collect(Collectors.toList()).forEach((entity) -> {
                Entity livingBase = getEntityCuttingOtherEntity(entity, null);
                if (livingBase instanceof EntityLivingBase) {
                    if (!entitiesToIgnore.contains((EntityLivingBase) livingBase)) {
                    entities.add((EntityLivingBase) livingBase);
                    }
                }
            });

        Vec3 playerPos = mc.thePlayer.getPositionVector();
        float normalizedYaw = AngleUtil.normalizeAngle(mc.thePlayer.rotationYaw);
        return entities.stream()
                .filter(EntityLivingBase::isEntityAlive)
                .sorted(Comparator.comparingDouble(ent -> {
                            Vec3 entPos = ent.getPositionVector();
                            double distanceCost = playerPos.distanceTo(entPos);
                            double angleCost = Math.abs(AngleUtil.getNeededYawChange(normalizedYaw, AngleUtil.getRotationYaw(entPos)));
                            return distanceCost * ((float) MightyMinerConfig.devMKillDist / 100f) + angleCost * ((float) MightyMinerConfig.devMKillRot / 100f);
                        }
                )).collect(Collectors.toList());
    }

    private static long pack(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }


    public static BlockPos nearbyBlock(EntityLivingBase entityLivingBase) {
        BlockPos closestBlock = null;
        double closestDistance = Double.MAX_VALUE;
        BlockStateAccessor bsa = new BlockStateAccessor(mc.theWorld);

        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos currentPos = entityLivingBase.getPosition().add(x, y, z);

                    if (MovementHelper.INSTANCE.canStandOn(
                            bsa,
                            currentPos.getX(),
                            currentPos.getY(),
                            currentPos.getZ(),
                            bsa.get(currentPos.getX(), currentPos.getY(), currentPos.getZ())
                    ) && RaytracingUtil.canSeePoint(new Vec3(currentPos), entityLivingBase.getPositionEyes(1.0F))) {
                        double distance = currentPos.distanceSq(PlayerUtil.getBlockStandingOn());

                        if (distance < closestDistance) {
                            closestBlock = currentPos;
                            closestDistance = distance;
                        }
                    }
                }
            }
        }

        if (closestBlock == null) {
            return getBlockStandingOn(entityLivingBase);
        }

        return closestBlock;
    }

}
