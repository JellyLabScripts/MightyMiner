package com.jelly.MightyMinerV2.Macro;

import baritone.api.utils.Rotation;
import cc.polyfrost.oneconfig.events.event.ReceivePacketEvent;
import com.jelly.MightyMinerV2.Feature.FeatureManager;
import com.jelly.MightyMinerV2.Util.LogUtil;
import com.jelly.MightyMinerV2.Util.helper.Clock;
import jdk.nashorn.internal.objects.annotations.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public abstract class AbstractMacro {
    public static final Minecraft mc = Minecraft.getMinecraft();
    private boolean enabled = false;
    private static Clock rewarDelay = new Clock();
    private static Clock analyzeDelay = new Clock();
    private static Clock DelayBeforeBreak = new Clock();
    private static Clock BreakTime = new Clock();


    @Setter
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Setter
    public void setAnalyzeDelay(long delay) {
        analyzeDelay.schedule(delay);
    }


    @Setter
    public void setBreakTime(double delay, double timeBefore) {
        BreakTime.schedule((long) delay);
        DelayBeforeBreak.schedule((long) timeBefore);
    }

    public boolean isEnableAndNoFeature() {
        return enabled && !FeatureManager.getInstance().shouldPauseMacroExecution();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void onEnable() {
        enabled = true;
    }

    public void onDisable() {
        enabled = false;
    }

    public void onLastRender() {
    }

    public void onChatMessageReceived(String message) {
    }

    public void onOverlayRender(RenderGameOverlayEvent.Post event) {
    }

    public void onPacketReceived(ReceivePacketEvent event) {
        if (!(event.packet instanceof S08PacketPlayerPosLook)) return;

        S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook) event.packet;
        Rotation packetRotation = new Rotation(packet.getYaw(), packet.getPitch());
        Rotation playerRotation = new Rotation(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
        Rotation difference = packetRotation.subtract(playerRotation);

//        if detect player rotation is not equal to packet rotation
        if (difference.getYaw() != 0 || difference.getPitch() != 0) {
            LogUtil.send("Player rotation hasn't change! Please disable any mod that blocks rotation packet!", LogUtil.ELogType.ERROR);

        }
    }

    public abstract void updateState();
    public abstract void invokeState();

    public void enable() {
        setAnalyzeDelay(60_000);
        setEnabled(true);
        onEnable();
    }

    public void disable() {
        setEnabled(false);
        onDisable();
    }





}
