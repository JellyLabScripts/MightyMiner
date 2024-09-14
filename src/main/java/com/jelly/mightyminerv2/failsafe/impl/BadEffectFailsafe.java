package com.jelly.mightyminerv2.failsafe.impl;

import com.google.common.collect.ImmutableSet;
import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.Logger;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

import java.util.Collection;

public class BadEffectFailsafe extends AbstractFailsafe {

  private static final BadEffectFailsafe instance = new BadEffectFailsafe();

  public static BadEffectFailsafe getInstance() {
    return instance;
  }

  private final Set<Integer> BAD_EFFECTS = ImmutableSet.of(
      Potion.poison.id,       // Poison
      Potion.wither.id,       // Wither
      Potion.weakness.id,     // Weakness
      Potion.blindness.id,    // Blindness
      Potion.hunger.id,       // Hunger
      Potion.moveSlowdown.id, // Slowness
      Potion.digSlowdown.id   // Mining Fatigue
  );

  @Override
  public String getName() {
    return "BadEffectFailsafe";
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
  public void react() {
    MacroManager.getInstance().disable();
    warn("Bad effect detected! Disabling macro.");
  }
}
