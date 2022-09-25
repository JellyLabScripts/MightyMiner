package com.jelly.MightyMiner.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerUtils {

    private static final List<String> npcList = new ArrayList<String>(){
        {
            add("Golden Goblin");
            add("Goblin");
            add("Weakling");
            add("Fireslinger");
            add("Executive Viper");
            add("Grunt");
            add("Eliza");
            add("Fraiser");
            add("Wilson");
            add("Ceanna");
            add("Carlton");
            add("Treasure Hoarder");
            add("Star Centry");

        }
    };

    private static Minecraft mc = Minecraft.getMinecraft();
    public static boolean hasStoppedMoving(){
        return mc.thePlayer.posX - mc.thePlayer.lastTickPosX == 0 &&
                mc.thePlayer.posY - mc.thePlayer.lastTickPosY == 0 &&
                mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ == 0;
    }
    public static int getItemInHotbar(final String... itemName) {
        for (int i = 0; i < 8; ++i) {
            final ItemStack is = mc.thePlayer.inventory.getStackInSlot(i);
            for(String s : itemName) {
                if (is != null && StringUtils.stripControlCodes(is.getDisplayName()).contains(s)) {
                    return i;
                }
            }
        }
        return 0;
    }

    public static boolean hasPlayerInsideRadius(int radius){
        for(Entity e :  mc.theWorld.getLoadedEntityList()){
            if(!(e instanceof EntityPlayer))
                continue;
            if(e.isInvisible() || e.equals(mc.thePlayer) || npcList.contains(e.getDisplayName().getUnformattedText()))
                continue;
            for(String s : npcList){
                if(e.getDisplayName().getUnformattedText().contains(s)){
                    return false;
                }
            }
            if(e.getDistanceToEntity(mc.thePlayer) < radius) {
                LogUtils.addMessage("Entity found: " + e.getDisplayName());
                return true;
            }
        }
        return false;
    }


    public static void warpBackToIsland(){
        mc.thePlayer.sendChatMessage("/warp home");
    }

    public static void centerToBlock() {
        if (mc.thePlayer != null) {
            BlockPos block = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
            mc.thePlayer.setPosition(block.getX() + 0.5, mc.thePlayer.posY, block.getZ() + 0.5);
        }
    }
}
