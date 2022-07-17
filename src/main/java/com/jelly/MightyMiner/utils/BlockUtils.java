package com.jelly.MightyMiner.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3i;

import java.util.Arrays;

public class BlockUtils {

    private static Minecraft mc = Minecraft.getMinecraft();
    private static final Block[] walkables = {
            Blocks.air,
            Blocks.water,
            Blocks.flowing_water,
            Blocks.dark_oak_fence_gate,
            Blocks.acacia_fence_gate,
            Blocks.birch_fence_gate,
            Blocks.oak_fence_gate,
            Blocks.jungle_fence_gate,
            Blocks.spruce_fence_gate,
            Blocks.wall_sign,
            Blocks.reeds,
            Blocks.tallgrass,
            Blocks.yellow_flower,
            Blocks.deadbush,
            Blocks.red_flower,
            Blocks.stone_slab,
            Blocks.wooden_slab,
            Blocks.sandstone_stairs,
            Blocks.acacia_stairs,
            Blocks.spruce_stairs,
            Blocks.stone_stairs,
            Blocks.stone_brick_stairs,
            Blocks.birch_stairs,
            Blocks.brick_stairs,
            Blocks.dark_oak_stairs,
            Blocks.jungle_stairs,
            Blocks.nether_brick_stairs,
            Blocks.oak_stairs,
            Blocks.quartz_stairs,
            Blocks.red_sandstone_stairs,
            Blocks.rail,
            Blocks.activator_rail,
            Blocks.detector_rail,
            Blocks.golden_rail
    };

    public static int getUnitX() {
        double modYaw = (mc.thePlayer.rotationYaw % 360 + 360) % 360;
        if (modYaw < 45 || modYaw > 315) {
            return 0;
        } else if (modYaw < 135) {
            return -1;
        } else if (modYaw < 225) {
            return 0;
        } else {
            return 1;
        }
    }

    public static int getUnitZ() {
        double modYaw = (mc.thePlayer.rotationYaw % 360 + 360) % 360;
        if (modYaw < 45 || modYaw > 315) {
            return 1;
        } else if (modYaw < 135) {
            return 0;
        } else if (modYaw < 225) {
            return -1;
        } else {
            return 0;
        }
    }

    public static Block getRelativeBlock(float x, float y, float z) {
        return (mc.theWorld.getBlockState(
                new BlockPos(
                        mc.thePlayer.posX + (getUnitX() * z) + (getUnitZ() * -1 * x),
                        mc.thePlayer.posY + y,
                        mc.thePlayer.posZ + (getUnitZ() * z) + (getUnitX() * x)
                )).getBlock());
    }
    public static BlockPos getRelativeBlockPos(float x, float y, float z) {
        return new BlockPos(
                mc.thePlayer.posX + (getUnitX() * z) + (getUnitZ() * -1 * x),
                mc.thePlayer.posY + y,
                mc.thePlayer.posZ + (getUnitZ() * z) + (getUnitX() * x)
        );
    }



    public static Block getLeftBlock(){
        return getRelativeBlock(-1, 0, 0);
    }
    public static Block getRightBlock(){
        return getRelativeBlock(1, 0, 0);
    }
    public static Block getBackBlock(){
        return getRelativeBlock(0, 0, -1);
    }
    public static Block getFrontBlock(){
        return getRelativeBlock(0, 0, 1);
    }

    public static boolean isWalkable(Block block) {
        return Arrays.asList(walkables).contains(block);
    }
    public static boolean canWalkOn(BlockPos groundBlock) {
        return !isWalkable(mc.theWorld.getBlockState(groundBlock).getBlock())
                && isWalkable(mc.theWorld.getBlockState(groundBlock.up()).getBlock())
                  && isWalkable(mc.theWorld.getBlockState(groundBlock.up(2)).getBlock());
    }
}
