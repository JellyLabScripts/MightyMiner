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
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import java.util.HashMap;
import java.util.Map;

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
            log("timer passed");
        }

        return this.timer.passed();
    }


    @Override
    public boolean onPacketReceive(PacketEvent.Received event) {
        if (mc.currentScreen instanceof GuiContainer || !(event.packet instanceof S2FPacketSetSlot)) {
            return false;
        }

        S2FPacketSetSlot packet = (S2FPacketSetSlot) event.packet;
        int slot = packet.func_149173_d();

        if (slot <= 0) {
            return false;
        }

        if (slot >= 45) {
            return false;
        }
        Slot oldSlot = mc.thePlayer.inventoryContainer.getSlot(slot);
        if (!oldSlot.getHasStack() || !oldSlot.getStack().hasDisplayName()) {
            return false;
        }
        String oldItem = InventoryUtil.getItemId(oldSlot.getStack());
        String newItem = InventoryUtil.getItemId(packet.func_149174_e());

        if (!oldItem.isEmpty() && newItem.isEmpty()) {
            String oldName = StringUtils.stripControlCodes(oldSlot.getStack().getDisplayName());
            if (MacroManager.getInstance().getCurrentMacro().getNecessaryItems().stream().anyMatch(oldName::contains)) {
                removedItems.put(oldItem, slot);
                note("Item " + oldName + " with id " + oldItem + " was removed from slot " + slot);
            }
        }

        if (!newItem.isEmpty() && oldItem.isEmpty()) {
            Integer oldSlotNumber = removedItems.remove(newItem);
            if (oldSlotNumber != null) {
                note("Item with id " + newItem + " was removed from " + oldSlotNumber + " and added back to slot " + slot);
                if (oldSlotNumber != slot) {
                    warn("Item was moved.");
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
