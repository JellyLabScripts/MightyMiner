package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.LogUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;

public class InventoryFullFailsafe extends AbstractFailsafe {

    private static final InventoryFullFailsafe instance = new InventoryFullFailsafe();

    public static InventoryFullFailsafe getInstance() {
        return instance;
    }

    public int getPriority() {
        return 5;
    }

    public boolean check() {
        InventoryPlayer inventory = Minecraft.getMinecraft().thePlayer.inventory;

        boolean isFull = isInventoryFull(inventory);

        return isFull;
    }

    private boolean isInventoryFull(InventoryPlayer inventory) {
        for (int i = 0; i < inventory.mainInventory.length; i++) {
            ItemStack stack = inventory.mainInventory[i];

            if (stack == null) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void react() {
        MacroManager.getInstance().disable();
        LogUtil.warn("Inventory is full! Disabeling macro.");
    }
}
