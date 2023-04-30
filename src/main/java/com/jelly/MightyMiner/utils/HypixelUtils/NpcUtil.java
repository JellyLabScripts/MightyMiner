package com.jelly.MightyMiner.utils.HypixelUtils;

import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.TablistUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StringUtils;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NpcUtil {


    public static boolean isNpc(Entity entity) {
        if (!(entity instanceof EntityOtherPlayerMP)) {
            return false;
        }
        return !TablistUtils.getTabListPlayersSkyblock().contains(entity.getName());
    }

    public static Entity getEntityCuttingOtherEntity(Entity e, Class<?> entityType) {
        List<Entity> possible = Minecraft.getMinecraft().theWorld.getEntitiesInAABBexcluding(e, e.getEntityBoundingBox().expand(0.3D, 2.0D, 0.3D), a -> {
            boolean flag1 = (!a.isDead && !a.equals(Minecraft.getMinecraft().thePlayer));
            boolean flag2 = !(a instanceof EntityArmorStand);
            boolean flag3 = !(a instanceof net.minecraft.entity.projectile.EntityFireball);
            boolean flag4 = !(a instanceof net.minecraft.entity.projectile.EntityFishHook);
            boolean flag5 = (entityType == null || entityType.isInstance(a));
            return flag1 && flag2 && flag3 && flag4 && flag5;
        });
        if (!possible.isEmpty())
            return Collections.min(possible, Comparator.comparing(e2 -> e2.getDistanceToEntity(e)));
        return null;
    }

    public static Entity getStandOfEntity(Entity e, Class<?> entityType) {
        List<Entity> possible = Minecraft.getMinecraft().theWorld.getEntitiesInAABBexcluding(e, e.getEntityBoundingBox().expand(0.3D, 2.0D, 0.3D), a -> {
            boolean flag1 = (!a.isDead && !a.equals(Minecraft.getMinecraft().thePlayer));
            boolean flag3 = !(a instanceof net.minecraft.entity.projectile.EntityFireball);
            boolean flag4 = !(a instanceof net.minecraft.entity.projectile.EntityFishHook);
            boolean flag5 = (entityType == null || entityType.isInstance(a));
            return flag1 && flag3 && flag4 && flag5;
        });
        if (!possible.isEmpty()) {
            return Collections.min(possible, Comparator.comparing(e2 -> e2.getDistanceToEntity(e)));
        }
        return null;
    }

    public static boolean entityIsTargeted(Entity entity) {
        int reach = 50;
        Minecraft mc = Minecraft.getMinecraft();
        Vec3 eyesPos = mc.thePlayer.getPositionEyes(1.0F);
        Vec3 lookVec = mc.thePlayer.getLook(1.0F);
        List<Entity> entityList = mc.theWorld.getEntitiesInAABBexcluding(mc.thePlayer, mc.thePlayer.getEntityBoundingBox().addCoord(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach).expand(1.0D, 1.0D, 1.0D), Entity::canBeCollidedWith);
        Entity entityMouseOver = null;
        for (Entity e : entityList) {
            AxisAlignedBB entityBoundingBox = e.getEntityBoundingBox().expand(0.3D, 0.3D, 0.3D);
            MovingObjectPosition movingObjectPosition = entityBoundingBox.calculateIntercept(eyesPos, eyesPos.addVector(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach));
            if (movingObjectPosition != null) {
                double distanceToEntity = eyesPos.distanceTo(movingObjectPosition.hitVec);
                if (distanceToEntity < reach) {
                    entityMouseOver = e;
                    reach = (int) distanceToEntity;
                }
            }
        }
        return entityMouseOver != null && entityMouseOver.equals(entity);
    }

    public static Entity getEntityLookingAt(int reach) {
        Minecraft mc = Minecraft.getMinecraft();
        Vec3 eyesPos = mc.thePlayer.getPositionEyes(1.0F);
        Vec3 lookVec = mc.thePlayer.getLook(1.0F);
        List<Entity> entityList = mc.theWorld.getEntitiesInAABBexcluding(mc.thePlayer,
                mc.thePlayer.getEntityBoundingBox().addCoord(lookVec.xCoord * reach,
                        lookVec.yCoord * reach,
                        lookVec.zCoord * reach).expand(1.0D, 1.0D, 1.0D),
                Entity::canBeCollidedWith);
        Entity entityMouseOver = null;
        for (Entity e : entityList) {
            AxisAlignedBB entityBoundingBox = e.getEntityBoundingBox().expand(0.3D, 0.3D, 0.3D);
            MovingObjectPosition movingObjectPosition = entityBoundingBox.calculateIntercept(eyesPos, eyesPos.addVector(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach));
            if (movingObjectPosition != null) {
                double distanceToEntity = eyesPos.distanceTo(movingObjectPosition.hitVec);
                if (distanceToEntity < reach) {
                    entityMouseOver = e;
                    reach = (int) distanceToEntity;
                }
            }
        }
        return entityMouseOver;
    }

    public static Entity getStandOfEntityLookingAt(int reach) {
        Minecraft mc = Minecraft.getMinecraft();
        Vec3 eyesPos = mc.thePlayer.getPositionEyes(1.0F);
        Vec3 lookVec = mc.thePlayer.getLook(1.0F);
        List<Entity> entityList = mc.theWorld.getEntitiesInAABBexcluding(mc.thePlayer,
                mc.thePlayer.getEntityBoundingBox().addCoord(lookVec.xCoord * reach,
                        lookVec.yCoord * reach,
                        lookVec.zCoord * reach).expand(1.0D, 1.0D, 1.0D),
                Entity::canBeCollidedWith);
        Entity entityMouseOver = null;
        for (Entity e : entityList) {
            AxisAlignedBB entityBoundingBox = e.getEntityBoundingBox().expand(0.3D, 0.3D, 0.3D);
            MovingObjectPosition movingObjectPosition = entityBoundingBox.calculateIntercept(eyesPos, eyesPos.addVector(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach));
            if (movingObjectPosition != null) {
                double distanceToEntity = eyesPos.distanceTo(movingObjectPosition.hitVec);
                if (distanceToEntity < reach) {
                    entityMouseOver = e;
                    reach = (int) distanceToEntity;
                }
            }
        }
        return entityMouseOver != null ? getStandOfEntity(entityMouseOver, EntityArmorStand.class) : null;
    }

    public static String stripString(String s) {
        char[] nonValidatedString = StringUtils.stripControlCodes(s).toCharArray();
        StringBuilder validated = new StringBuilder();
        for (char a : nonValidatedString) {
            if (a < '' && a > '\024')
                validated.append(a);
        }
        return validated.toString();
    }

    public static int getEntityHp(EntityArmorStand aStand) {
        double mobHp = -1.0D;
        Pattern pattern = Pattern.compile(".+?\\s([.\\d]+)[BMk]?/[.\\d]+[BMk]?.*");
        String stripped = StringUtils.stripControlCodes(aStand.getName());
        Matcher mat = pattern.matcher(stripped);
        if (mat.matches()) {
            try {
                mobHp = Double.parseDouble(mat.group(1));
                return (int)Math.ceil(mobHp);
            } catch (NumberFormatException ignored) {

            }
        }

        Pattern pattern2 = Pattern.compile(".+?\\s(\\d+)+[BMk]?.*");
        Matcher mat2 = pattern2.matcher(stripped);
        if (mat2.matches()) {
            try {
                mobHp = Double.parseDouble(mat2.group(1));
                return (int)Math.ceil(mobHp);
            } catch (NumberFormatException ignored) {

            }
        }
        return -1;
    }

    @SuppressWarnings("unused")
    private static final String[] npcNames = {
            "Golden Goblin",
            "Goblin",
            "Weakling",
            "Fireslinger",
            "Executive Viper",
            "Grunt",
            "Eliza",
            "Fraiser",
            "Wilson",
            "Ceanna",
            "Carlton",
            "Treasure Hoarder",
            "Team Treasuire",
            "Star Centry"
    };

}
