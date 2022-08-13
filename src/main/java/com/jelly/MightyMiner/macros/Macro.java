package com.jelly.MightyMiner.macros;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public abstract class Macro {
    protected Minecraft mc = Minecraft.getMinecraft();
    protected boolean enabled = false;

    public void toggle() {
        enabled = !enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    protected abstract void onEnable();

    protected abstract void onDisable();

    public void onTick(TickEvent.Phase phase) {}

    public void onKeyBindTick() {}

    public void onLastRender() {}

    public void onOverlayRenderEvent(RenderGameOverlayEvent event) {}

    public void onPacketReceived(Packet<?> packet) {}

    public void onMessageReceived(String message) {}

    public boolean isEnabled(){
        return enabled;
    }
}
