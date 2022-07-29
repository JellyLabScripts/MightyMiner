package com.jelly.MightyMiner.baritone;

import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public abstract class Baritone{
    protected Minecraft mc = Minecraft.getMinecraft();
    protected boolean enabled = false;

    protected abstract void onEnable(BlockPos destinationBlock) throws Exception;

    protected abstract void onDisable();

    public final void enableBaritone(BlockPos destinationBlock) throws Exception{
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

    public void onTickEvent(TickEvent.Phase phase){}
    public void onRenderEvent(){}
    public void onOverlayRenderEvent(RenderGameOverlayEvent event){}
}
