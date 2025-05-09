package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import com.jelly.mightyminerv2.util.Logger;
import lombok.Getter;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.GuiOpenEvent;

public class ProfileFailsafe extends AbstractFailsafe {

    @Getter
    private static final ProfileFailsafe instance = new ProfileFailsafe();

    private static final String TRIGGER_PHRASE = "Profile";

    @Override
    public String getName() {
        return "ProfileFailsafe";
    }

    @Override
    public Failsafe getFailsafeType() {
        return Failsafe.PLAYER_PROFILE_OPEN;
    }

    @Override
    public int getPriority() { return 2; }

    @Override
    public boolean onGuiOpen(GuiOpenEvent event) {
        if (event.gui == null) {
            return false;
        }

        if (event.gui instanceof GuiChest) {
            GuiChest guiChest = (GuiChest) event.gui;

            if (guiChest.inventorySlots instanceof ContainerChest) {
                ContainerChest container = (ContainerChest) guiChest.inventorySlots;
                IInventory chestInventory = container.getLowerChestInventory();

                if (chestInventory != null && chestInventory.hasCustomName()) {
                    String inventoryName = StringUtils.stripControlCodes(chestInventory.getDisplayName().getUnformattedText());
                    if (inventoryName.toLowerCase().contains(TRIGGER_PHRASE.toLowerCase())) {
                        note("Detected inventory open with name containing " + inventoryName);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean react() {
        if (mc.currentScreen != null && mc.thePlayer != null) {
            mc.addScheduledTask(() -> {
                if (mc.currentScreen != null && mc.thePlayer != null) {
                    mc.thePlayer.closeScreen();
                    note("Closing the menu... continuing");
                }
            });
        } else {
            warn("Menu already closed... continuing");
        }
        return true;
    }
}