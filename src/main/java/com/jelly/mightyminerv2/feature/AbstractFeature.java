package com.jelly.mightyminerv2.feature;

import com.jelly.mightyminerv2.event.BlockChangeEvent;
import com.jelly.mightyminerv2.event.BlockDestroyEvent;
import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.event.UpdateTablistEvent;
import com.jelly.mightyminerv2.failsafe.AbstractFailsafe.Failsafe;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.helper.Clock;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public abstract class AbstractFeature {

  protected final Minecraft mc = Minecraft.getMinecraft();
  protected final Clock timer = new Clock();
  protected List<Failsafe> failsafesToIgnore;
  protected boolean enabled = false;

  public AbstractFeature() {
    this.failsafesToIgnore = new ArrayList<>();
  }

  public abstract String getName();

  public boolean isRunning() {
    return this.enabled;
  }

  public List<Failsafe> getFailsafesToIgnore() {
    return this.failsafesToIgnore;
  }

  public void start() {
  }

  public void stop() {
    this.enabled = false;
    this.resetStatesAfterStop();
  }

  public void pause() {
    this.enabled = false;
  }

  public void resume() {
    this.enabled = true;
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
    return this.timer.isScheduled() && this.timer.passed();
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

  protected void onWorldLoad(WorldEvent.Load event) {

  }

  protected void onWorldUnload(WorldEvent.Unload event) {

  }

  protected void onBlockChange(BlockChangeEvent event) {
  }

  protected void onBlockDestroy(BlockDestroyEvent event) {
  }

  protected void onKeyEvent(InputEvent.KeyInputEvent event) {
  }

  protected void log(String message) {
    Logger.sendLog(formatMessage(message));
  }

  protected void send(String message) {
    Logger.sendMessage(formatMessage(message));
  }

  protected void error(String message) {
    Logger.sendError(formatMessage(message));
  }

  protected void warn(String message) {
    Logger.sendWarning(formatMessage(message));
  }

  protected void note(String message) {
    Logger.sendNote(formatMessage(message));
  }

  protected String formatMessage(String message) {
    return "[" + getName() + "] " + message;
  }
}
