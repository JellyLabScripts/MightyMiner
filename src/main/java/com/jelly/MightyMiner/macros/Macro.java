package com.jelly.MightyMiner.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.handlers.MacroHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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

    public void onLastRender(RenderWorldLastEvent event) {}

    public void onOverlayRenderEvent(RenderGameOverlayEvent event) {}

    public void onPacketReceived(Packet<?> packet) {}

    public void onRenderEvent(RenderWorldEvent event){}

    public void onMessageReceived(String message) {}

    public boolean isEnabled(){
        return enabled;
    }

    public void useMiningSpeedBoost() {
        if (MightyMiner.config.useMiningSpeedBoost && MacroHandler.pickaxeSkillReady) {
            ItemStack itemInHand = mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem);
            if (itemInHand != null && (itemInHand.getItem().getUnlocalizedName().toLowerCase().contains("drill") || itemInHand.getItem().getUnlocalizedName().toLowerCase().contains("pickaxe") || itemInHand.getItem().getUnlocalizedName().toLowerCase().contains("gauntlet"))) {
                mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getStackInSlot(mc.thePlayer.inventory.currentItem));
                MacroHandler.pickaxeSkillReady = false;
            }
        }
    }

}
