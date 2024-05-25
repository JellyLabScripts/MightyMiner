package com.jelly.MightyMinerV2.Util;

import com.jelly.MightyMinerV2.Macro.impl.commissionmacro.helper.Commission;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.item.ItemStack;

public class CommissionUtil {

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

}
