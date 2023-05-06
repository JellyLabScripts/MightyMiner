package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.utils.Clock;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

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
            pingAlertClock.schedule(100);
            numPings--;
        } else if (!pingAlertClock.isScheduled()) {
            pingAlertClock.schedule(100);
        }
    }
}
