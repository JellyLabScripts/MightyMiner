package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.utils.DrawUtils;
import com.jelly.MightyMiner.utils.HypixelUtils.NpcUtil;
import com.jelly.MightyMiner.utils.PlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.List;

public class PlayerESP {

    private final Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onRenderWorldLastPlayerESP(RenderWorldLastEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;
        if(!MightyMiner.config.playerESP) return;

        List<EntityPlayer> players = mc.theWorld.playerEntities;

        for (EntityPlayer player : players) {
            if (player == mc.thePlayer) continue;
            if (NpcUtil.isNpc(player)) continue;
            if (player.getDistanceToEntity(mc.thePlayer) > 50) continue;

            DrawUtils.drawEntity(player, MightyMiner.config.playerESPColor, 3, event.partialTicks);

            if (!PlayerUtils.entityIsVisible(player)) {
                DrawUtils.drawText(player.getName(), player.posX, player.posY + player.height + 0.3, player.posZ, false, 0.7f);
            }
        }
    }
}
