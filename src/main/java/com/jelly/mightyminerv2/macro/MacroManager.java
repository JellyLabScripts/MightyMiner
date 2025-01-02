package com.jelly.mightyminerv2.macro;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.event.UpdateTablistEvent;
import com.jelly.mightyminerv2.feature.FeatureManager;
import com.jelly.mightyminerv2.feature.impl.MouseUngrab;
import com.jelly.mightyminerv2.macro.commissionmacro.CommissionMacro;
import com.jelly.mightyminerv2.macro.mithrilmacro.MithrilMacro;
import com.jelly.mightyminerv2.util.Logger;
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
      case 1:
        return MithrilMacro.getInstance();
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
    if (this.currentMacro == null) {
      return;
    }
    log("Macro::disable");
    FeatureManager.getInstance().disableAll();
    MouseUngrab.getInstance().regrabMouse();
    this.currentMacro.disable();
    send(this.currentMacro.getName() + " Disabled");
    this.currentMacro = null;
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

  public boolean isEnabled() {
    return this.currentMacro != null;
  }

  public boolean isRunning() {
    return this.currentMacro != null && this.currentMacro.isEnabled();
  }

  @SubscribeEvent
  public void onTick(ClientTickEvent event) {
    if (this.currentMacro == null) {
      return;
    }

//    needed to remove for pause to work
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
    Logger.sendLog(getMessage(message));
  }

  public void send(String message) {
    Logger.sendMessage(getMessage(message));
  }

  public void error(String message) {
    Logger.sendError(getMessage(message));
  }

  public void warn(String message) {
    Logger.sendWarning(getMessage(message));
  }

  public String getMessage(String message) {
    return "[MacroHandler] " + message;
  }
}
