package com.jelly.MightyMiner.handlers;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.events.ReceivePacketEvent;
import com.jelly.MightyMiner.features.MobKiller;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.macros.macros.*;
import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.utils.DrawUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.SkyblockInfo;
import com.jelly.MightyMiner.utils.UngrabUtils;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class MacroHandler {
    public static List<Macro> macros = new ArrayList<>();
    public static Minecraft mc = Minecraft.getMinecraft();

    public static boolean pickaxeSkillReady = true;

    static boolean enabled = false;
    static BlockRenderer blockRenderer = new BlockRenderer();

    public static void initializeMacro(){
       macros.add(new GemstoneMacro());
       macros.add(new OreMacro());
       macros.add(new PowderMacro());
       macros.add(new MithrilMacro());
        macros.add(new AOTVMacro());
    }

    @SubscribeEvent
    public void onTickPlayer(TickEvent.ClientTickEvent tickEvent) {

        if (!enabled || mc.thePlayer == null || mc.theWorld == null)
            return;

        for (Macro process : macros) {
            if (process.isEnabled()) {
                process.onTick(tickEvent.phase);
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(final RenderWorldLastEvent event) {
        if(mc.theWorld == null || mc.thePlayer == null)
            return;

        if(SkyblockInfo.onCrystalHollows() && MightyMiner.coordsConfig.getSelectedRoute().valueList() != null)
            drawRoutes(MightyMiner.coordsConfig.getSelectedRoute().valueList(), event);

        if (!enabled)
            return;

        for (Macro process : macros) {
            if (process.isEnabled()) {
                process.onLastRender(event);
            }
        }
    }

    @SubscribeEvent
    public void onRenderOverlay(RenderGameOverlayEvent event) {
        if (!enabled || mc.thePlayer == null || mc.theWorld == null)
            return;

        for (Macro process : macros) {
            if (process.isEnabled()) {
                process.onOverlayRenderEvent(event);
            }
        }
    }


    @SubscribeEvent
    public void onMessageReceived(ClientChatReceivedEvent event) {
        String message = ChatFormatting.stripFormatting(event.message.getUnformattedText());
        if(message.equals("You used your Mining Speed Boost Pickaxe Ability!")) {
            pickaxeSkillReady = false;
        } else if(message.equals("Mining Speed Boost is now available!")) {
            pickaxeSkillReady = true;
        }



        if (!enabled || mc.thePlayer == null || mc.theWorld == null)
            return;

        if (message.contains("Your pass to the Crystal Hollows will expire in 1 minute")) {
            if (MightyMiner.config.autoRenewCrystalHollowsPass) {
                LogUtils.addMessage("Auto renewing Crystal Hollows pass");
                mc.thePlayer.sendChatMessage("/purchasecrystallhollowspass");
            }
        }

        for (Macro process : macros) {
            if (process.isEnabled()) {
                process.onMessageReceived(event.message.getUnformattedText());
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceived(ReceivePacketEvent event) {
        if (!enabled || mc.thePlayer == null || mc.theWorld == null)
            return;

        for (Macro process : macros) {
            if (process.isEnabled()) {
                process.onPacketReceived(event.packet);
            }
        }
    }


    public static void startScript(Macro macro){
        if(!macro.isEnabled()) {
            LogUtils.addMessage("Enabled script");
            macro.toggle();
            enabled = true;
            if (MightyMiner.config.mouseUngrab) {
                UngrabUtils.ungrabMouse();
            }
        }
    }
    public static void startScript(int index){
        startScript(macros.get(index));
    }

    public static void disableScript() {

       boolean flag = false;
       for(Macro macro : macros){
           if(macro.isEnabled()) {
               macro.toggle();
               flag = true;
           }
       }
       pickaxeSkillReady = true;
       enabled = false;
       MobKiller.Disable();
       if(flag)
           LogUtils.addMessage("Disabled script");

       UngrabUtils.regrabMouse();
    }


    public static void drawRoutes(List<BlockPos> coords, RenderWorldLastEvent event) {
        blockRenderer.renderAABB(event);
        if (MightyMiner.config.highlightRouteBlocks) {
            coords.forEach(coord -> blockRenderer.renderAABB(coord, MightyMiner.config.routeBlocksColor));
        }

        if (MightyMiner.config.showRouteLines) {
            DrawUtils.drawCoordsRoute(coords, event);
        }
    }




}
