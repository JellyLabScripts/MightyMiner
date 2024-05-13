package com.jelly.MightyMinerV2.Util;

import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;


public class BlockUtil {

    public static Block getBlockBeingMined(World world, EntityPlayer player) {
        Vec3 vec3 = player.getPositionVector();
        Vec3 vec31 = player.getLook(1.0F);
        Vec3 vec32 = vec3.addVector(vec31.xCoord * 5.0D, vec31.yCoord * 5.0D, vec31.zCoord * 5.0D);
        MovingObjectPosition mop = world.rayTraceBlocks(vec3, vec32);
        if (mop != null && mop.typeOfHit == MovingObjectType.BLOCK) {
            IBlockState blockState = world.getBlockState(mop.getBlockPos());
            return blockState.getBlock();
        }
        return null;
    }

    public static float getBlockStrength(IBlockState blockState) {
        if (blockState == null) {
            return 30.0f;
        }

        Block block = blockState.getBlock();

        if (block == Blocks.diamond_block) {
            return 50.0f;
        } else if (block == Blocks.gold_block) {
            return 600.0f;
        } else if (block == Blocks.sponge) {
            return 500.0f;
        } else if (block == Blocks.stone) {
            if (blockState.getValue(BlockStone.VARIANT) == BlockStone.EnumType.DIORITE_SMOOTH) {
                return 2000.0f;
            }
        } else if (block == Blocks.wool) {
            switch (blockState.getValue(BlockColored.COLOR)) {
                case GRAY:
                    return 500.0f;
                case LIGHT_BLUE:
                    return 1500.0f;
                default:
                    break;
            }
        } else if (block == Blocks.stained_hardened_clay) {
            if (blockState.getValue(BlockColored.COLOR) == EnumDyeColor.CYAN) {
                return 500.0f;
            }
        } else if (block == Blocks.prismarine) {
            switch (blockState.getValue(BlockPrismarine.VARIANT)) {
                case ROUGH:
                case DARK:
                case BRICKS:
                    return 800.0f;
                default:
                    break;
            }
        } else if (block == Blocks.stained_glass) {
            switch (blockState.getValue(BlockColored.COLOR)) {
                case RED:
                    return 2500.0f;
                case PURPLE:
                case LIGHT_BLUE:
                case ORANGE:
                case LIME:
                    return 3200.0f;
                case WHITE:
                case YELLOW:
                    return 4000.0f;
                case MAGENTA:
                    return 5000.0f;
                default:
                    break;
            }
        } else if (block == Blocks.stained_glass_pane) {
            switch (blockState.getValue(BlockColored.COLOR)) {
                case RED:
                    return 2300.0f;
                case PURPLE:
                case LIGHT_BLUE:
                case ORANGE:
                case LIME:
                    return 3000.0f;
                case WHITE:
                case YELLOW:
                    return 3800.0f;
                case MAGENTA:
                    return 4800.0f;
                default:
                    break;
            }
        } else {
            return 30.0f;
        }

        return 5000.0f;
    }

    public static float getMiningTime(float getBlockStrength, int miningSpeed) {
        return (getBlockStrength * 30.0f) / miningSpeed;
    }
}
