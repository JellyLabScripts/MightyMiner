package com.jelly.MightyMiner.utils.HypixelUtils;

import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.handlers.MacroHandler;
import com.jelly.MightyMiner.macros.macros.GemstoneMacro;
import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;

import com.jelly.MightyMiner.utils.InventoryUtils;
import com.jelly.MightyMiner.utils.LogUtils;
import com.jelly.MightyMiner.utils.PlayerUtils;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

public class FastMineUtils {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean shouldPreBreakBlock(float miningTicks, float blockDamage) {
        return !MacroHandler.macros.get(1).isEnabled() && MightyMiner.config.fastMine && blockDamage < 1f && miningTicks >= getRequiredMiningTicks();
    }

    public static float getRequiredMiningTicks() {
        IBlockState blockState = BlockUtils.getBlockState(mc.thePlayer.rayTrace(5, 1).getBlockPos());

        float blockStrength = getBlockStrength(blockState);
        float miningSpeed = (float) MacroHandler.miningSpeed - 120.0f;
        if (MacroHandler.miningSpeedActive) {
            for (Slot slot: InventoryUtils.getInventorySlots()) {
                if (slot.getStack() != null && slot.getStack().getDisplayName().contains("Drill") ||
                        slot.getStack().getDisplayName().contains("Gauntlet") ||
                        slot.getStack().getDisplayName().contains("Pick")) {
                    NBTTagList lore = InventoryUtils.getLore(slot.getStack());
                    if (lore != null) {
                        for (int i = 0; i < lore.tagCount(); i++) {
                            if (lore.get(i).toString().contains("Mining Speed Boost")) {
                                if (lore.tagCount() >= i + 1 && lore.get(i + 1).toString().contains("+")) {
                                    String integersInLore = lore.get(i + 1).toString().replaceAll("[\\D]", "");
                                    float miningSpeedBoost = Integer.parseInt(integersInLore.charAt(1) + "");
                                    return (blockStrength * 30.0f) / (miningSpeed * miningSpeedBoost) >= 4 ? (blockStrength * 30.0f) / (miningSpeed * miningSpeedBoost) : 4;
                                }
                            }
                        }
                    }
                }
            }
        }
        return (blockStrength * 30.0f) / miningSpeed >= 4 ? (blockStrength * 30.0f) / miningSpeed : 4;
    }

    public static float getBlockStrength(IBlockState blockState) {
        if (blockState == null) {
            return 30.0f;
        } else if (blockState.equals(Blocks.diamond_block.getDefaultState())) {
            return 50.0f;
        } else if (blockState.equals(Blocks.wool.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.GRAY)) ||
                blockState.equals(Blocks.stained_hardened_clay.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.CYAN))) {
            return 500.0f;
        } else if (blockState.equals(Blocks.gold_block.getDefaultState())) {
            return 600.0f;
        } else if (blockState.equals(Blocks.sponge.getDefaultState())) {
            return 500.0f;
        } else if (blockState.equals(Blocks.prismarine.getDefaultState().withProperty(BlockPrismarine.VARIANT, BlockPrismarine.EnumType.ROUGH)) ||
                blockState.equals(Blocks.prismarine.getDefaultState().withProperty(BlockPrismarine.VARIANT, BlockPrismarine.EnumType.DARK)) ||
                blockState.equals(Blocks.prismarine.getDefaultState().withProperty(BlockPrismarine.VARIANT, BlockPrismarine.EnumType.BRICKS))) {
            return 800.0f;
        } else if (blockState.equals(Blocks.wool.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.LIGHT_BLUE))) {
            return 1500.0f;
        } else if (blockState.equals(Blocks.stone.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.DIORITE_SMOOTH))) {
            return 2000.0f;
        } else if (blockState.equals(Blocks.stained_glass_pane.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.RED))) {
            return 2300.0f;
        } else if (blockState.equals(Blocks.stained_glass.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.RED))) {
            return 2500.0f;
        } else if (blockState.equals(Blocks.stained_glass_pane.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.PURPLE)) ||
                blockState.equals(Blocks.stained_glass_pane.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.LIGHT_BLUE)) ||
                blockState.equals(Blocks.stained_glass_pane.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.ORANGE)) ||
                blockState.equals(Blocks.stained_glass_pane.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.LIME))) {
            return 3000.0f;
        } else if (blockState.equals(Blocks.stained_glass.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.PURPLE)) ||
                blockState.equals(Blocks.stained_glass.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.LIGHT_BLUE)) ||
                blockState.equals(Blocks.stained_glass.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.ORANGE)) ||
                blockState.equals(Blocks.stained_glass.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.LIME))) {
            return 3200.0f;
        } else if (blockState.equals(Blocks.stained_glass_pane.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.WHITE)) ||
                blockState.equals(Blocks.stained_glass_pane.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.YELLOW))) {
            return 3800.0f;
        } else if (blockState.equals(Blocks.stained_glass.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.WHITE)) ||
                blockState.equals(Blocks.stained_glass.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.YELLOW))) {
            return 4000.0f;
        } else if (blockState.equals(Blocks.stained_glass_pane.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.MAGENTA))) {
            return 4800.0f;
        } else if (blockState.equals(Blocks.stained_glass.getDefaultState().withProperty(BlockColored.COLOR, EnumDyeColor.MAGENTA))) {
            return 5000.0f;
        }
        return 5000.0f;
    }
}
