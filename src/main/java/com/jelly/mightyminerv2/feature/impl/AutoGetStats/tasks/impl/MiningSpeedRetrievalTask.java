package com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.impl;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.AbstractInventoryTask;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.TaskStatus;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.helper.Clock;
import net.minecraft.client.Minecraft;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A task that retrieves the Mining Speed value from the player's SkyBlock GUI.
 */
public class MiningSpeedRetrievalTask extends AbstractInventoryTask<Integer> {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Clock timer = new Clock();
    private Integer miningSpeed;
    private static final Pattern MINING_SPEED_PATTERN = Pattern.compile("Mining Speed\\s+([\\d,]+\\.?\\d*)");

    @Override
    public void init() {
        taskStatus = TaskStatus.RUNNING;

        InventoryUtil.holdItem(MightyMinerConfig.miningTool);

        if(!InventoryUtil.getInventoryName().equals("SkyBlock Menu")) {
            if (mc.currentScreen != null) {
                InventoryUtil.closeScreen();
            }
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
            Matcher matcher = MINING_SPEED_PATTERN.matcher(lore);
            if (matcher.find()) {
                try {
                    // The number - for example, "2,000" or "123.45" or "1,234.56"
                    String numberAsString = matcher.group(1);
                    String cleanNumberString = numberAsString.replace(",", "");

                    // Mining speeds from the 'sbmenu' can be a decimal
                    double rawMiningSpeed = Double.parseDouble(cleanNumberString);
                    miningSpeed = (int) rawMiningSpeed;

                    taskStatus = TaskStatus.SUCCESS;
                    return;
                } catch (NumberFormatException e) {
                    taskStatus = TaskStatus.FAILURE;
                    error = "Found 'Mining Speed' but failed to parse the number in line: '" + lore + "'. Exiting with error: " + e.getMessage();
                    return;
                }
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
