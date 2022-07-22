package com.jelly.MightyMiner.macros;

import net.minecraft.client.Minecraft;

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

    public void onTick() {}

    public void onLastRender() {}

    public boolean isEnabled(){
        return enabled;
    }
}
