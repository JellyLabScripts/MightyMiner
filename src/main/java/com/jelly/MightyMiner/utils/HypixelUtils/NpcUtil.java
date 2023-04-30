package com.jelly.MightyMiner.utils.HypixelUtils;

import com.jelly.MightyMiner.utils.TablistUtils;
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

    private static final Pattern healthPattern = Pattern.compile("(?:§8[§7Lv(\\d)§8])?\\s*(?:§c)?(.+)(?:§r)? §[ae]([\\dBMk]+)§c❤");
    private static final Pattern healthPattern2 = Pattern.compile("(?:§8[§7Lv(\\d)§8])?\\s*(?:§c)?(.+)(?:§r) §[ae]([\\dBMk]+)§f/§[ae]([\\dBMk]+)§c❤");


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

    public static String stripString(String s) {
        char[] nonValidatedString = StringUtils.stripControlCodes(s).toCharArray();
        StringBuilder validated = new StringBuilder();
        for (char a : nonValidatedString) {
            if (a < '' && a > '\024')
                validated.append(a);
        }
        return validated.toString();
    }

    public static int getEntityHp(Entity entity) {
        if (entity instanceof EntityArmorStand) {
            String name = entity.getCustomNameTag();
            if (name.contains("❤")) {
                Matcher matcher = healthPattern.matcher(name);
                Matcher matcher2 = healthPattern2.matcher(name);
                System.out.println(name);
                if (matcher.find() || matcher2.find()) {
                    String hp = matcher.find() ? matcher.group(2) : matcher2.group(2);
                    int modifer = 1;
                    if (name.contains("k§c❤")) {
                        modifer = 1000;
                    } else if (name.contains("M§c❤")) {
                        modifer = 1000000;
                    } else if (name.contains("B§c❤")) {
                        modifer = 1000000000;
                    }
                    System.out.println(hp);
                    return (int) (Double.parseDouble(hp.replace("k", "").replace("M", "").replace("B", "")) * modifer);
                }
            }
        } else if (entity instanceof EntityLivingBase) {
            System.out.println(((EntityLivingBase) entity).getHealth());
            return (int) ((EntityLivingBase) entity).getHealth();
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
