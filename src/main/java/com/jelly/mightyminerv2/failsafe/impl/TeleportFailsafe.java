package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.Vec3;

public class TeleportFailsafe extends AbstractFailsafe {

  private static final TeleportFailsafe instance = new TeleportFailsafe();

  public static TeleportFailsafe getInstance() {
    return instance;
  }

  public int getPriority() {
    return 5;
  }

  public boolean onPacketReceive(PacketEvent.Received event) {
    if (!(event.packet instanceof S08PacketPlayerPosLook)) {
      return false;
    }

    S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook) event.packet;
    Vec3 playerPos = mc.thePlayer.getPositionVector();
    Vec3 packetPos = new Vec3(
        packet.getX() + (packet.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.X) ? playerPos.xCoord : 0),
        packet.getY() + (packet.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Y) ? playerPos.yCoord : 0),
        packet.getZ() + (packet.func_179834_f().contains(S08PacketPlayerPosLook.EnumFlags.Z) ? playerPos.zCoord : 0)
    );

    double distance = playerPos.distanceTo(packetPos);

    return false;
  }

  public void react() {

  }

  public void finishReact() {

  }
}
