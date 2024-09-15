package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.Logger;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class RotationFailsafe extends AbstractFailsafe {

  private static final RotationFailsafe instance = new RotationFailsafe();

  public static RotationFailsafe getInstance() {
    return instance;
  }

  public int getPriority() {
    return 5;
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public Failsafe getFailsafeType() {
    return Failsafe.ROTATION;
  }

  public boolean onPacketReceive(PacketEvent.Received event) {
    if (!(event.packet instanceof S08PacketPlayerPosLook)) {
      return false;
    }

    S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook) event.packet;

    float playerYaw = mc.thePlayer.rotationYaw;
    float playerPitch = mc.thePlayer.rotationPitch;

    float packetYaw = packet.getYaw();
    float packetPitch = packet.getPitch();

    float yawDifference = Math.abs(playerYaw - packetYaw);
    float pitchDifference = Math.abs(playerPitch - packetPitch);

    double yawThreshold = 30.0;
    double pitchThreshold = 30.0;

    if (yawDifference > yawThreshold || pitchDifference > pitchThreshold) {
      return true;
    }

    return false;
  }

  @Override
  public boolean react() {
    MacroManager.getInstance().disable();
    Logger.sendWarning("You`ve got rotated! Disabeling macro.");
    return true;
  }
}
