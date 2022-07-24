package com.jelly.MightyMiner.baritone;

import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

public abstract class Baritone{
    protected Minecraft mc = Minecraft.getMinecraft();
    protected boolean enabled = false;

    protected abstract void onEnable(BlockPos destinationBlock);

    protected abstract void onDisable();

    public final void enableBaritone(BlockPos destinationBlock){
        onEnable(destinationBlock);
        enabled = true;
    }
    public final void disableBaritone(){
        onDisable();
        enabled = false;
    }

    public boolean isEnabled(){
        return enabled;
    }

    public void onTickEvent(){};
    public void onRenderEvent(){};
}
