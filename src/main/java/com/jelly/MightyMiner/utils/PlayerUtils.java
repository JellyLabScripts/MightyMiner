package com.jelly.MightyMiner.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMagmaCube;
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
            if(containsLore(is, blackListedLore))
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
    public static boolean hasYogInRadius(int radius) {
        for (Entity e : mc.theWorld.getLoadedEntityList()) {
            if (!(e instanceof EntityMagmaCube)) continue;
            if (NpcUtil.isNpc(e)) continue;

            if (e.getDistanceToEntity(mc.thePlayer) < radius) {
                LogUtils.debugLog("Found yog.");
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

    public static Entity entityIsVisible(Entity entityToCheck) {
        Entity entity = null;

        // Raycast to entityToCheck and check if any blocks are in the way
        Vec3 playerPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        Vec3 entityPos = new Vec3(entityToCheck.posX, entityToCheck.posY + entityToCheck.getEyeHeight(), entityToCheck.posZ);
        MovingObjectPosition raycast = mc.theWorld.rayTraceBlocks(playerPos, entityPos, false, true, false);
        if (raycast != null) {
            // If the raycast hits a block, check if the block is the entityToCheck
            if (raycast.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                if (raycast.entityHit == entityToCheck) {
                    entity = entityToCheck;
                }
            }
        } else {
            // If the raycast doesn't hit a block, the entity is visible
            entity = entityToCheck;
        }

        return entity;
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

    public static boolean isNearPlayer(int radius){
        return hasPlayerInsideRadius(radius);
    }
}
