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

  @Override
  public String getName() {
    return "DisconnectFailsafe";
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
  public void react() {
    warn("Disconnected. Disabling Macro");
    MacroManager.getInstance().disable();
  }
}
