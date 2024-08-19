package com.jelly.mightyminerv2.Util;

import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InventoryUtil {

  private static final Minecraft mc = Minecraft.getMinecraft();

  public static boolean holdItem(String item) {
    int slot = getHotbarSlotOfItem(item);
    if (slot == -1) {
      return false;
    }
    mc.thePlayer.inventory.currentItem = slot;
    return true;
  }

  public static int getSlotIdOfItemInContainer(String item) {
    return getSlotIdOfItemInContainer(item, false);
  }

  public static int getSlotIdOfItemInContainer(String item, boolean equals) {
    Slot slot = getSlotOfItemInContainer(item, equals);
    return slot != null ? slot.slotNumber : -1;
  }

  public static Slot getSlotOfItemInContainer(String item) {
    return getSlotOfItemInContainer(item, false);
  }

  public static Slot getSlotOfItemInContainer(String item, boolean equals) {
    for (Slot slot : mc.thePlayer.openContainer.inventorySlots) {
      if (slot.getHasStack()) {
        String itemName = StringUtils.stripControlCodes(slot.getStack().getDisplayName());
        if (equals) {
          if (itemName.equalsIgnoreCase(item)) {
            return slot;
          }
        } else {
          if (itemName.contains(item)) {
            return slot;
          }
        }
      }
    }
    return null;
  }

  public static int getHotbarSlotOfItem(String items) {
    for (int i = 0; i < 9; i++) {
      ItemStack slot = mc.thePlayer.inventory.getStackInSlot(i);
      if (slot == null || !slot.hasDisplayName()) {
        continue;
      }

      if (slot.getDisplayName().contains(items)) {
        return i;
      }
    }
    return -1;
  }

  public static boolean areItemsInInventory(List<String> items) {
    List<String> itemsToFind = new ArrayList<>(items);
    for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
      if (stack != null && stack.hasDisplayName()) {
        itemsToFind.removeIf(it -> stack.getDisplayName().contains(it));
      }
    }
    return itemsToFind.isEmpty();
  }

  public static boolean areItemsInHotbar(List<String> items) {
    List<String> itemsToFind = new ArrayList<>(items);
    for (int i = 0; i < 8; i++) {
      ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
      if (stack != null && stack.hasDisplayName()) {
        itemsToFind.removeIf(it -> stack.getDisplayName().contains(it));
      }
    }
    return itemsToFind.isEmpty();
  }

  // returns the items that arent in hotbar and slots that items can be moved into
  public static Pair<List<Integer>, List<String>> getAvailableHotbarSlots(List<String> items) {
    List<String> itemsToMove = new ArrayList<>(items);
    List<Integer> slotsToMoveTo = new ArrayList<>();

    for (int i = 0; i < 8; i++) {
      ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);

      if (stack == null || !stack.hasDisplayName()) {
        slotsToMoveTo.add(i);
      } else if (!itemsToMove.removeIf(item -> stack.getDisplayName().contains(item))) {
        slotsToMoveTo.add(i);
      }

      if (itemsToMove.isEmpty()) {
        break;
      }
    }

    return new Pair<>(slotsToMoveTo, itemsToMove);
  }

  public static String getInventoryName(Container container) {
    if (container instanceof ContainerChest) {
      IInventory inv = ((ContainerChest) container).getLowerChestInventory();
      return inv != null && inv.hasCustomName() ? inv.getName() : "";
    }
    return "";
  }

  public static String getInventoryName() {
    return getInventoryName(mc.thePlayer.openContainer);
  }

  public static void clickContainerSlot(int slot, ClickType mouseButton, ClickMode mode) {
    clickContainerSlot(slot, mouseButton.ordinal(), mode.ordinal());
  }

  public static void clickContainerSlot(int slot, int mouseButton, ClickMode mode) {
    clickContainerSlot(slot, mouseButton, mode.ordinal());
  }

  public static void clickContainerSlot(int slot, int mouseButton, int clickMode) {
    mc.playerController.windowClick(mc.thePlayer.openContainer.windowId, slot, mouseButton, clickMode, mc.thePlayer);
  }

  public static void swapSlots(int slot, int hotbarSlot) {
    mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, hotbarSlot, 2, mc.thePlayer);
  }

  public static void openInventory() {
    KeyBinding.onTick(mc.gameSettings.keyBindInventory.getKeyCode());
  }

  public static void closeScreen() {
    if (mc.currentScreen != null && mc.thePlayer != null) {
      mc.thePlayer.closeScreen();
    }
  }

  public static ArrayList<String> getItemLore(ItemStack itemStack) {
    NBTTagList loreTag = itemStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
    ArrayList<String> loreList = new ArrayList<>();
    for (int i = 0; i < loreTag.tagCount(); i++) {
      loreList.add(StringUtils.stripControlCodes(loreTag.getStringTagAt(i)));
    }
    return loreList;
  }

  public static List<String> getLoreOfItemInContainer(int slot) {
    if (slot == -1) {
      return new ArrayList<>();
    }
    ItemStack itemStack = mc.thePlayer.openContainer.getSlot(slot).getStack();
    if (itemStack == null) {
      return new ArrayList<>();
    }
    return getItemLore(itemStack);
  }

  public static int getAmountOfItemInInventory(String item) {
    int amount = 0;
    for (Slot slot : mc.thePlayer.inventoryContainer.inventorySlots) {
      if (slot.getHasStack()) {
        String itemName = StringUtils.stripControlCodes(slot.getStack().getDisplayName());
        if (itemName.equals(item)) {
          amount += slot.getStack().stackSize;
        }
      }
    }
    return amount;
  }

  public static boolean isInventoryLoaded() {
    if (mc.thePlayer == null || mc.thePlayer.openContainer == null) {
      return false;
    }
    if (!(mc.currentScreen instanceof GuiChest)) {
      return false;
    }
    ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
    ItemStack lastSlot = chest.getLowerChestInventory()
        .getStackInSlot(chest.getLowerChestInventory().getSizeInventory() - 1);
    return lastSlot != null && lastSlot.getItem() != null;
  }

  public static boolean isInventoryEmpty() {
    for (int i = 0; i < mc.thePlayer.inventory.getSizeInventory(); i++) {
      if (mc.thePlayer.inventory.getStackInSlot(i) == null) {
        continue;
      }
      return false;
    }
    return true;
  }

  public enum ClickType {
    LEFT, RIGHT
  }

  public enum ClickMode {
    PICKUP, QUICK_MOVE, SWAP
  }
}
