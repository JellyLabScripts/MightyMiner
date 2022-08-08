package com.jelly.MightyMiner.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayerUtils {

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
}
