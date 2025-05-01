package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import lombok.Getter;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.network.play.server.S40PacketDisconnect;

public class DisconnectFailsafe extends AbstractFailsafe {

    @Getter
    private static final DisconnectFailsafe instance = new DisconnectFailsafe();

    @Override
    public String getName() {
        return "DisconnectFailsafe";
    }

    @Override
    public Failsafe getFailsafeType() {
        return Failsafe.DISCONNECT;
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public boolean onPacketReceive(PacketEvent.Received event) {
        return event.packet instanceof S40PacketDisconnect || mc.currentScreen instanceof GuiDisconnected;
    }

    @Override
    public boolean react() {
        warn("Disconnected. Disabling Macro");
        MacroManager.getInstance().disable();
        return true;
    }
}
