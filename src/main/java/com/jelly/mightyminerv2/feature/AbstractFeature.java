package com.jelly.mightyminerv2.feature;

import com.jelly.mightyminerv2.event.BlockChangeEvent;
import com.jelly.mightyminerv2.event.BlockDestroyEvent;
import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.event.UpdateTablistEvent;
import com.jelly.mightyminerv2.feature.impl.FeatureTracker;
import com.jelly.mightyminerv2.util.LogUtil;
import com.jelly.mightyminerv2.util.helper.Clock;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public abstract class AbstractFeature {

  protected final Minecraft mc = Minecraft.getMinecraft();
  protected final Clock timer = new Clock();
  protected boolean enabled = false;

  public AbstractFeature() {
    FeatureTracker.getInstance().addFeature(this);
  }

  public abstract String getName();

  public boolean isRunning() {
    return this.enabled;
  }

  // this (next 4 methods) is so bad please improve this if you want
  // i think i made this part too complicated
  // remember - if you dont need to accept params in start and stop (like autocommclaim.java) then use onEnable and onDisable
  // if you need to accept parameters to enable / disable then override start and/or stop but make sure you call this.start() and/or this.stop() at the end
  public void start() {
    this.onEnable();
    if (this.enabled) {
      FeatureTracker.getInstance().enableFeature(this);
    }
  }

  public void stop() {
    this.onDisable();
    FeatureTracker.getInstance().disableFeature(this);
  }

  public void onEnable() {
  }

  public void onDisable() {
    this.enabled = false;
    this.resetStatesAfterStop();
  }

  public void resetStatesAfterStop() {
  }

  public boolean isEnabled() {
    return true;
  }

  public boolean shouldStartAtLaunch() {
    return false;
  }

  public boolean shouldNotCheckForFailsafe() {
    return false;
  }

  protected boolean isTimerRunning() {
    return this.timer.isScheduled() && !this.timer.passed();
  }

  protected boolean hasTimerEnded() {
    return !this.timer.isScheduled() || this.timer.passed();
  }

  protected void onTick(ClientTickEvent event) {
  }

  protected void onRender(RenderWorldLastEvent event) {
  }

  protected void onChat(String message) {
  }

  protected void onTablistUpdate(UpdateTablistEvent event) {
  }

  protected void onOverlayRender(RenderGameOverlayEvent event) {
  }

  protected void onPacketReceive(PacketEvent.Received event) {
  }

  protected void onBlockChange(BlockChangeEvent event) {
  }

  protected void onBlockDestroy(BlockDestroyEvent event) {
  }

  protected void onKeyEvent(InputEvent.KeyInputEvent event) {
  }

  public void log(String message) {
    LogUtil.log(formatMessage(message));
  }

  public void send(String message) {
    LogUtil.send(formatMessage(message));
  }

  public void error(String message) {
    LogUtil.error(formatMessage(message));
  }

  public void warn(String message) {
    LogUtil.warn(formatMessage(message));
  }

  private String formatMessage(String message) {
    return "[" + getName() + "] " + message;
  }
}
