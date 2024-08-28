package com.jelly.mightyminerv2.util;

import cc.polyfrost.oneconfig.utils.Notifications;
import com.jelly.mightyminerv2.config.MightyMinerConfig;
import jline.internal.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class LogUtil {
  private static final Long MsgAppearTime = 1000L;
  private static final Minecraft mc = Minecraft.getMinecraft();
  private static String lastDebugMessage = null;
  private static String lastWebhookMessage = null;

  public synchronized static void sendMessage(ChatComponentText chat) {
    if (mc.thePlayer == null || mc.theWorld == null) {
      System.out.println("Mighty Miner" + chat.getUnformattedText());
    } else {
      mc.thePlayer.addChatMessage(chat);
    }
  }

  public static void send(final String message) {
    sendMessage(new ChatComponentText("§l§2[Mighty Miner] §8» §a" + message));
  }

  public static void warn(final String message) {
    sendMessage(new ChatComponentText("§l§6[Mighty Miner] §8» §e" + message));
  }

  public static void error(final String message) {
    sendMessage(new ChatComponentText("§l§4[Mighty Miner] §8» §c" + message));
  }

  public static void log(final String message) {
    if (lastDebugMessage != null && lastDebugMessage.equals(message)) {
      return;
    }
    if (MightyMinerConfig.debugMode && mc.thePlayer != null) {
      sendMessage(new ChatComponentText("§l§2[Mighty Miner] §8» §7" + message));
    } else {
      System.out.println("[Mighty Miner] " + message);
    }
    lastDebugMessage = message;
  }

  public static void sendNotification(String title, String message, @Nullable Long duration) {
    if (lastWebhookMessage != null && lastWebhookMessage.equals(message)) {
      return;
    }
    if (duration == null) {
      duration = MsgAppearTime;
    }
    Notifications.INSTANCE.send(title, message, duration);
  }
}