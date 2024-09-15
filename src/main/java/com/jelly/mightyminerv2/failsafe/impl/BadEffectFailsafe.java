package com.jelly.mightyminerv2.failsafe.impl;

import com.google.common.collect.ImmutableSet;
import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import java.util.Set;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.potion.Potion;

public class BadEffectFailsafe extends AbstractFailsafe {

  private static final BadEffectFailsafe instance = new BadEffectFailsafe();

  public static BadEffectFailsafe getInstance() {
    return instance;
  }

  private final Set<Integer> BAD_EFFECTS = ImmutableSet.of(
      Potion.poison.id,
      Potion.wither.id,
      Potion.weakness.id,
      Potion.blindness.id,
      Potion.hunger.id,
      Potion.moveSlowdown.id,
      Potion.digSlowdown.id
  );

  @Override
  public String getName() {
    return "BadEffectFailsafe";
  }

  @Override
  public Failsafe getFailsafeType() {
    return Failsafe.BAD_EFFECTS;
  }

  @Override
  public int getPriority() {
    return 7;
  }

  // i dont know if this will work or not
  @Override
  public boolean onPacketReceive(PacketEvent.Received event) {
    if (!(event.packet instanceof S1DPacketEntityEffect)) {
      return false;
    }

    S1DPacketEntityEffect packet = (S1DPacketEntityEffect) event.packet;
    return mc.theWorld.getEntityByID(packet.getEntityId()) instanceof EntityPlayerSP && this.BAD_EFFECTS.contains(packet.getEffectId());
  }

  @Override
  public boolean react() {
    MacroManager.getInstance().disable();
    warn("Bad effect detected! Disabling macro.");
    return true;
  }
}
