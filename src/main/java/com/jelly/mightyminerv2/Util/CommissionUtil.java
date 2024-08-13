package com.jelly.mightyminerv2.Util;

import com.google.common.collect.ImmutableMap;
import com.jelly.mightyminerv2.Macro.commissionmacro.helper.Commission;
import com.jelly.mightyminerv2.Util.helper.Angle;
import com.jelly.mightyminerv2.Util.helper.heap.HeapNode;
import com.jelly.mightyminerv2.Util.helper.heap.MinHeap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.text.html.parser.Entity;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;

public class CommissionUtil {

  private static final Map<Commission, String> slayerMob = ImmutableMap.of(
      Commission.GOBLIN_SLAYER, "Goblin",
      Commission.GLACITE_WALKER_SLAYER, "Ice Walker",
      Commission.TREASURE_HOARDER_SLAYER, "Treasure Hoarder"
  );

  private static final Minecraft mc = Minecraft.getMinecraft();

  public static Commission getCurrentCommission() {
    Commission comm = null;
    boolean foundCommission = false;
    for (final String text : TablistUtil.getCachedTablist()) {
      if (!foundCommission) {
        if (text.equalsIgnoreCase("Commissions:")) {
          foundCommission = true;
        }
        continue;
      }

      if (comm == null) {
        comm = Commission.getCommission(text.split(": ")[0].trim());
      }

      if (text.contains("DONE")) {
        comm = Commission.COMMISSION_CLAIM;
        break;
      }

      if (text.isEmpty()) {
        break;
      }
    }
    return comm;
  }

  public static int getClaimableCommissionSlot() {
    if (!(mc.thePlayer.openContainer instanceof ContainerChest)) {
      return -1;
    }
    final ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
    for (int i = 0; i < chest.getLowerChestInventory().getSizeInventory(); i++) {
      final ItemStack stack = chest.getLowerChestInventory().getStackInSlot(i);
      if (stack == null || stack.getItem() == null) {
        continue;
      }
      for (final String lore : InventoryUtil.getItemLore(stack)) {
        if (lore.equalsIgnoreCase("completed")) {
          return i;
        }
      }
    }
    return -1;
  }

  public static List<EntityPlayer> getCommissionMobs(Commission commission) {
    String mobName;
    if ((mobName = slayerMob.get(commission)) == null) {
      return new ArrayList<>();
    }
    List<EntityPlayer> mobs = new ArrayList<>();
    for (EntityPlayer mob : mc.theWorld.playerEntities) {
      if (mob.getName().equals(mobName)) {
        mobs.add(mob);
      }
    }

    Angle playerAngle = new Angle(AngleUtil.get360RotationYaw(), 0);
    Vec3 playerPos = mc.thePlayer.getPositionVector();
    mobs.sort(Comparator.comparingDouble(mob ->
        mob.getPositionVector().distanceTo(playerPos)
            + AngleUtil.getNeededChange(playerAngle, AngleUtil.getRotation(mob)).yaw)
    );
    return mobs;
  }

  // right now i have no use for a list of mobs so this should do it
  public static EntityPlayer getBestMob(String mobName, EntityPlayer mobToIgnore) {
    if (!slayerMob.containsValue(mobName)) {
      return null;
    }
    MinHeap<EntityPlayer> mobs = new MinHeap<>(100);
    Vec3 playerPos = mc.thePlayer.getPositionVector();
    for (EntityPlayer mob : mc.theWorld.playerEntities) {
      if (mob.getName().equals(mobName) && mob.isEntityAlive() && (mobToIgnore == null || !mobToIgnore.equals(mob))) {
        mobs.add(new HeapNode<>(mob, mob.getPositionVector().distanceTo(playerPos) + Math.abs(AngleUtil.normalizeAngle(mc.thePlayer.rotationYaw) - AngleUtil.getRotation(mob).yaw) * 0.5));
      }
    }

    return mobs.poll();
  }
}
