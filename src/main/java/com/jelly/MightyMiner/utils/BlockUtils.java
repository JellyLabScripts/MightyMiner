package com.jelly.MightyMiner.utils;

import com.jelly.MightyMiner.baritone.structures.BlockType;
import com.jelly.MightyMiner.render.BlockRenderer;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;

import java.awt.*;
import java.util.*;
import java.util.List;

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

    public enum BlockSides {
        up,
        down,
        posX,
        posZ,
        negX,
        negZ,
    }

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
    public static Block getBlock(BlockPos blockPos) {
        return mc.theWorld.getBlockState(blockPos).getBlock();
    }
    public static List<BlockPos>  findBlock(int range, Block... requiredBlock) {
        return findBlock(range, null, requiredBlock);
    }
    public static List<BlockPos> findBlock(int range, ArrayList<BlockPos> forbiddenBlockPos, Block... requiredBlock) {

        List<Block> requiredBlocks = Arrays.asList(requiredBlock);
        List<BlockPos> foundBlocks = new ArrayList<>();

        for (int i = 0; i < range; i++) {
            for (int j = 0; j < range; j++) {
                for (int k = 0; k < range; k++) {
                    if (requiredBlocks.contains(getBlock(getRelativeBlockPos(0, 0, 0).add(i - range / 2, j - range / 2, k - range / 2)))) {
                        if(forbiddenBlockPos != null && forbiddenBlockPos.contains(getRelativeBlockPos(0, 0, 0).add(i - range / 2, j - range / 2, k - range / 2)))
                            continue;
                        foundBlocks.add(getRelativeBlockPos(0, 0, 0).add(i - range / 2, j - range / 2, k - range / 2));
                    }

                }
            }
        }
        foundBlocks.sort(Comparator.comparingDouble(b -> MathUtils.getDistanceBetweenTwoBlock(b, BlockUtils.getPlayerLoc().add(0, 1.62d, 0))));
        return foundBlocks;
    }


    public static Block getRelativeBlock(float rightOffset, float upOffset, float frontOffset) {
        return (getBlock(
                new BlockPos(
                        mc.thePlayer.posX + (getUnitX() * frontOffset) + (getUnitZ() * -1 * rightOffset),
                        mc.thePlayer.posY + upOffset,
                        mc.thePlayer.posZ + (getUnitZ() * frontOffset) + (getUnitX() * frontOffset)
                )));
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
    public static BlockPos getPlayerLoc() {
        return getRelativeBlockPos(0, 0);
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
        return canWalkOn(getBlock(groundBlock))
                && isPassable(getBlock(groundBlock.up()))
                && isPassable(getBlock(groundBlock.up(2)));
    }
    public static boolean onTheSameXZ (BlockPos b1, BlockPos b2) {
        return b1.getX() == b2.getX() && b1.getZ() == b2.getZ();

    }

    public static boolean canSeeBlock(BlockPos blockChecked) {

        Vec3 vec3 = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        Vec3 vec31 = MathUtils.getVectorForRotation(AngleUtils.getRequiredPitch(blockChecked), AngleUtils.getRequiredYaw(blockChecked));
        Vec3 vec32 = vec3.addVector(vec31.xCoord * 4.5f, vec31.yCoord * 4.5f, vec31.zCoord * 4.5f);
        MovingObjectPosition objectPosition = mc.theWorld.rayTraceBlocks(vec3, vec32, false, false, true);
        return objectPosition.getBlockPos().equals(blockChecked);
    }
    public static boolean canSeeBlock(BlockPos blockFrom, BlockPos blockChecked) {

        Vec3 vec3 = new Vec3(blockFrom.getX() + 0.5d, blockFrom.getY() + 0.5d, blockFrom.getZ() + 0.5d);
        Vec3 vec31 = MathUtils.getVectorForRotation(AngleUtils.getRequiredPitch(blockFrom, blockChecked), AngleUtils.getRequiredYaw(blockFrom, blockChecked));
        Vec3 vec32 = vec3.addVector(vec31.xCoord * 4.5f, vec31.yCoord * 4.5f, vec31.zCoord * 4.5f);
        MovingObjectPosition objectPosition = mc.theWorld.rayTraceBlocks(vec3, vec32, false, false, true);
        return objectPosition.getBlockPos().equals(blockChecked);
    }
    public static boolean canReachBlock(BlockPos blockChecked) {
        return MathUtils.getDistanceBetweenTwoPoints(
                mc.thePlayer.posX, mc.thePlayer.posY + 1.62f, mc.thePlayer.posZ, blockChecked.getX(), blockChecked.getY(), blockChecked.getZ()) < 4.5f;
    }


    public static ArrayList<BlockSides> getAdjBlocksNotCovered(BlockPos blockToSearch) {
        ArrayList<BlockSides> blockSidesNotCovered = new ArrayList<>();
        if(isPassable(blockToSearch.up()))
            blockSidesNotCovered.add(BlockSides.up);
        if(isPassable(blockToSearch.down()))
            blockSidesNotCovered.add(BlockSides.down);
        if(isPassable(blockToSearch.add(1, 0, 0)))
            blockSidesNotCovered.add(BlockSides.posX);
        if(isPassable(blockToSearch.add(-1, 0, 0)))
            blockSidesNotCovered.add(BlockSides.negX);
        if(isPassable(blockToSearch.add(0, 0, 1)))
            blockSidesNotCovered.add(BlockSides.posZ);
        if(isPassable(blockToSearch.add(0, 0, -1)))
            blockSidesNotCovered.add(BlockSides.negZ);

        return blockSidesNotCovered;
    }


}
