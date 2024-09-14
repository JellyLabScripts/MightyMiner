package com.jelly.mightyminerv2.util;

import cc.polyfrost.oneconfig.utils.Notifications;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StringUtils;

public abstract class Logger {

  private static Minecraft mc = Minecraft.getMinecraft();
  private static String lastDebugMessage = null;
  private static String lastWebhookMessage = null;

  public abstract String getName();

  public static void addMessage(String text) {
    if (mc.thePlayer == null || mc.theWorld == null) {
      System.out.println("MightyMiner" + StringUtils.stripControlCodes(text));
    } else {
      mc.thePlayer.addChatMessage(new ChatComponentText(text));
    }
  }

  public static void sendMessage(final String message) {
    addMessage("§l§2[Mighty Miner] §8» §a" + message);
  }

  public static void sendWarning(final String message) {
    addMessage("§l§6[Mighty Miner] §8» §e" + message);
  }

  public static void sendError(final String message) {
    addMessage("§l§4[Mighty Miner] §8» §c" + message);
  }

  public static void sendLog(final String message) {
    if (lastDebugMessage != null && lastDebugMessage.equals(message)) {
      return;
    }
    if (MightyMinerConfig.debugMode && mc.thePlayer != null) {
      addMessage("§l§2[Mighty Miner] §8» §7" + message);
    } else {
      System.out.println("[Mighty Miner] " + message);
    }
    lastDebugMessage = message;
  }

  public static void sendNotification(String title, String message, Long duration) {
    if (lastWebhookMessage != null && lastWebhookMessage.equals(message)) {
      return;
    }
    Notifications.INSTANCE.send(title, message, duration);
  }

  protected void log(String message) {
    log(formatMessage(message));
  }

  protected void send(String message) {
    send(formatMessage(message));
  }

  protected void error(String message) {
    error(formatMessage(message));
  }

  protected void warn(String message) {
    warn(formatMessage(message));
  }

  protected String formatMessage(String message) {
    return "[" + getName() + "] " + message;
  }
}
