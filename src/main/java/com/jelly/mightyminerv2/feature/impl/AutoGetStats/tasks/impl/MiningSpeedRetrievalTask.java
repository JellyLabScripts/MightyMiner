package com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.impl;

import com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.AbstractInventoryTask;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.TaskStatus;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.helper.Clock;
import net.minecraft.client.Minecraft;

import java.util.List;

/**
 * A task that retrieves the Mining Speed value from the player's SkyBlock GUI.
 */
public class MiningSpeedRetrievalTask extends AbstractInventoryTask<Integer> {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Clock timer = new Clock();
    private Integer miningSpeed;

    @Override
    public void init() {
        taskStatus = TaskStatus.RUNNING;

        if (mc.currentScreen != null) {
            InventoryUtil.closeScreen();
        }

        if (!InventoryUtil.getInventoryName().equals("SkyBlock Menu")) {
            mc.thePlayer.sendChatMessage("/sbmenu");
        }

        timer.schedule(1000);
    }

    @Override
    public void onTick() {
        if (!timer.passed() && timer.isScheduled()) {
            return;
        }

        if (!InventoryUtil.getInventoryName().equals("SkyBlock Menu")) {
            taskStatus = TaskStatus.FAILURE;
            error = "Cannot open SkyBlock Menu";
            return;
        }

        List<String> loreList = InventoryUtil.getItemLoreFromOpenContainer("Your SkyBlock Profile");
        for (String lore : loreList) {
            if (!lore.contains("Mining Speed")) continue;
            try {
                String[] split = lore.replace(",", "").split(" ");
                miningSpeed = Integer.parseInt(split[split.length - 1]);
                taskStatus = TaskStatus.SUCCESS;
                return;
            } catch (Exception e) {
                taskStatus = TaskStatus.FAILURE;
                error = "Failed to parse mining speed in GUI";
                return;
            }
        }

        taskStatus = TaskStatus.FAILURE;
        error = "Failed to get mining speed in GUI";
    }

    @Override
    public void end() {
        InventoryUtil.closeScreen();
    }

    @Override
    public Integer getResult() {
        return miningSpeed;
    }
}
