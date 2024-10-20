package com.jelly.mightyminerv2.util;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.MobTracker;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

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

  public static BlockPos getBlockStandingOn(Entity entity){
    return new BlockPos(entity.posX, Math.ceil(entity.posY - 0.25) - 1, entity.posZ);
  }

  public static Optional<Entity> getEntityLookingAt() {
    return Optional.ofNullable(mc.objectMouseOver.entityHit);
  }

  public static boolean isStandDead(String name) {
    return getHealthFromStandName(name) == 0;
  }

  public static String getEntityNameFromArmorStand(String armorstandName) {
    char[] carr = armorstandName.toCharArray();
    if (carr.length == 0 || carr[carr.length - 1] != '❤') {
      return "";
    }
    StringBuilder builder = new StringBuilder();
    boolean foundSpace = false;
    boolean foundDigit = false;
    byte charCounter = 0;
    for (int i = carr.length - 1; i >= 0; i--) {
      char curr = carr[i];
      if (!foundDigit) {
        foundDigit = Character.isDigit(curr);
        continue;
      }
      if (!foundSpace) {
        foundSpace = curr == ' ';
        continue;
      }
      if (curr == '§') {
        charCounter++;
        if (charCounter == 2) {
          builder.deleteCharAt(builder.length() - 1);
          break;
        }
        continue;
      }
      if (charCounter == 1) {
        builder.append(curr);
      }
    }
    return builder.reverse().toString();
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

  public static List<EntityLivingBase> getEntities(Set<String> entityNames, Set<EntityLivingBase> entitiesToIgnore) {
    List<EntityLivingBase> entities = new ArrayList<>();
    entityNames.forEach(it -> {
      Set<EntityLivingBase> set = MobTracker.getInstance().getEntity(it);
      entitiesToIgnore.forEach(set::remove);
      entities.addAll(set);
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

//  public Pair<Integer, Integer> unpack(long packed) {
//    return new Pair<>((int) (packed >> 32), (int) packed);
//  }
}
