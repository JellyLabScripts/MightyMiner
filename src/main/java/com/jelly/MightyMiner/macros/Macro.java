package com.jelly.MightyMiner.macros;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.baritone.automine.logging.Logger;
import com.jelly.MightyMiner.events.BlockChangeEvent;
import com.jelly.MightyMiner.features.FuelFilling;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.macros.CommissionMacro;
import com.jelly.MightyMiner.macros.macros.MithrilMacro;

import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.crypto.Mac;

public abstract class Macro {
    protected Minecraft mc = Minecraft.getMinecraft();
    protected boolean enabled = false;
    public static boolean brokeBlockUnderPlayer = false;

    public void toggle() {
        enabled = !enabled;
        FuelFilling.currentState = FuelFilling.states.NONE;
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

    public void onBlockChange(BlockChangeEvent event) {
    }

    public void checkMiningSpeedBoost() {

        if (MightyMiner.config.useMiningSpeedBoost && MacroHandler.pickaxeSkillReady) {
            int slotCache = mc.thePlayer.inventory.currentItem;
            int targetSlot = MightyMiner.config.blueCheeseOmeletteToggle ? PlayerUtils.getItemInHotbarFromLore(true, "Blue Cheese") : PlayerUtils.getItemInHotbar(true, "Pick", "Gauntlet", "Drill");

            if(targetSlot == -1) {
                Logger.playerLog("Blue cheese drill not found. Disabled blue cheese swap");
                MightyMiner.config.blueCheeseOmeletteToggle = false;
                targetSlot = PlayerUtils.getItemInHotbar(true, "Pick", "Gauntlet", "Drill");
                if (targetSlot == -1) {
                    Logger.playerLog("Pickaxe not found. Disabling mining speed boost");
                    MightyMiner.config.useMiningSpeedBoost = false;
                    return;
                }
            }
            mc.thePlayer.inventory.currentItem = targetSlot;
            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getStackInSlot(targetSlot));
            mc.thePlayer.inventory.currentItem = slotCache;

            MacroHandler.pickaxeSkillReady = false;
        }
    }

}
