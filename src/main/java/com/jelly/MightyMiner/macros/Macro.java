package com.jelly.MightyMiner.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.logging.Logger;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
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
            int slotCache = mc.thePlayer.inventory.currentItem;
            int targetSlot = MightyMiner.config.blueCheeseOmeletteToggle ? PlayerUtils.getItemInHotbarFromLore(true, "Blue Cheese") : PlayerUtils.getItemInHotbar("Pick", "Gauntlet", "Drill");

            if(targetSlot == -1) {
                Logger.playerLog("Blue cheese drill not found. Disabled blue cheese swap");
                MightyMiner.config.blueCheeseOmeletteToggle = false;
                targetSlot = PlayerUtils.getItemInHotbar("Pick", "Gauntlet", "Drill");
            }
            mc.thePlayer.inventory.currentItem = targetSlot;
            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getStackInSlot(targetSlot));
            mc.thePlayer.inventory.currentItem = slotCache;

            MacroHandler.pickaxeSkillReady = false;
        }
    }

    public boolean isPaused() {
        return false;
    }

    public void Pause() {
    }

    public void Unpause() {

    }

}
