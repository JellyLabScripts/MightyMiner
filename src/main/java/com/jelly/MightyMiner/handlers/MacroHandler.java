package com.jelly.MightyMiner.handlers;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.config.Config;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.macros.macros.*;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.UngrabUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class MacroHandler {
    public static List<Macro> macros = new ArrayList<>();
    public static Minecraft mc = Minecraft.getMinecraft();


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
        if (!enabled || mc.thePlayer == null || mc.theWorld == null)
            return;

        for (Macro process : macros) {
            if (process.isEnabled()) {
                process.onLastRender();
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

       enabled = false;
       if(flag)
           LogUtils.addMessage("Disabled script");

       UngrabUtils.regrabMouse();
    }




}
