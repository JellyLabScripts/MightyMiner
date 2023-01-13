package com.jelly.MightyMiner.handlers;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.events.ReceivePacketEvent;
import com.jelly.MightyMiner.macros.Macro;
import com.jelly.MightyMiner.macros.macros.*;
import com.jelly.MightyMiner.render.BlockRenderer;
import com.jelly.MightyMiner.utils.*;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.HypixelUtils.SkyblockInfo;
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

    List<BlockPos> coords;

    public static void initializeMacro(){
       macros.add(new GemstoneMacro());
       macros.add(new PowderMacro());
       macros.add(new MithrilMacro());
        macros.add(new AOTVMacro());
    }

    @SubscribeEvent
    public void onTickPlayer(TickEvent.ClientTickEvent tickEvent) {

        coords = MightyMiner.coordsConfig.getSelectedRoute().valueList();
        scanBlockingVisionBlocks();

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

        if(SkyblockInfo.onCrystalHollows()) {
            if (MightyMiner.coordsConfig.getSelectedRoute().valueList() != null)
                drawRoutes(MightyMiner.coordsConfig.getSelectedRoute().valueList(), event);

            if (MightyMiner.config.drawBlocksBlockingAOTV && blocksBlockingVision.size() > 0) {
                for (BlockPos pos : blocksBlockingVision) {
                    DrawUtils.drawBlockBox(pos, MightyMiner.config.aotvVisionBlocksColor, 2f);
                }
            }
        }

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
       MightyMiner.mobKiller.Disable();
       if(flag)
           LogUtils.addMessage("Disabled script");

       UngrabUtils.regrabMouse();
    }


    //region AOTV

    private final ArrayList<BlockPos> blocksBlockingVision = new ArrayList<>();


    public static void drawRoutes(List<BlockPos> coords, RenderWorldLastEvent event) {
        blockRenderer.renderAABB(event);
        if (MightyMiner.config.highlightRouteBlocks) {
            int i = 0;
            for (BlockPos pos : coords) {
                DrawUtils.drawBlockBox(pos, MightyMiner.config.routeBlocksColor, 2f);
                DrawUtils.drawWaypointText(String.valueOf(i), pos.getX() + 0.5, pos.getY() + 1.6, pos.getZ() + 0.5, event.partialTicks);
                i++;
            }
        }

        if (MightyMiner.config.showRouteLines) {
            DrawUtils.drawCoordsRoute(coords, event);
        }
    }

    public void scanBlockingVisionBlocks() {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (!blocksBlockingVision.isEmpty())
            blocksBlockingVision.clear();

        if (!MightyMiner.config.drawBlocksBlockingAOTV) return;

        if (coords != null && coords.size() > 1) {

            for (int i = 0; i < coords.size() - 1; i++) {
                BlockPos pos1 = new BlockPos(coords.get(i).getX(), coords.get(i).getY(), coords.get(i).getZ());
                BlockPos pos2 = new BlockPos(coords.get(i + 1).getX(), coords.get(i + 1).getY(), coords.get(i + 1).getZ());

                blocksBlockingVision.addAll(BlockUtils.GetAllBlocksInline(pos1, pos2));
            }

            BlockPos pos1 = new BlockPos(coords.get(coords.size() - 1).getX(), coords.get(coords.size() - 1).getY(), coords.get(coords.size() - 1).getZ());
            BlockPos pos2 = new BlockPos(coords.get(0).getX(), coords.get(0).getY(), coords.get(0).getZ());

            blocksBlockingVision.addAll(BlockUtils.GetAllBlocksInline(pos1, pos2));
        }
    }

    //endregion
}
