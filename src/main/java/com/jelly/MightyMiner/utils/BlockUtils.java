package com.jelly.MightyMiner.utils;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.util.Arrays;

public class BlockUtils {

    private static Minecraft mc = Minecraft.getMinecraft();
    private static final Block[] walkables = {
            Blocks.air,
            Blocks.wall_sign,
            Blocks.reeds,
            Blocks.tallgrass,
            Blocks.yellow_flower,
            Blocks.deadbush,
            Blocks.red_flower,
            Blocks.stone_slab,
            Blocks.wooden_slab,
            Blocks.rail,
            Blocks.activator_rail,
            Blocks.detector_rail,
            Blocks.golden_rail,
            Blocks.carpet
    };
    private static final Block[] cannotWalkOn = { // cannot be treated as full block
            Blocks.air,
            Blocks.water,
            Blocks.flowing_water,
            Blocks.lava,
            Blocks.flowing_lava,
            Blocks.rail,
            Blocks.activator_rail,
            Blocks.detector_rail,
            Blocks.golden_rail,
            Blocks.carpet,
            Blocks.slime_block
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

    public static Block getRelativeBlock(float rightOffset, float upOffset, float frontOffset) {
        return (mc.theWorld.getBlockState(
                new BlockPos(
                        mc.thePlayer.posX + (getUnitX() * frontOffset) + (getUnitZ() * -1 * rightOffset),
                        mc.thePlayer.posY + upOffset,
                        mc.thePlayer.posZ + (getUnitZ() * frontOffset) + (getUnitX() * frontOffset)
                )).getBlock());
    }
    public static BlockPos getRelativeBlockPos(float rightOffset, float upOffset, float frontOffset) {
        return new BlockPos(
                mc.thePlayer.posX + (getUnitX() * frontOffset) + (getUnitZ() * -1 * rightOffset),
                mc.thePlayer.posY + upOffset,
                mc.thePlayer.posZ + (getUnitZ() * frontOffset) + (getUnitX() * rightOffset)
        );
    }
    public static BlockPos getRelativeBlockPos(float rightOffset, float frontOffset) {
        return getRelativeBlockPos(rightOffset, 0, frontOffset);
    }


    public static boolean isAStraightLine(BlockPos b1, BlockPos b2, BlockPos b3){
        if((b1.getX() - b2.getX()) == 0 || (b2.getX() - b3.getX()) == 0 || (b1.getX() - b3.getX()) == 0)
            return (b1.getX() - b2.getX()) == 0 && (b2.getX() - b3.getX()) == 0 && (b1.getX() - b3.getX()) == 0 && b1.getY() == b2.getY() && b2.getY()== b3.getY();
        return ((b1.getZ() - b2.getZ())/(b1.getX() - b2.getX()) == (b2.getZ() - b3.getZ())/(b2.getX() - b3.getX()) &&
                (b1.getZ() - b2.getZ())/(b1.getX() - b2.getX()) == (b1.getZ() - b3.getZ())/(b1.getX() - b3.getX())) && b1.getY() == b2.getY() && b2.getY()== b3.getY();

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

    public static boolean isPassable(Block block) {
        return Arrays.asList(walkables).contains(block);
    }
    public static boolean isPassable(BlockPos block) {return isPassable(mc.theWorld.getBlockState(block).getBlock());}
    public static boolean canWalkOn(Block groundBlock) {
        return !Arrays.asList(cannotWalkOn).contains(groundBlock);
    }
    public static boolean canWalkOn(BlockPos groundBlock) {return canWalkOn(mc.theWorld.getBlockState(groundBlock).getBlock());}
    public static boolean fitsPlayer(BlockPos groundBlock) {
        return canWalkOn(mc.theWorld.getBlockState(groundBlock).getBlock())
                && isPassable(mc.theWorld.getBlockState(groundBlock.up()).getBlock())
                && isPassable(mc.theWorld.getBlockState(groundBlock.up(2)).getBlock());
    }
}
