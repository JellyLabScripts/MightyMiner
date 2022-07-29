package com.jelly.MightyMiner.utils;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AngleUtils {

    static final List<Block> lookAtCenterBlocks = new ArrayList<Block>(){
        {
            add(Blocks.stained_glass_pane);
        }
    };
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static float get360RotationYaw(float yaw) {
        return (yaw % 360 + 360) % 360;
    }

    public static float get360RotationYaw() {
        return get360RotationYaw(mc.thePlayer.rotationYaw);
    }

    public static float clockwiseDifference(float initialYaw360, float targetYaw360) {
        return get360RotationYaw(targetYaw360 - initialYaw360);
    }

    public static float antiClockwiseDifference(float initialYaw360, float targetYaw360) {
        return get360RotationYaw(initialYaw360 - targetYaw360);
    }

    public static float smallestAngleDifference(float initialYaw360, float targetYaw360) {
        return Math.min(clockwiseDifference(initialYaw360, targetYaw360), antiClockwiseDifference(initialYaw360, targetYaw360));
    }

    public static float getClosest() {
        if (get360RotationYaw() < 45 || get360RotationYaw() > 315) {
            return 0f;
        } else if (get360RotationYaw() < 135) {
            return 90f;
        } else if (get360RotationYaw() < 225) {
            return 180f;
        } else {
            return 270f;
        }
    }
    public static float getClosest(float yaw) {
        if (yaw < 45 || yaw > 315) {
            return 0f;
        } else if (yaw < 135) {
            return 90f;
        } else if (yaw < 225) {
            return 180f;
        } else {
            return 270f;
        }
    }
    public static boolean shouldLookAtCenter(BlockPos blockPos){
        return shouldLookAtCenter(BlockUtils.getBlock(blockPos));
    }
    public static boolean shouldLookAtCenter(Block block){
        return lookAtCenterBlocks.contains(block);
    }
    public static float getRequiredYaw(BlockPos blockFrom, BlockPos blockLookingAt) {

        double deltaX = blockLookingAt.getX() - blockFrom.getX();
        double deltaZ = blockLookingAt.getZ() - blockFrom.getZ();

        ArrayList<BlockUtils.BlockSides> blockSidesNotCovered = BlockUtils.getAdjBlocksNotCovered(blockLookingAt);
        BlockUtils.BlockSides blockSideToMine;
        if(blockSidesNotCovered.size() > 0 && !lookAtCenterBlocks.contains(BlockUtils.getBlock(blockLookingAt))){
            double lowestCost = 9999;
            blockSideToMine = blockSidesNotCovered.get(0);

            for(BlockUtils.BlockSides blockSide : blockSidesNotCovered){
                double tempCost = 0;
                switch (blockSide){
                    case posX:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                blockFrom.getX(), blockFrom.getX(), blockFrom.getZ(), blockLookingAt.getX() + 0.5d, blockLookingAt.getY(), blockLookingAt.getZ());
                        break;
                    case negX:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                blockFrom.getX(), blockFrom.getY(), blockFrom.getZ(), blockLookingAt.getX() - 0.5d, blockLookingAt.getY(), blockLookingAt.getZ());
                        break;
                    case posZ:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                blockFrom.getX(), blockFrom.getY(),blockFrom.getZ(), blockLookingAt.getX(), blockLookingAt.getY(), blockLookingAt.getZ() + 0.5d);
                        break;
                    case negZ:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                blockFrom.getX(), blockFrom.getY(), blockFrom.getZ(), blockLookingAt.getX(), blockLookingAt.getY(), blockLookingAt.getZ() - 0.5d);
                        break;
                }
                if(tempCost < lowestCost) {
                    lowestCost = tempCost;
                    blockSideToMine = blockSide;
                }
            }
            switch (blockSideToMine){
                case posX:
                    deltaX += 0.5d;
                    break;
                case negX:
                    deltaX -= 0.5d;
                    break;
                case posZ:
                    deltaZ += 0.5d;
                    break;
                case negZ:
                    deltaZ -= 0.5d;
                    break;
            }

        }
        return  (float) (Math.atan(-deltaX / deltaZ) * 180 / Math.PI) + ((deltaX > 0 && deltaZ < 0) ? -180 : 0) +
                ((deltaX < 0 && deltaZ < 0) ? 180 : 0);
    }
    public static float getRequiredPitch(BlockPos blockFrom, BlockPos blockLookingAt) {
        double deltaX = blockLookingAt.getX() - blockFrom.getX();
        double deltaZ = blockLookingAt.getZ() - blockFrom.getZ();
        double deltaY = blockLookingAt.getY() - blockFrom.getY();

        ArrayList<BlockUtils.BlockSides> blockSidesNotCovered = BlockUtils.getAdjBlocksNotCovered(blockLookingAt);
        BlockUtils.BlockSides blockSideToMine;
        if(blockSidesNotCovered.size() > 0 && !lookAtCenterBlocks.contains(BlockUtils.getBlock(blockLookingAt))){
            double lowestCost = 9999;
            blockSideToMine = blockSidesNotCovered.get(0);

            for(BlockUtils.BlockSides blockSide : blockSidesNotCovered){
                double tempCost = 0;
                switch (blockSide){
                    case posX:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                blockFrom.getX(), blockFrom.getX(), blockFrom.getZ(), blockLookingAt.getX() + 0.5d, blockLookingAt.getY(), blockLookingAt.getZ());
                        break;
                    case negX:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                blockFrom.getX(), blockFrom.getX(), blockFrom.getZ(), blockLookingAt.getX() - 0.5d, blockLookingAt.getY(), blockLookingAt.getZ());
                        break;
                    case posZ:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                blockFrom.getX(), blockFrom.getX(), blockFrom.getZ(), blockLookingAt.getX(), blockLookingAt.getY(), blockLookingAt.getZ() + 0.5d);
                        break;
                    case negZ:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                blockFrom.getX(), blockFrom.getX(), blockFrom.getZ(), blockLookingAt.getX(), blockLookingAt.getY(), blockLookingAt.getZ() - 0.5d);
                        break;
                    case up:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                blockFrom.getX(), blockFrom.getX(), blockFrom.getZ(), blockLookingAt.getX(), blockLookingAt.getY() + 0.5d, blockLookingAt.getZ());
                        break;
                    case down:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                blockFrom.getX(), blockFrom.getX(), blockFrom.getZ(), blockLookingAt.getX(), blockLookingAt.getY() - 0.5d, blockLookingAt.getZ());
                        break;
                }
                if(tempCost < lowestCost) {
                    lowestCost = tempCost;
                    blockSideToMine = blockSide;
                }
            }
            switch (blockSideToMine){
                case posX:
                    deltaX += 0.5d;
                    break;
                case negX:
                    deltaX -= 0.5d;
                    break;
                case posZ:
                    deltaZ += 0.5d;
                    break;
                case negZ:
                    deltaZ -= 0.5d;
                    break;
                case up:
                    deltaY += 0.5d;
                    break;
                case down:
                    deltaY -= 0.5d;
                    break;
            }

        }

        double deltaDis = MathUtils.getDistanceBetweenTwoPoints(deltaX, deltaZ);
        double pitch = -(Math.atan(deltaY / deltaDis) * 180 / Math.PI);

        if( (float) pitch > 90 ||  (float) pitch < -90){
            System.out.println(pitch + " " + deltaX + " " + deltaZ);
            return 0;
        }
        return  (float) pitch;
    }

    public static float getRequiredYaw(BlockPos blockLookingAt) {

        double deltaX = blockLookingAt.getX() - mc.thePlayer.posX + 0.5d;
        double deltaZ = blockLookingAt.getZ() - mc.thePlayer.posZ + 0.5d;

        ArrayList<BlockUtils.BlockSides> blockSidesNotCovered = BlockUtils.getAdjBlocksNotCovered(blockLookingAt);
        BlockUtils.BlockSides blockSideToMine;
        if(blockSidesNotCovered.size() > 0 &&  !lookAtCenterBlocks.contains(BlockUtils.getBlock(blockLookingAt))){
            double lowestCost = 9999;
            blockSideToMine = blockSidesNotCovered.get(0);

            for(BlockUtils.BlockSides blockSide : blockSidesNotCovered){
                double tempCost = 0;
                switch (blockSide){
                    case posX:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, blockLookingAt.getX() + 0.5d, blockLookingAt.getY(), blockLookingAt.getZ());
                        break;
                    case negX:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, blockLookingAt.getX() - 0.5d, blockLookingAt.getY(), blockLookingAt.getZ());
                        break;
                    case posZ:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, blockLookingAt.getX(), blockLookingAt.getY(), blockLookingAt.getZ() + 0.5d);
                        break;
                    case negZ:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, blockLookingAt.getX(), blockLookingAt.getY(), blockLookingAt.getZ() - 0.5d);
                        break;
                }
                if(tempCost < lowestCost) {
                    lowestCost = tempCost;
                    blockSideToMine = blockSide;
                }
            }
            switch (blockSideToMine){
                case posX:
                    deltaX += 0.5d;
                    break;
                case negX:
                    deltaX -= 0.5d;
                    break;
                case posZ:
                    deltaZ += 0.5d;
                    break;
                case negZ:
                    deltaZ -= 0.5d;
                    break;
            }

        }
        return  (float) (Math.atan(-deltaX / deltaZ) * 180 / Math.PI) + ((deltaX > 0 && deltaZ < 0) ? -180 : 0) +
                ((deltaX < 0 && deltaZ < 0) ? 180 : 0);
    }

    public static float getRequiredPitch(BlockPos blockLookingAt) {
        double deltaX = blockLookingAt.getX() - mc.thePlayer.posX + 0.5d;
        double deltaZ = blockLookingAt.getZ() - mc.thePlayer.posZ + 0.5d;
        double deltaY = (blockLookingAt.getY() + 0.5d) - (mc.thePlayer.posY + 1.62d);

        ArrayList<BlockUtils.BlockSides> blockSidesNotCovered = BlockUtils.getAdjBlocksNotCovered(blockLookingAt);
        BlockUtils.BlockSides blockSideToMine;
        if(blockSidesNotCovered.size() > 0 && !lookAtCenterBlocks.contains(BlockUtils.getBlock(blockLookingAt))){
            double lowestCost = 9999;
            blockSideToMine = blockSidesNotCovered.get(0);

            for(BlockUtils.BlockSides blockSide : blockSidesNotCovered){
                double tempCost = 0;
                switch (blockSide){
                    case posX:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, blockLookingAt.getX() + 0.5d, blockLookingAt.getY(), blockLookingAt.getZ());
                        break;
                    case negX:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, blockLookingAt.getX() - 0.5d, blockLookingAt.getY(), blockLookingAt.getZ());
                        break;
                    case posZ:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, blockLookingAt.getX(), blockLookingAt.getY(), blockLookingAt.getZ() + 0.5d);
                        break;
                    case negZ:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, blockLookingAt.getX(), blockLookingAt.getY(), blockLookingAt.getZ() - 0.5d);
                        break;
                    case up:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, blockLookingAt.getX(), blockLookingAt.getY() + 0.5d, blockLookingAt.getZ());
                        break;
                    case down:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, blockLookingAt.getX(), blockLookingAt.getY() - 0.5d, blockLookingAt.getZ());
                        break;
                }
                if(tempCost < lowestCost) {
                    lowestCost = tempCost;
                    blockSideToMine = blockSide;
                }
            }
            switch (blockSideToMine){
                case posX:
                    deltaX += 0.5d;
                    break;
                case negX:
                    deltaX -= 0.5d;
                    break;
                case posZ:
                    deltaZ += 0.5d;
                    break;
                case negZ:
                    deltaZ -= 0.5d;
                    break;
                case up:
                    deltaY += 0.5d;
                    break;
                case down:
                    deltaY -= 0.5d;
                    break;
            }

        }

        double deltaDis = MathUtils.getDistanceBetweenTwoPoints(deltaX, deltaZ);
        double pitch = -(Math.atan(deltaY / deltaDis) * 180 / Math.PI);

        if( (float) pitch > 90 ||  (float) pitch < -90){
            System.out.println(pitch + " " + deltaX + " " + deltaZ);
            return 0;
        }
        return  (float) pitch;
    }

    public static int getRelativeYawFromBlockPos(BlockPos facingBlockPos) {
        if (BlockUtils.onTheSameXZ(BlockUtils.getRelativeBlockPos(1, 0), facingBlockPos)) {
            return 90;
        } else if (BlockUtils.onTheSameXZ(BlockUtils.getRelativeBlockPos(-1, 0), facingBlockPos)) {
            return -90;
        } else if (BlockUtils.onTheSameXZ(BlockUtils.getRelativeBlockPos(0, 1), facingBlockPos)) {
            return 0;
        } else if (BlockUtils.onTheSameXZ(BlockUtils.getRelativeBlockPos(0, -1), facingBlockPos)) {
            return 180;
        }
        return -1;

    }

}
