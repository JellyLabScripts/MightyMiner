package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.event.PacketEvent;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.FifoQueue;
import lombok.Getter;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class LagDetector extends AbstractFeature {

    private static LagDetector instance;
    private final Clock lagTimer = new Clock();
    private final Clock recentlyLagged = new Clock();
    @Getter
    private long lastReceivedPacketTime = -1;
    private Vec3 lastPacketPosition = null;
    private final FifoQueue<Float> tpsHistory = new FifoQueue<>(20);
    private float timeJoined = 0;

    public static LagDetector getInstance() {
        if (instance == null) {
            instance = new LagDetector();
        }
        return instance;
    }

    @Override
    public String getName() {
        return "LagDetector";
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent event) {
        if (event.packet instanceof S03PacketTimeUpdate) {
            lastReceivedPacketTime = System.currentTimeMillis();
        }
    }


    public Vec3 getLastPacketPosition() {
        if (lastPacketPosition == null) {
            return mc.thePlayer.getPositionVector();
        }

        return lastPacketPosition;
    }

    public boolean isLagging() {
        return getTimeSinceLastTick() > 1.3;
    }

    public boolean wasJustLagging() {
        return recentlyLagged.isScheduled() && !recentlyLagged.passed();
    }

    public long getLaggingTime() {
        return System.currentTimeMillis() - lastReceivedPacketTime;
    }

    @SubscribeEvent
    public void onGameJoined(PlayerEvent.PlayerLoggedInEvent event) {
        timeJoined = System.currentTimeMillis();
        tpsHistory.clear();
    }

    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Received event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (!(event.packet instanceof S03PacketTimeUpdate)) return;
        long now = System.currentTimeMillis();
        float timeElapsed = (now - lastReceivedPacketTime) / 1000F;
        tpsHistory.add(clamp(20F / timeElapsed));
        lastReceivedPacketTime = now;
        lastPacketPosition = mc.thePlayer.getPositionVector();
    }


    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (lastReceivedPacketTime == -1) return;
        if (isLagging()) {
            recentlyLagged.schedule(900);
        }
        if (recentlyLagged.isScheduled() && recentlyLagged.passed()) {
            recentlyLagged.reset();
        }
    }

    public float getTickRate() {
        if (mc.thePlayer == null || mc.theWorld == null) return 0F;
        if (System.currentTimeMillis() - timeJoined < 5000) return 20F;

        int ticks = 0;
        float sumTickRates = 0f;
        for (float tickRate : tpsHistory) {
            if (tickRate > 0) {
                sumTickRates += tickRate;
                ticks++;
            }
        }
        return ticks > 0 ? sumTickRates / ticks : 0F;
    }

    public float getTimeSinceLastTick() {
        long now = System.currentTimeMillis();
        if (now - timeJoined < 5000) return 0F;
        return (now - lastReceivedPacketTime) / 1000F;
    }

    private float clamp(float value) {
        return Math.max((float) 0.0, Math.min((float) 20.0, value));
    }

}
