package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiDisconnected;

public class DisconnectFailsafe extends AbstractFailsafe {

    private static final DisconnectFailsafe instance = new DisconnectFailsafe();

    public static DisconnectFailsafe getInstance() {
        return instance;
    }

    public int getPriority() {
        return 10;
    }

    @Override
    public boolean onPacketReceive(PacketEvent.Received event) {
        if (event.packet instanceof S40PacketDisconnect) {
            return true;
        }

        if (Minecraft.getMinecraft().currentScreen instanceof GuiDisconnected) {
            return true;
        }

        return false;
    }

    @Override
    public void react() {
        MacroManager.getInstance().disable();
    }
}
