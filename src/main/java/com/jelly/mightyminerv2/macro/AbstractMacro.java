package com.jelly.mightyminerv2.macro;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.event.UpdateTablistEvent;
import com.jelly.mightyminerv2.hud.CommissionHUD;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.helper.Clock;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public abstract class AbstractMacro {

  protected final Minecraft mc = Minecraft.getMinecraft();
  private boolean enabled = false;
  public Clock timer = new Clock();
  public Clock uptime = new Clock();

  public abstract String getName();

  public boolean isEnabled() {
    return this.enabled;
  }

  public void enable() {
    log("AbstractMacro::enable");
    this.onEnable();
    this.uptime.start(CommissionHUD.getInstance().commHudResetStats);
    this.enabled = true;
  }

  public void disable() {
    log("AbstractMacro::disable");
    this.uptime.stop(CommissionHUD.getInstance().commHudResetStats);
    this.enabled = false;
    this.onDisable();
  }

  public void pause() {
    log("AbstractMacro::pause");
    this.uptime.stop(false);
    this.enabled = false;
    this.onPause();
  }

  public void resume() {
    log("AbstractMacro::resume");
    this.onResume();
    this.uptime.start(false);
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
