package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.Logger;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.network.play.server.S30PacketWindowItems;

public class ItemChangeFailsafe extends AbstractFailsafe {

  private static final ItemChangeFailsafe instance = new ItemChangeFailsafe();

  public static ItemChangeFailsafe getInstance() {
    return instance;
  }

  private ItemStack lastHeldItem;

  @Override
  public String getName() {
    return "ItemChangeFailsafe";
  }

  @Override
  public int getPriority() {
    return 5;
  }

  @Override
  public boolean onPacketReceive(PacketEvent.Received event) {
    if (event.packet instanceof S2FPacketSetSlot) {
      S2FPacketSetSlot packet = (S2FPacketSetSlot) event.packet;

      int slot = packet.func_149173_d();
      if (slot >= 36 && slot <= 44) {
        ItemStack newHeldItem = packet.func_149174_e();

        if (!ItemStack.areItemStacksEqual(lastHeldItem, newHeldItem)) {
          lastHeldItem = newHeldItem;
          return true;
        }
      }
    } else if (event.packet instanceof S30PacketWindowItems) {
      S30PacketWindowItems packet = (S30PacketWindowItems) event.packet;

      Container container = Minecraft.getMinecraft().thePlayer.openContainer;

      for (int i = 36; i <= 44; i++) {
        ItemStack newHeldItem = packet.getItemStacks()[i];

        if (!ItemStack.areItemStacksEqual(lastHeldItem, newHeldItem)) {
          lastHeldItem = newHeldItem;
          return true;
        }
      }
    }

    return false;
  }

  public void react() {
    MacroManager.getInstance().disable();
    Logger.sendWarning("Your item has been changed! Disabeling macro.");
  }
}
