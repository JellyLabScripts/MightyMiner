package com.jelly.mightyminerv2.macro;

import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.event.UpdateTablistEvent;
import com.jelly.mightyminerv2.util.LogUtil;
import com.jelly.mightyminerv2.util.helper.Clock;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public abstract class AbstractMacro {

  private static final Minecraft mc = Minecraft.getMinecraft();
  private boolean enabled = false;
  public Clock timer = new Clock();

  public abstract String getName();

  public boolean isEnabled() {
    return this.enabled;
  }

  public void enable() {
    log("AbstractMacro::enable");
    this.onEnable();
    this.enabled = true;
  }

  public void disable() {
    log("AbstractMacro::disable");
    this.enabled = false;
    this.onDisable();
  }

  public void pause() {
    log("AbstractMacro::pause");
    this.enabled = false;
    this.onPause();
  }

  public void resume() {
    log("AbstractMacro::resume");
    this.onResume();
    this.enabled = true;
  }

  public void toggle() {
    if (this.enabled) {
      this.disable();
    } else {
      this.enable();
    }
  }

  public abstract List<String> getNecessaryItems();

  public boolean hasTimerEnded() {
    return this.timer.isScheduled() && this.timer.passed();
  }

  public boolean isTimerRunning() {
    return this.timer.isScheduled() && !this.timer.passed();
  }

  public void onEnable() {
  }

  public void onDisable() {
  }

  public void onPause() {
  }

  public void onResume() {
  }

  public void onTick(ClientTickEvent event) {
  }

  public void onWorldRender(RenderWorldLastEvent event) {
  }

  public void onChat(String message) {
  }

  public void onTablistUpdate(UpdateTablistEvent event) {
  }

  public void onOverlayRender(RenderGameOverlayEvent event) {
  }

  public void onReceivePacket(PacketEvent.Received event) {
  }

  public void log(String message) {
    LogUtil.log(getMessage(message));
  }

  public void send(String message) {
    LogUtil.send(getMessage(message));
  }

  public void error(String message) {
    LogUtil.error(getMessage(message));
  }

  public void warn(String message) {
    LogUtil.warn(getMessage(message));
  }

  public String getMessage(String message) {
    return "[" + this.getName() + "] " + message;
  }
}
