package com.jelly.mightyminerv2.Util;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

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

  public static Optional<EntityPlayer> getCeanna() {
    return mc.theWorld.playerEntities.stream()
        .filter(entity -> entity.posX == 42.50 && entity.posY == 134.50 && entity.posZ == 22.50
            && !entity.getName().contains("Sentry") // Just Because; It should never happen
            && isNpc(entity))
        .findFirst();
  }

  public static Optional<Entity> getEntityLookingAt() {
    return Optional.ofNullable(mc.objectMouseOver.entityHit);
  }
}
