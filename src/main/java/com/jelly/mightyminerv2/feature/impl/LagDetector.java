package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.util.helper.Clock;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class LagDetector extends AbstractFeature {

  private static LagDetector instance;

  public static LagDetector getInstance() {
    if (instance == null) {
      instance = new LagDetector();
    }
    return instance;
  }

  @Override
  public String getName() {
    return "LagDetector";
  }

  private final Clock lagTimer = new Clock();
  private long lastReceivedPacketTime = -1;

  @SubscribeEvent
  public void onReceivePacket(PacketEvent event) {
    if (event.packet instanceof S03PacketTimeUpdate) {
      lastReceivedPacketTime = System.currentTimeMillis();
    }
  }

  @SubscribeEvent
  public void onTick(TickEvent.ClientTickEvent event) {
    if (System.currentTimeMillis() - lastReceivedPacketTime > 1300) {
      lagTimer.schedule(900);
    }
    if (lagTimer.isScheduled() && lagTimer.passed()) {
      lagTimer.reset();
    }
  }

  public boolean isLagging() {
    return lagTimer.isScheduled();
  }

//  private boolean isLagging() {
//    return getTimeSinceLastPacket() > 1.3;
//  }

  private float getTimeSinceLastPacket() {
    return (System.currentTimeMillis() - lastReceivedPacketTime) / 1000F;
  }
}
