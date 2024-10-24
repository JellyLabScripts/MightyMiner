package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.helper.Clock;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S09PacketHeldItemChange;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class SlotChangeFailsafe extends AbstractFailsafe {

    private static final SlotChangeFailsafe instance = new SlotChangeFailsafe();
    private final Minecraft mc = Minecraft.getMinecraft();
    private final Clock timer = new Clock();
    private int lastSelectedSlot;
    private boolean slotChanged = false;

    public static SlotChangeFailsafe getInstance() {
        return instance;
    }

    private SlotChangeFailsafe() {
        this.lastSelectedSlot = mc.thePlayer != null ? mc.thePlayer.inventory.currentItem : -1;
    }

    @Override
    public String getName() {
        return "SlotChangeFailsafe";
    }

    @Override
    public Failsafe getFailsafeType() {
        return Failsafe.SLOT_CHANGE;
    }

    @Override
    public int getPriority() {
        return 5;
    }

    @Override
    public boolean onTick(ClientTickEvent event) {
        if (slotChanged && timer.passed()) {
            Logger.sendLog("Timer passed after slot change");
            return true;
        }
        return false;
    }

    @Override
    public boolean onPacketReceive(PacketEvent.Received event) {
        if (event.packet instanceof S09PacketHeldItemChange) {
            S09PacketHeldItemChange packet = (S09PacketHeldItemChange) event.packet;
            int slotIndex = packet.getHeldItemHotbarIndex();

            if (slotIndex != lastSelectedSlot) {
                log("Slot changed by S09 packet from " + lastSelectedSlot + " to " + slotIndex);
                slotChanged = true;
                lastSelectedSlot = slotIndex;

                if (!timer.isScheduled()) {
                    timer.schedule(2000);
                }
            }
        }
        return false;
    }

    @Override
    public boolean react() {
        if (slotChanged) {
            MacroManager.getInstance().disable();
            warn("Slot selection changed! Disabling macro.");
            slotChanged = false;
            return true;
        }
        return false;
    }

    @Override
    public void resetStates() {
        timer.reset();
        slotChanged = false;
        log("SlotChangeFailsafe state reset.");
    }
}
