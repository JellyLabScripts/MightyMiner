package com.jelly.mightyminerv2.util;

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
import java.util.Collection;
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
        if (items.isEmpty()) {
            return -1;
        }
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

    /**
     * Returns a list of required items that are not present in the player's inventory
     * @param requiredItems Collection of item names to check for
     * @return List of missing item names (empty if all items are present)
     */
    public static List<String> getMissingItemsInInventory(Collection<String> requiredItems) {
        List<String> missingItems = new ArrayList<>(requiredItems);

        for (ItemStack stack : mc.thePlayer.inventory.mainInventory) {
            if (stack != null && stack.hasDisplayName()) {
                String displayName = stack.getDisplayName();
                missingItems.removeIf(displayName::contains);
            }
        }

        return missingItems;
    }

    public static boolean areItemsInInventory(Collection<String> items) {
        return getMissingItemsInInventory(items).isEmpty();
    }

    /**
     * Returns a list of required items that are not present in the player's hotbar
     * @param requiredItems Collection of item names to check for
     * @return List of missing item names (empty if all items are present)
     */
    public static List<String> getMissingItemsInHotbar(Collection<String> requiredItems) {
        List<String> missingItems = new ArrayList<>(requiredItems);
        for (int i = 0; i < 8; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.hasDisplayName()) {
                missingItems.removeIf(item -> stack.getDisplayName().contains(item));
            }
        }
        return missingItems;
    }

    public static boolean areItemsInHotbar(Collection<String> items) {
        return getMissingItemsInHotbar(items).isEmpty();
    }

    // returns the items that arent in hotbar and slots that items can be moved into
    public static Pair<List<Integer>, List<String>> getAvailableHotbarSlots(Collection<String> items) {
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

    public static List<String> getItemLoreFromOpenContainer(String name) {
        Container openContainer = mc.thePlayer.openContainer;
        for (int i = 0; i < openContainer.inventorySlots.size(); i++) {
            Slot slot = openContainer.getSlot(i);
            if (slot == null || !slot.getHasStack()) {
                continue;
            }
            ItemStack stack = slot.getStack();
            if (!stack.hasDisplayName() || !StringUtils.stripControlCodes(stack.getDisplayName()).contains(name)) {
                continue;
            }
            return getItemLore(stack);
        }
        return new ArrayList<>();
    }

    public static List<String> getItemLoreFromInventory(String name) {
        Container container = mc.thePlayer.inventoryContainer;
        for (int i = 0; i < container.inventorySlots.size(); i++) {
            Slot slot = container.getSlot(i);
            if (slot == null || !slot.getHasStack()) {
                continue;
            }
            ItemStack stack = slot.getStack();
            if (!stack.hasDisplayName() || !StringUtils.stripControlCodes(stack.getDisplayName()).contains(name)) {
                continue;
            }
            return getItemLore(stack);
        }
        return new ArrayList<>();
    }

    public static List<String> getItemLore(ItemStack itemStack) {
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
            if (slot.getHasStack() && slot.getStack().hasDisplayName()) {
                String itemName = StringUtils.stripControlCodes(slot.getStack().getDisplayName());
                if (itemName.equals(item)) {
                    amount += slot.getStack().stackSize;
                }
            }
        }
        return amount;
    }

    public static String getItemId(ItemStack stack) {
        if (stack == null || !stack.hasDisplayName()) {
            return "";
        }
        try {
            return stack.getTagCompound().getCompoundTag("ExtraAttributes").getString("id");
        } catch (Exception ignored) {
            return StringUtils.stripControlCodes(stack.getDisplayName());
        }
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

    public static String getFullName(String name) {
        for (int i = 0; i < mc.thePlayer.openContainer.inventorySlots.size(); i++) {
            Slot slot = mc.thePlayer.openContainer.getSlot(i);
            if (slot == null || !slot.getHasStack()) {
                continue;
            }
            ItemStack stack = slot.getStack();
            if (!stack.hasDisplayName()) {
                continue;
            }
            String itemName = StringUtils.stripControlCodes(stack.getDisplayName());
            if (!itemName.toLowerCase().contains(name.toLowerCase())) {
                continue;
            }
            return itemName;
        }
        return "";
    }

    public static int getDrillFuelCapacity(String drillName) {
        List<String> loreList = InventoryUtil.getItemLoreFromInventory(drillName);
        if (loreList.isEmpty()) {
            return -1;
        }
        for (String lore : loreList) {
            if (!lore.startsWith("Fuel: ")) {
                continue;
            }
            try {
                return Integer.parseInt(lore.split("/")[1].replace("k", "000"));
            } catch (Exception e) {
                Logger.sendNote("Could not retrieve fuel capacity. Lore: " + lore + ", Splitted: " + Arrays.toString(lore.split("/")));
                e.printStackTrace();
                break;
            }
        }
        return -1;
    }

    public static int getDrillRemainingFuel(String drillName) {
        List<String> loreList = InventoryUtil.getItemLoreFromInventory(drillName);
        if (loreList.isEmpty()) {
            return -1;
        }
        for (String lore : loreList) {
            if (!lore.startsWith("Fuel: ")) {
                continue;
            }
            try {
                return Integer.parseInt(lore.split(" ")[1].split("/")[0].replace(",", ""));
            } catch (Exception e) {
                Logger.sendNote("Could not retrieve fuel. Lore: " + lore + ", Splitted: " + Arrays.toString(lore.split("/")));
                e.printStackTrace();
                break;
            }
        }
        return -1;

    }

    public enum ClickType {
        LEFT, RIGHT
    }

    public enum ClickMode {
        PICKUP, QUICK_MOVE, SWAP
    }
}
