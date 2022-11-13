package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.config.Config;
import com.jelly.MightyMiner.handlers.KeybindHandler;
import com.jelly.MightyMiner.player.Rotation;
import com.jelly.MightyMiner.utils.AngleUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.inventory.Slot;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


public class YogKiller {

    private final Minecraft mc = Minecraft.getMinecraft();
    public static boolean enabled;

    private final Rotation rotation = new Rotation();

    private final String[] items = new String[]{"Juju"};


    // doesnt work idk why i gtg
    // NEED FIX someone help pls 
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent tickEvent) {
        if (mc.thePlayer == null) return;
        if (mc.theWorld == null) return;
        if (!enabled) return;

        if (!PlayerUtils.hasYogInRadius(MightyMiner.config.yogsRadius)) return;



        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityMagmaCube)) continue;
            float cacheYaw = mc.thePlayer.rotationYaw;
            float cachePitch = mc.thePlayer.rotationPitch;

            rotation.intLockAngle(
                    AngleUtils.getRequiredYaw(entity.posX - mc.thePlayer.posX, entity.posZ - mc.thePlayer.posZ),
                    AngleUtils.getRequiredPitch(entity.posX - mc.thePlayer.posX, entity.posY - (mc.thePlayer.posY + 1.62D), entity.posZ - mc.thePlayer.posZ),
                    250);
            int slot = mc.thePlayer.inventory.currentItem;
            mc.thePlayer.inventory.currentItem = PlayerUtils.getItemInHotbar("Bow");
            KeybindHandler.setKeyBindState(KeybindHandler.keybindUseItem, true);
            LogUtils.debugLog("Clicked :)");
            rotation.intLockAngle(cacheYaw, cachePitch, 250);
            mc.thePlayer.inventory.currentItem = slot;
            LogUtils.debugLog("Yog should be killed.");

        }

    }

    @SubscribeEvent
    public void onLastRender(RenderWorldLastEvent event){
        if (!enabled) return;
        if(rotation.rotating)
            rotation.update();
    }
}
