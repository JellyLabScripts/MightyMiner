package com.jelly.MightyMiner.handlers;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.macros.macros.*;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.UngrabUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
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
        if (!enabled && MightyMiner.config.showAlwaysDrawings && mc.thePlayer != null && mc.theWorld != null) {
            // Have to find better solution for this...
            // switch is there for possible next global drawings
            switch (MightyMiner.config.macroType) {
                case 3:
                    AOTVMacro.drawRoutes(MightyMiner.coordsConfig.getSelectedRoute().valueList(), event);
                    break;
            }
            return;
        }
        if (!enabled || mc.thePlayer == null || mc.theWorld == null)
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

    public static void onPacketReceive(Packet<?> packet) {
        if (!enabled || mc.thePlayer == null || mc.theWorld == null)
            return;

        for (Macro process : macros) {
            if (process.isEnabled()) {
                process.onPacketReceived(packet);
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




}
