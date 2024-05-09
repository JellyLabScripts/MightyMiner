package com.jelly.MightyMinerV2.Util;

import cc.polyfrost.oneconfig.utils.Notifications;
import com.jelly.MightyMinerV2.Config.MightyMinerConfig;
import jline.internal.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;

public class LogUtil {

    public static enum ELogType {
        SUCCESS,
        WARNING,
        ERROR,
        DEBUG
    }

    private static final Long MsgAppearTime = 1000L;
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static String lastDebugMessage = null;
    private static String lastWebhookMessage = null;

    public synchronized static void sendLog(ChatComponentText chat) {
        if (mc.thePlayer == null || mc.theWorld == null) System.out.println("Mighty Miner" + chat.getUnformattedText());
        else mc.thePlayer.addChatMessage(chat);
    }

    public static void send(String message, ELogType type) {
        ChatComponentText chat = new ChatComponentText(message);
        switch (type) {
            case SUCCESS:
                sendLog(new ChatComponentText("§l§2[Mighty Miner] §8» §a" + message));
                break;
            case WARNING:
                sendLog(new ChatComponentText("§l§6[Mighty Miner] §8» §e" + message));
                break;
            case ERROR:
                sendLog(new ChatComponentText("§l§4[Mighty Miner] §8» §c" + message));
                break;
            case DEBUG:
                if (lastDebugMessage != null && lastDebugMessage.equals(message)) return;
                if (MightyMinerConfig.debugMode && mc.thePlayer != null)
                    sendLog(new ChatComponentText("§l§9[Mighty Miner] §8» §9" + message));
                else
                    System.out.println("[Mighty Miner] " + message);
                lastDebugMessage = message;
        }
    }

    public static void sendNotification(String title, String message, @Nullable Long duration) {
        if (lastWebhookMessage != null && lastWebhookMessage.equals(message)) return;
        if (duration == null) duration = MsgAppearTime;
        Notifications.INSTANCE.send(title, message, duration);
    }
}