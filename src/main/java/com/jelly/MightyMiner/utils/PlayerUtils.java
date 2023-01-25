package com.jelly.MightyMiner.utils;

import com.jelly.MightyMiner.utils.HypixelUtils.NpcUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.*;

import java.util.ArrayList;

public class PlayerUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();
    public static boolean hasStoppedMoving(){
        return mc.thePlayer.posX - mc.thePlayer.lastTickPosX == 0 &&
                mc.thePlayer.posY - mc.thePlayer.lastTickPosY == 0 &&
                mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ == 0;
    }

    public static boolean hasOpenContainer() {
        return mc.currentScreen != null && !(mc.currentScreen instanceof net.minecraft.client.gui.GuiChat);
    }

    public static void sendPingAlert() {
        new Thread(() -> {
            for (int i = 0; i < 15; i++) {
                mc.theWorld.playSound(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, "random.orb", 10.0F, 1.0F, false);
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static int getItemInHotbar(boolean returnErrorCode, final String... itemName) {
        for (int i = 0; i < 8; ++i) {
            final ItemStack is = mc.thePlayer.inventory.getStackInSlot(i);
            for(String s : itemName) {
                if (is != null && StringUtils.stripControlCodes(is.getDisplayName()).contains(s)) {
                    return i;
                }
            }
        }
        return returnErrorCode ? -1 : 0;
    }
    public static int getItemInHotbar(final String... itemName) {
        return getItemInHotbar(false, itemName);
    }

    public static int getItemInHotbarWithBlackList(boolean returnErrorCode, String blackListedLore, final String... itemName) {
        for (int i = 0; i < 8; ++i) {
            final ItemStack is = mc.thePlayer.inventory.getStackInSlot(i);
            if(blackListedLore != null && containsLore(is, blackListedLore))
                continue;
            for(String s : itemName) {
                if (is != null && StringUtils.stripControlCodes(is.getDisplayName()).contains(s)) {
                    return i;
                }
            }
        }
        return returnErrorCode ? -1 : 0;
    }
    public static int getItemInHotbarWithBlackList(String blackListedLore, final String... itemName) {
        return getItemInHotbarWithBlackList(false, blackListedLore, itemName);
    }
    public static int getItemInHotbarFromLore(boolean returnErrorCode, final String lore) {
        for (int i = 0; i < 8; ++i) {
            if(containsLore(mc.thePlayer.inventory.mainInventory[i], lore))
                return i;
        }
        return returnErrorCode ? -1 : 0;
    }
    public static int getItemInHotbarFromLore(final String lore) {
        return getItemInHotbarFromLore(false, lore);
    }

    public static boolean notAtCenter(){
        return notAtCenter(AngleUtils.get360RotationYaw());
    }

    public static boolean notAtCenter(float rotationYawAxis){
        return !((rotationYawAxis % 180 == 0) ?
                Math.abs(mc.thePlayer.posX) % 1 >= 0.3f && Math.abs(mc.thePlayer.posX) % 1 <= 0.7f : Math.abs(mc.thePlayer.posZ) % 1 >= 0.3f && Math.abs(mc.thePlayer.posZ) % 1 <= 0.7f);
    }

    public static boolean containsLore(ItemStack item, String lore) {
        ArrayList<String> lores = new ArrayList<>();
        if(item != null)
            lores = getItemLore(item);

        if(lores.isEmpty())
            return false;

        for(String s : lores) {
            if (!s.isEmpty() && s.contains(lore)) {
                return true;
            }
        }

        return false;
    }

    public static ArrayList<String> getItemLore(ItemStack item) {
        NBTTagList loreTag = item.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
        ArrayList<String> loreList = new ArrayList<>();
        for (int i = 0; i < loreTag.tagCount(); i++) {
            loreList.add(StringUtils.stripControlCodes(loreTag.getStringTagAt(i)));
        }
        return loreList;
    }

    public static boolean hasPlayerInsideRadius(int radius){
        for(Entity e :  mc.theWorld.getLoadedEntityList()){

            if(!(e instanceof EntityPlayer) || e == mc.thePlayer) continue;

            if(NpcUtil.isNpc(e))
                continue;

            if(e.getDistanceToEntity(mc.thePlayer) < radius) {
                LogUtils.debugLog("Entity found");
                return true;
            }
        }
        return false;
    }

    public static boolean hasEntityInRadius(int radius){
        for(Entity e :  mc.theWorld.getLoadedEntityList()){

            if(e.getDistanceToEntity(mc.thePlayer) < radius) {
                LogUtils.debugLog("Entity found");
                return true;
            }
        }
        return false;
    }
    public static boolean hasEmissaryInRadius(int radius){
        for(Entity e :  mc.theWorld.getLoadedEntityList()){
            if (!NpcUtil.isNpc(e)) continue;
            if(e.getDistanceToEntity(mc.thePlayer) < radius) {
                LogUtils.debugLog("Emissary found");
                return true;
            }
        }
        return false;
    }

    public static boolean hasMobsInRadius(int radius, String mobClass) {
        Class<?> type;
        try {
            type = Class.forName(mobClass);
        } catch (ClassNotFoundException e) {
            return false;
        }

        for (Entity e : mc.theWorld.getLoadedEntityList()) {
            if (!type.isInstance(e)) continue;
            if (NpcUtil.isNpc(e)) continue;

            if (e.getDistanceToEntity(mc.thePlayer) < radius) {
                LogUtils.debugLog("Found mob.");
                return true;
            }
        }
        return false;
    }

    public static <T> T getClosestMob(ArrayList<T> list) {
        T closest = null;

        for (T element : list) {
            Entity entity = (Entity) element;

            if (entity instanceof EntityLiving) {
                if (((EntityLiving) entity).getHealth() <= 0f) {
                    continue;
                }
            }

            if (entity.isDead) continue;

            if (closest == null) {
                closest = element;
                continue;
            }

            if (mc.thePlayer.getDistanceToEntity(entity) < mc.thePlayer.getDistanceToEntity((Entity) closest)) {
                closest = element;
            }
        }

        return closest;
    }

    public static boolean entityIsVisible(Entity entityToCheck) {
        Vec3 startPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        Vec3 endPos = new Vec3(entityToCheck.posX, entityToCheck.posY + entityToCheck.height / 2, entityToCheck.posZ);

        Vec3 direction = new Vec3(endPos.xCoord - startPos.xCoord, endPos.yCoord - startPos.yCoord, endPos.zCoord - startPos.zCoord);

        double maxDistance = startPos.distanceTo(endPos);

        double increment = 0.05;

        Vec3 currentPos = startPos;

        while (currentPos.distanceTo(startPos) < maxDistance) {

            ArrayList<BlockPos> blocks = AnyBlockAroundVec3(currentPos, 0.1f);

            boolean flag = false;

            for (BlockPos pos : blocks) {
                // Add the block to the list if it hasn't been added already
                if (!mc.theWorld.isAirBlock(pos)) {
                    flag = true;
                }
            }

            if (flag) {
                return false;
            }

            // Move along the line by the specified increment
            Vec3 scaledDirection = new Vec3(direction.xCoord * increment, direction.yCoord * increment, direction.zCoord * increment);
            currentPos = currentPos.add(scaledDirection);
        }
        return true;
    }

    public static ArrayList<BlockPos> AnyBlockAroundVec3(Vec3 pos, float around) {
        ArrayList<BlockPos> blocks = new ArrayList<>();
        for (double x = (pos.xCoord - around); x <= pos.xCoord + around; x += around) {
            for (double y = (pos.yCoord - around); y <= pos.yCoord + around; y += around) {
                for (double z = (pos.zCoord - around); z <= pos.zCoord + around; z += around) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    if (!blocks.contains(blockPos)) {
                        blocks.add(blockPos);
                    }
                }
            }
        }
        return blocks;
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
    public static boolean isNotMoving() {
         return mc.thePlayer.posX - mc.thePlayer.lastTickPosX == 0 && mc.thePlayer.posY - mc.thePlayer.lastTickPosY == 0 && mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ == 0;
    }

    public static boolean isNearPlayer(int radius){
        return hasPlayerInsideRadius(radius);
    }
}
