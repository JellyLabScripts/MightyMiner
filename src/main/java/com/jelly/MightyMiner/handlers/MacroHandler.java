package com.jelly.MightyMiner.handlers;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.config.Config;
import com.jelly.MightyMiner.events.ReceivePacketEvent;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.macros.macros.*;
import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.utils.DrawUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.UngrabUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StringUtils;
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
        if (!enabled || mc.thePlayer == null || mc.theWorld == null)
            return;

        if(MightyMiner.coordsConfig.getSelectedRoute().valueList() != null)
            drawRoutes(MightyMiner.coordsConfig.getSelectedRoute().valueList(), event);

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
        if (!enabled || mc.thePlayer == null || mc.theWorld == null)
            return;

        for (Macro process : macros) {
            if (process.isEnabled()) {
                process.onMessageReceived(event.message.getUnformattedText());
            }
        }
        try {
            String message = StringUtils.stripControlCodes(event.message.getUnformattedText());
            if (message.contains(":") || message.contains(">")) return;
            if(message.startsWith("You used your")) {
                pickaxeSkillReady = false;
            } else if(message.endsWith("is now available!")) {
                pickaxeSkillReady = true;
            }
        } catch (Exception ignored) {}
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
