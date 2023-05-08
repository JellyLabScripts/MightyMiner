package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.utils.Clock;
import com.jelly.MightyMiner.utils.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class PingAlert {
    private static final Clock pingAlertClock = new Clock();
    private static int numPings = 15;
    private final Minecraft mc = Minecraft.getMinecraft();
    private static boolean pingAlertPlaying = false;

    public static void sendPingAlert() {
        pingAlertPlaying = true;
        numPings = 15;
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!pingAlertPlaying) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (event.phase == TickEvent.Phase.START) return;

        if (numPings <= 0) {
            pingAlertPlaying = false;
            numPings = 15;
            return;
        }

        if (pingAlertClock.isScheduled() && pingAlertClock.passed()) {
            mc.theWorld.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, "random.orb", 10.0F, 1.0F, false);
            try {
                SystemTray tray = SystemTray.getSystemTray();
                createNotification("You got staff checked go in minecraft and verify the situation", tray, TrayIcon.MessageType.WARNING);
            } catch (UnsupportedOperationException e) {
                LogUtils.debugLog("Notifications are not supported on this system");
            }
            pingAlertClock.schedule(500);
            numPings--;
        } else if (!pingAlertClock.isScheduled()) {
            pingAlertClock.schedule(500);
        }
    }

    public static void createNotification(String text, SystemTray tray, TrayIcon.MessageType messageType) {
        new Thread(() -> {


            if(Minecraft.isRunningOnMac) {
                try {
                    Runtime.getRuntime().exec(new String[]{"osascript", "-e", "display notification"});
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                TrayIcon trayIcon = new TrayIcon(new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR), "Mighty Miner Failsafe Notification");
                trayIcon.setToolTip("Mighty Miner Failsafe Notification");
                try {
                    tray.add(trayIcon);
                } catch (AWTException e) {
                    throw new RuntimeException(e);
                }

                trayIcon.displayMessage("Mighty Miner - Failsafes", text, messageType);

                tray.remove(trayIcon);
            }

        }).start();

    }
}
