package com.jelly.MightyMiner.utils.HypixelUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NpcUtil {


    /**
     * CREDIT: <a href="https://github.com/BiscuitDevelopment/SkyblockAddons/blob/7334393cc39010a37ebcb41f0beef5fc4bc7f447/src/main/java/codes/biscuit/skyblockaddons/core/npc/NPCUtils.java">...</a>
     * Checks if the given entity is an NPC
     *
     * @param entity the entity to check
     * @return {@code true} if the entity is an NPC, {@code false} otherwise
     */
    public static boolean isNpc(Entity entity) {
        if (!(entity instanceof EntityOtherPlayerMP)) {
            return false;
        }

        EntityLivingBase entityLivingBase = (EntityLivingBase) entity;

        return (entity.getUniqueID().version() == 2 && entityLivingBase.getHealth() == 20.0F && !entityLivingBase.isPlayerSleeping() && !entity.isInvisible()) ||
                // Watchdog NPC probably
                (entity.getUniqueID().version() == 4 && !entityLivingBase.isPlayerSleeping() && entity.isInvisible() && (entity.getEntityId() == (Minecraft.getMinecraft().thePlayer.getEntityId() + 1)));
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
        Pattern pattern = Pattern.compile(".+? ([.\\d]+)[BMk]?/[.\\d]+[BMk]?");
        String stripped = stripString(aStand.getName());
        Matcher mat = pattern.matcher(stripped);
        if (mat.matches())
            try {
                mobHp = Double.parseDouble(mat.group(1));
            } catch (NumberFormatException ignored) {

            }
        else {
            pattern = Pattern.compile("\\[Lv(\\d+)]\\s+(\\w+)\\s+(\\d+)+[BMk]?");
            stripped = stripString(aStand.getName());
            mat = pattern.matcher(stripped);
            if (mat.matches())
                try {
                    mobHp = Double.parseDouble(mat.group(3));
                } catch (NumberFormatException ignored) {

                }
        }
        return (int)Math.ceil(mobHp);
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
