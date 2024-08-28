package com.jelly.mightyminerv2.macro;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.event.UpdateTablistEvent;
import com.jelly.mightyminerv2.feature.FeatureManager;
import com.jelly.mightyminerv2.macro.commissionmacro.CommissionMacro;
import com.jelly.mightyminerv2.util.LogUtil;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class MacroManager {

  private static MacroManager instance = new MacroManager();

  public static MacroManager getInstance() {
    return instance;
  }

  private AbstractMacro currentMacro;

  public AbstractMacro getCurrentMacro() {
    switch (MightyMinerConfig.macroType) {
      case 0:
        return CommissionMacro.getInstance();
      default:
        return CommissionMacro.getInstance(); // Throw error or something
    }
  }

  public void toggle() {
    log("Toggling");
    if (currentMacro != null) {
      log("CurrMacro != null");
      this.disable();
    } else {
      log("CurrMacro == null");
      this.enable();
    }
  }

  public void enable() {
    log("Macro::enable");
    FeatureManager.getInstance().enableAll();
    this.currentMacro = this.getCurrentMacro();
    this.currentMacro.enable();
    send(this.currentMacro.getName() + " Enabled");
  }

  public void disable() {
    log("Macro::disable");
    FeatureManager.getInstance().disableAll();
    this.currentMacro.disable();
    this.currentMacro = null;
    send(this.currentMacro.getName() + " Disabled");
  }

  public void pause() {
    if (this.currentMacro == null) {
      return;
    }
    log("Macro::pause");
    this.currentMacro.pause();
    send(this.currentMacro.getName() + " Paused");
  }

  public void resume() {
    if (this.currentMacro == null) {
      return;
    }
    log("Macro::resume");
    this.currentMacro.resume();
    send(this.currentMacro.getName() + " Resumed");
  }

  public boolean isRunning() {
    return this.currentMacro != null;
  }

  @SubscribeEvent
  public void onTick(ClientTickEvent event) {
    if (this.currentMacro == null) {
      return;
    }

    if (!currentMacro.isEnabled()) {
      this.disable();
      return;
    }

    this.currentMacro.onTick(event);
  }

  @SubscribeEvent
  public void onChat(ClientChatReceivedEvent event) {
    if (this.currentMacro == null) {
      return;
    }

    this.currentMacro.onChat(event.message.getUnformattedText());
  }

  @SubscribeEvent
  public void onTablistUpdate(UpdateTablistEvent event) {
    if (this.currentMacro == null) {
      return;
    }

    this.currentMacro.onTablistUpdate(event);
  }

  @SubscribeEvent
  public void onRender(RenderWorldLastEvent event) {
    if (this.currentMacro == null) {
      return;
    }

    this.currentMacro.onWorldRender(event);
  }

  @SubscribeEvent
  public void onOverlayRender(RenderGameOverlayEvent event) {
    if (this.currentMacro == null) {
      return;
    }

    this.currentMacro.onOverlayRender(event);
  }

  @SubscribeEvent
  public void onPacketReceive(PacketEvent.Received event) {
    if (this.currentMacro == null) {
      return;
    }

    this.currentMacro.onReceivePacket(event);
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
    return "[MacroHandler] " + message;
  }
}
