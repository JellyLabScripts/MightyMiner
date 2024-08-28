package com.jelly.mightyminerv2.util;

import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;

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

  public static Optional<Entity> getEntityLookingAt() {
    return Optional.ofNullable(mc.objectMouseOver.entityHit);
  }
}
