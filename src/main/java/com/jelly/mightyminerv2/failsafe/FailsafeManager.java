package com.jelly.mightyminerv2.failsafe;

import com.jelly.mightyminerv2.command.OsamaTestCommandNobodyTouchPleaseLoveYou;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.event.BlockChangeEvent;
import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe.Failsafe;
import com.jelly.mightyminerv2.failsafe.impl.*;
import com.jelly.mightyminerv2.feature.FeatureManager;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.StrafeUtil;
import com.jelly.mightyminerv2.util.helper.Clock;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import javax.crypto.Mac;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class FailsafeManager {

  private static FailsafeManager instance;

  public static FailsafeManager getInstance() {
    if (instance == null) {
      instance = new FailsafeManager();
    }
    return instance;
  }

  private final Minecraft mc = Minecraft.getMinecraft();
  private Clock timer = new Clock();
  public final List<AbstractFailsafe> failsafes = new ArrayList<>();
  public Optional<AbstractFailsafe> triggeredFailsafe = Optional.empty();
  public final Queue<AbstractFailsafe> emergencyQueue = new PriorityQueue<>(Comparator.comparing(AbstractFailsafe::getPriority));
  public Set<Failsafe> failsafesToIgnore = new HashSet<>();

  public FailsafeManager() {
    this.failsafes.addAll(Arrays.asList(
            BadEffectFailsafe.getInstance(),
            DisconnectFailsafe.getInstance(),
            ItemChangeFailsafe.getInstance(),
            KnockbackFailsafe.getInstance(),
            TeleportFailsafe.getInstance(),
            RotationFailsafe.getInstance(),
//            BedrockBlockChangeFailsafe.getInstance(),
            BedrockCheckFailsafe.getInstance()
        )
    );
  }

  public void stopFailsafes() {
    triggeredFailsafe = Optional.empty();
    emergencyQueue.clear();
  }

  public boolean shouldNotCheckForFailsafe() {
    return !MacroManager.getInstance().isRunning() || this.triggeredFailsafe.isPresent();
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onTick(ClientTickEvent event) {
    if (this.shouldNotCheckForFailsafe()) {
      return;
    }

    List<AbstractFailsafe> failsafeCopy = new ArrayList<>(failsafes);
    this.failsafesToIgnore.clear();
    this.failsafesToIgnore.addAll(FeatureManager.getInstance().getFailsafesToIgnore());

    for (AbstractFailsafe failsafe : failsafeCopy) {
      if (!this.failsafesToIgnore.contains(failsafe.getFailsafeType()) && failsafe.onTick(event)) {
        this.emergencyQueue.add(failsafe);
      }
    }
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onBlockChange(BlockChangeEvent event) {
    if (this.shouldNotCheckForFailsafe()) {
      return;
    }

    failsafes.forEach(failsafe -> {
      if (!this.failsafesToIgnore.contains(failsafe.getFailsafeType()) && failsafe.onBlockChange(event)) {
        this.emergencyQueue.add(failsafe);
      }
    });
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onPacketReceive(PacketEvent.Received event) {
    if (this.shouldNotCheckForFailsafe()) {
      return;
    }

    failsafes.forEach(failsafe -> {
      if (!this.failsafesToIgnore.contains(failsafe.getFailsafeType()) && failsafe.onPacketReceive(event)) {
        this.emergencyQueue.add(failsafe);
      }
    });
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onChat(ClientChatReceivedEvent event) {
    if (this.shouldNotCheckForFailsafe()) {
      return;
    }

    failsafes.forEach(failsafe -> {
      if (!this.failsafesToIgnore.contains(failsafe.getFailsafeType()) && failsafe.onChat(event)) {
        this.emergencyQueue.add(failsafe);
      }
    });
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onWorldUnload(WorldEvent.Unload event) {
    if (this.shouldNotCheckForFailsafe()) {
      return;
    }

    failsafes.forEach(failsafe -> {
      if (!this.failsafesToIgnore.contains(failsafe.getFailsafeType()) && failsafe.onWorldUnload(event)) {
        this.emergencyQueue.add(failsafe);
      }
    });
  }

  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
    if (this.shouldNotCheckForFailsafe()) {
      return;
    }

    failsafes.forEach(failsafe -> {
      if (!this.failsafesToIgnore.contains(failsafe.getFailsafeType()) && failsafe.onDisconnect(event)) {
        this.emergencyQueue.add(failsafe);
      }
    });
  }

  @SubscribeEvent
  public void onTickChooseEmergency(ClientTickEvent event) {
    if (this.shouldNotCheckForFailsafe()) {
      return;
    }
    if (this.triggeredFailsafe.isPresent()) {
      return;
    }
    if (this.emergencyQueue.isEmpty()) {
      return;
    }

    StrafeUtil.forceStop = true;
    if (!this.timer.isScheduled()) {
      this.timer.schedule(MightyMinerConfig.failsafeToggleDelay);
    } else if (this.timer.passed()) {
      this.triggeredFailsafe = Optional.ofNullable(this.emergencyQueue.peek());
      this.emergencyQueue.clear();
      this.timer.reset();
    }
  }

  @SubscribeEvent
  public void onTickReact(ClientTickEvent event) {
    if (!this.triggeredFailsafe.isPresent()) {
      return;
    }

    // make a reset method
    if (this.triggeredFailsafe.get().react()) {
      StrafeUtil.forceStop = false;
      this.triggeredFailsafe = Optional.empty();
      this.emergencyQueue.clear();
      this.failsafes.forEach(AbstractFailsafe::resetStates);
    }
  }

  public boolean isFailsafeActive(Failsafe failsafe) {
    System.out.println("failsafequeue: " + this.emergencyQueue);
    return this.emergencyQueue.stream().anyMatch(it -> it.getFailsafeType().equals(failsafe));
  }
}
