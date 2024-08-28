package com.jelly.mightyminerv2.failsafe;

import com.jelly.mightyminerv2.event.BlockChangeEvent;
import com.jelly.mightyminerv2.event.PacketEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public abstract class AbstractFailsafe {

  public final Minecraft mc = Minecraft.getMinecraft();

  public abstract int getPriority();

  public boolean onBlockChange(BlockChangeEvent event) {
    return false;
  }

  public boolean onPacketReceive(PacketEvent.Received event) {
    return false;
  }

  public boolean onTick(TickEvent.ClientTickEvent event) {
    return false;
  }

  public boolean onChat(ClientChatReceivedEvent event) {
    return false;
  }

  public boolean onWorldUnload(WorldEvent.Unload event) {
    return false;
  }

  public boolean onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
    return false;
  }

  public abstract void react();

  public void resetStates() {
  }
}
