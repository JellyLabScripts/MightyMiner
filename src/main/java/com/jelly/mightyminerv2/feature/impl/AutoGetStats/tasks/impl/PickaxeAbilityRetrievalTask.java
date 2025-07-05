package com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.impl;

import com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.AbstractInventoryTask;
import com.jelly.mightyminerv2.feature.impl.AutoGetStats.tasks.TaskStatus;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.helper.Clock;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;

/**
 * A task that retrieves the players pickaxe ability from the HOTM GUI.
 */
public class PickaxeAbilityRetrievalTask extends AbstractInventoryTask<BlockMiner.PickaxeAbility> {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Clock timer = new Clock();
    private BlockMiner.PickaxeAbility pickaxeAbility;

    @Override
    public void init() {
        pickaxeAbility = BlockMiner.PickaxeAbility.NONE;
        taskStatus = TaskStatus.RUNNING;

        // The case that the HOTM menu is already open
        if (!InventoryUtil.getInventoryName().equals("Heart of the Mountain")) {
            if (mc.currentScreen != null) {
                InventoryUtil.closeScreen();
            }

            mc.thePlayer.sendChatMessage("/hotm");
        }

        timer.schedule(1000);
    }

    @Override
    public void onTick() {
        if (!timer.passed() && timer.isScheduled()) {
            return;
        }

        if (isSelected("Pickobulus")) {
            pickaxeAbility = BlockMiner.PickaxeAbility.PICKOBULUS;
        } else {
            pickaxeAbility = BlockMiner.PickaxeAbility.MINING_SPEED_BOOST;
        }
        taskStatus = TaskStatus.SUCCESS;
    }

    private boolean isSelected(String name) {
        final Slot slot = InventoryUtil.getSlotOfItemInContainer(name);
        final Block block = slot != null
                ? Block.getBlockFromItem(slot.getStack().getItem())
                : null;

        return block != null && block == Blocks.emerald_block;
    }

    @Override
    public void end() {
        InventoryUtil.closeScreen();
    }

    @Override
    public BlockMiner.PickaxeAbility getResult() {
        return pickaxeAbility;
    }
}