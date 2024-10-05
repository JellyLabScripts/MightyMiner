package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.feature.impl.LagDetector; // Import LagDetector
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.Vec3;

public class TeleportFailsafe extends AbstractFailsafe {

  private static final TeleportFailsafe instance = new TeleportFailsafe();
  private final LagDetector lagDetector = LagDetector.getInstance();  // Get LagDetector instance

  public static TeleportFailsafe getInstance() {
    return instance;
  }

  @Override
  public String getName() {
    return "TeleportFailsafe";
  }

  @Override
  public Failsafe getFailsafeType() {
    return Failsafe.TELEPORT;
  }

  @Override
  public int getPriority() {
    return 5;
  }

  @Override
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

    if (lagDetector.isLagging()) {
      warn("Lagback detected, ignoring teleport packet. Distance: " + distance + " blocks.");
      return false;
    } else {
      warn("Got Teleported " + distance + " blocks.");
      return true;
    }
  }

  @Override
  public boolean react() {
    MacroManager.getInstance().disable();
    warn("Disabling macro.");
    return true;
  }
}
