package com.jelly.MightyMinerV2.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.structure.StructureStrongholdPieces.ChestCorridor;

public class CommissionUtil {

  private static final Minecraft mc = Minecraft.getMinecraft();

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
