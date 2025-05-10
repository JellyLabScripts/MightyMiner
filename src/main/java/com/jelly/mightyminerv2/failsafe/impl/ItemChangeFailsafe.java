package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.helper.Clock;
import lombok.Getter;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemChangeFailsafe extends AbstractFailsafe {

    @Getter
    private static final ItemChangeFailsafe instance = new ItemChangeFailsafe();
    private final Clock timer = new Clock();
    private final Map<String, Integer> removedItems = new HashMap();

    @Override
    public String getName() {
        return "ItemChangeFailsafe";
    }

    @Override
    public Failsafe getFailsafeType() {
        return Failsafe.ITEM_CHANGE;
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public boolean onTick(ClientTickEvent event) {
        if (mc.currentScreen instanceof GuiContainer) {
            if (!this.removedItems.isEmpty() || this.timer.isScheduled()) {
                resetStates();
            }
            return false;
        }

        if (this.removedItems.isEmpty()) {
            if (this.timer.isScheduled()) {
                this.timer.reset();
            }
            return false;
        }

        if (!this.timer.isScheduled()) {
            this.timer.schedule(2000);
        }

        if (this.timer.passed()) {
            for (Map.Entry<String, Integer> entry : removedItems.entrySet()) {
                warn("Necessary item with ID '" + entry.getKey() + "' is confirmed missing from slot " + entry.getValue() + " after timeout.");
            }
            return true;
        }

        return false;
    }


    @Override
    public boolean onPacketReceive(PacketEvent.Received event) {
        if (mc.currentScreen instanceof GuiContainer) {
            if (!this.removedItems.isEmpty() || this.timer.isScheduled()) {
                resetStates();
            }
            return false;
        }

        if (!(event.packet instanceof S2FPacketSetSlot)) {
            return false;
        }

        S2FPacketSetSlot packet = (S2FPacketSetSlot) event.packet;
        int slot = packet.func_149173_d();

        // Slots 1-44 are main player inventory
        if (slot <= 0 || slot >= 45) {
            return false;
        }

        Slot oldSlotObj = mc.thePlayer.inventoryContainer.getSlot(slot);
        ItemStack oldStackInSlot = oldSlotObj.getHasStack() ? oldSlotObj.getStack() : null;
        ItemStack newStackFromPacket = packet.func_149174_e();

        String oldItemId = (oldStackInSlot != null) ? InventoryUtil.getItemId(oldStackInSlot) : "";
        String newItemId = (newStackFromPacket != null) ? InventoryUtil.getItemId(newStackFromPacket) : "";

        // Get and filter the list of necessary items
        List<String> necessaryDisplayNames = MacroManager.getInstance().getCurrentMacro().getNecessaryItems()
                .stream()
                .filter(name -> name != null && !name.isEmpty())
                .collect(Collectors.toList());

        // Check if the removed/replaced item was necessary
        if (oldStackInSlot != null && (newStackFromPacket == null || !oldItemId.equals(newItemId))) {
            if (oldStackInSlot.hasDisplayName()) {
                String oldDisplayName = StringUtils.stripControlCodes(oldStackInSlot.getDisplayName());
                if (necessaryDisplayNames.stream().anyMatch(oldDisplayName::contains)) {
                    removedItems.put(oldItemId, slot);
                    log("Necessary item '" + oldDisplayName + "' (ID: " + oldItemId + ") was removed/replaced from slot " + slot);
                }
            }
        }

        if (newStackFromPacket != null && (oldStackInSlot == null || !newItemId.equals(oldItemId))) {
            Integer originalSlotOfThisItem = removedItems.get(newItemId);

            if (originalSlotOfThisItem != null) {
                removedItems.remove(newItemId);

                String newDisplayName = newStackFromPacket.hasDisplayName() ? StringUtils.stripControlCodes(newStackFromPacket.getDisplayName()) : newItemId;
                log("Tracked necessary item '" + newDisplayName + "' (ID: " + newItemId + ") from original slot " + originalSlotOfThisItem + " now in slot " + slot + ".");

                if (!originalSlotOfThisItem.equals(slot)) {
                    log("Necessary item '" + newDisplayName + "' (ID: " + newItemId + ") was MOVED from slot " + originalSlotOfThisItem + " to slot " + slot + ". Triggering failsafe!");
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onChat(ClientChatReceivedEvent event) {
        if (event.type != 0 || this.removedItems.isEmpty()) {
            return false;
        }
        String message = event.message.getUnformattedText();
        if (message.equals("Oh no! Your Pickonimbus 2000 broke!") && this.removedItems.remove("PICKONIMBUS") != null) {
            error("Pickonimbus broke. Ignoring");
        }
        return false;
    }

    @Override
    public boolean react() {
        MacroManager.getInstance().disable();
        Logger.sendWarning("Your item has been changed! Disabling macro.");
        return true;
    }

    @Override
    public void resetStates() {
        this.timer.reset();
        this.removedItems.clear();
    }
}
