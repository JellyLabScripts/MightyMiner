package com.jelly.mightyminerv2.feature;

import com.jelly.mightyminerv2.event.BlockChangeEvent;
import com.jelly.mightyminerv2.event.BlockDestroyEvent;
import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.event.UpdateTablistEvent;
import com.jelly.mightyminerv2.feature.impl.FeatureTracker;
import java.util.Set;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class FeatureManager {

  private static FeatureManager instance;

  public static FeatureManager getInstance() {
    if (instance == null) {
      instance = new FeatureManager();
    }
    return instance;
  }

  private Set<AbstractFeature> features = FeatureTracker.getInstance().activeFeatures;
  private boolean paused = false;

  @SubscribeEvent
  protected void onTick(ClientTickEvent event) {
    if (this.paused) {
      return;
    }
    if (FeatureTracker.getInstance().updated && event.phase == Phase.START) {
      this.features = FeatureTracker.getInstance().activeFeatures;
      FeatureTracker.getInstance().updated = false;
    }
    this.features.forEach(it -> it.onTick(event));
  }

  @SubscribeEvent
  protected void onRender(RenderWorldLastEvent event) {
    if (this.paused) {
      return;
    }
    features.forEach(it -> it.onRender(event));
  }

  @SubscribeEvent
  protected void onChat(ClientChatReceivedEvent event) {
    if (this.paused) {
      return;
    }
    features.forEach(it -> it.onChat(event.message.getUnformattedText()));
  }

  @SubscribeEvent
  protected void onTablistUpdate(UpdateTablistEvent event) {
    if (this.paused) {
      return;
    }
    features.forEach(it -> it.onTablistUpdate(event));
  }

  @SubscribeEvent
  protected void onOverlayRender(RenderGameOverlayEvent event) {
    if (this.paused) {
      return;
    }
    features.forEach(it -> it.onOverlayRender(event));
  }

  @SubscribeEvent
  protected void onPacketReceive(PacketEvent.Received event) {
    if (this.paused) {
      return;
    }
    features.forEach(it -> it.onPacketReceive(event));
  }

  @SubscribeEvent
  protected void onBlockChange(BlockChangeEvent event) {
    if (this.paused) {
      return;
    }
    features.forEach(it -> it.onBlockChange(event));
  }

  @SubscribeEvent
  protected void onBlockDestroy(BlockDestroyEvent event) {
    if (this.paused) {
      return;
    }
    features.forEach(it -> it.onBlockDestroy(event));
  }

  @SubscribeEvent
  protected void onKeyEvent(InputEvent.KeyInputEvent event) {
    if (this.paused) {
      return;
    }
    features.forEach(it -> it.onKeyEvent(event));
  }

  public void enableAll() {
    FeatureTracker.getInstance().startFeatures();
    this.paused = false;
  }

  public void disableAll() {
    FeatureTracker.getInstance().stopAllFeatures();
  }

  public void pauseAll() {
    this.paused = true;
  }

  public void resumeAll() {
    this.paused = false;
  }

  public boolean shouldNotCheckForFailsafe() {
    return FeatureTracker.getInstance().activeFeatures.stream().anyMatch(AbstractFeature::shouldNotCheckForFailsafe);
  }
}
