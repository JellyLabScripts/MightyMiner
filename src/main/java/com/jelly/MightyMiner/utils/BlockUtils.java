package com.jelly.MightyMiner.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jelly.MightyMiner.baritone.automine.AutoMineBaritone;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BlockUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();
    public static final Block[] walkables = {
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
    public static final Block[] cannotWalkOn = { // cannot be treated as full block
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
        NONE
    }

    // ;p
    private boolean isTitanium(BlockPos pos) {
        IBlockState state = mc.theWorld.getBlockState(pos);
        return (state.getBlock() == Blocks.stone && ((BlockStone.EnumType)state.getValue((IProperty)BlockStone.VARIANT)).equals(BlockStone.EnumType.DIORITE_SMOOTH));
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

    @SuppressWarnings("UnstableApiUsage")
    private static final LoadingCache<BlockPos, Block> blockCache = CacheBuilder.newBuilder().expireAfterWrite(3L, TimeUnit.SECONDS).build(new CacheLoader<BlockPos, Block>() {
        public Block load(@NotNull BlockPos pos) {
            return getBlock(pos);
        }
    });

    public static Block getBlock(BlockPos b){
        return mc.theWorld.getBlockState(b).getBlock();
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Block getBlockCached(BlockPos blockPos) {
        return blockCache.getUnchecked(blockPos);
    }

    public static List<BlockPos> findBlock(int i, ArrayList<BlockPos> blackListedPos, int minY, int maxY, AutoMineBaritone.BlockData<EnumDyeColor> block) {
        return findBlock(i, blackListedPos, minY, maxY, new ArrayList<AutoMineBaritone.BlockData<EnumDyeColor>>() {{
            add(block);
        }});
    }

    public static List<BlockPos> findBlock(int searchDiameter, ArrayList<AutoMineBaritone.BlockData<EnumDyeColor>> requiredBlock) {
        return findBlock(searchDiameter, null, 0, 256, requiredBlock);
    }
    public static List<BlockPos> findBlock(int boxDiameter, ArrayList<BlockPos> forbiddenBlockPos, int minY, int maxY, ArrayList<AutoMineBaritone.BlockData<EnumDyeColor>> requiredBlock) {
        return findBlock(new Box(-boxDiameter / 2, boxDiameter / 2, -boxDiameter / 2, boxDiameter / 2, -boxDiameter / 2, boxDiameter / 2)
                ,forbiddenBlockPos, minY, maxY, requiredBlock);
    }

    public static List<BlockPos> findBlock(Box searchBox, ArrayList<BlockPos> forbiddenBlockPos, int minY, int maxY, ArrayList<AutoMineBaritone.BlockData<EnumDyeColor>> requiredBlock) {

        List<BlockPos> foundBlocks = new ArrayList<>();

        BlockPos currentBlock;

        for (int i = 0; i <= Math.abs(searchBox.dx_bound2 - searchBox.dx_bound1); i++) {
            for (int j = 0; j <= Math.abs(searchBox.dy_bound2 - searchBox.dy_bound1); j++) {
                for (int k = 0; k <= Math.abs(searchBox.dz_bound2 - searchBox.dz_bound1); k++) {

                    //rectangular scan
                    currentBlock = (getPlayerLoc().add(i + Math.min(searchBox.dx_bound2, searchBox.dx_bound1),  j + Math.min(searchBox.dy_bound2, searchBox.dy_bound1),  k + Math.min(searchBox.dz_bound2, searchBox.dz_bound1)));
                    BlockPos finalCurrentBlock = currentBlock;
                    if(requiredBlock.stream().anyMatch(blockData -> {
                        Block block = mc.theWorld.getBlockState(finalCurrentBlock).getBlock();
                        if (!blockData.block.equals(block)) return false;
                        if (blockData.requiredBlockStateValue == null) return true;
                        return block.getMetaFromState(mc.theWorld.getBlockState(finalCurrentBlock)) == blockData.requiredBlockStateValue.getMetadata();
                    })) {
                        if (forbiddenBlockPos != null && !forbiddenBlockPos.isEmpty() && forbiddenBlockPos.contains(currentBlock))
                            continue;
                        if (currentBlock.getY() > maxY || currentBlock.getY() < minY)
                            continue;
                        foundBlocks.add(currentBlock);
                    }
                }
            }
        }
        foundBlocks.sort(Comparator.comparingDouble(b -> MathUtils.getDistanceBetweenTwoBlock(b, BlockUtils.getPlayerLoc().add(0, 1.62d, 0))));
        return foundBlocks;
    }



    public static Block getRelativeBlock(float rightOffset, float upOffset, float frontOffset) {
        return getBlock(getRelativeBlockPos(rightOffset, upOffset, frontOffset));
    }

    public static BlockPos getRelativeBlockPos(float rightOffset, float upOffset, float frontOffset) {
        int unitX = getUnitX();
        int unitZ = getUnitZ();
        return new BlockPos(
                mc.thePlayer.posX + (unitX * frontOffset) + (unitZ * -1 * rightOffset),
                mc.thePlayer.posY + upOffset,
                mc.thePlayer.posZ + (unitZ * frontOffset) + (unitX * rightOffset)
        );
    }


    public static BlockPos getRelativeBlockPos(float rightOffset, float frontOffset) {
        return getRelativeBlockPos(rightOffset, 0, frontOffset);
    }

    public static BlockPos getPlayerLoc() {
        return getRelativeBlockPos(0, 0);
    }


    public static boolean isAStraightLine(BlockPos b1, BlockPos b2, BlockPos b3) {
        if ((b1.getX() - b2.getX()) == 0 || (b2.getX() - b3.getX()) == 0 || (b1.getX() - b3.getX()) == 0)
            return (b1.getX() - b2.getX()) == 0 && (b2.getX() - b3.getX()) == 0 && (b1.getX() - b3.getX()) == 0 && b1.getY() == b2.getY() && b2.getY() == b3.getY();
        return ((b1.getZ() - b2.getZ()) / (b1.getX() - b2.getX()) == (b2.getZ() - b3.getZ()) / (b2.getX() - b3.getX()) &&
                (b1.getZ() - b2.getZ()) / (b1.getX() - b2.getX()) == (b1.getZ() - b3.getZ()) / (b1.getX() - b3.getX())) && b1.getY() == b2.getY() && b2.getY() == b3.getY();

    }

    public static Block getLeftBlock() {
        return getRelativeBlock(-1, 0, 0);
    }

    public static Block getRightBlock() {
        return getRelativeBlock(1, 0, 0);
    }

    public static Block getBackBlock() {
        return getRelativeBlock(0, 0, -1);
    }

    public static Block getFrontBlock() {
        return getRelativeBlock(0, 0, 1);
    }

    public static boolean isPassable(Block block) {
        return Arrays.asList(walkables).contains(block);
    }

    public static boolean isPassable(BlockPos block) {
        return isPassable(getBlock(block));
    }

    public static boolean canWalkOn(Block groundBlock) {
        return !Arrays.asList(cannotWalkOn).contains(groundBlock);
    }

    public static boolean canWalkOn(BlockPos groundBlock) {
        return canWalkOn(getBlock(groundBlock));
    }

    public static boolean fitsPlayer(BlockPos groundBlock) {
        return canWalkOn(getBlock(groundBlock))
                && isPassable(getBlock(groundBlock.up()))
                && isPassable(getBlock(groundBlock.up(2)));
    }

    public static boolean onTheSameXZ(BlockPos b1, BlockPos b2) {
        return b1.getX() == b2.getX() && b1.getZ() == b2.getZ();

    }

    public static boolean onTheSameAxis(BlockPos b1, BlockPos b2) {
        return b1.getX() == b2.getX() || b1.getZ() == b2.getZ();
    }

    public static boolean isAdjacentXZ(BlockPos b1, BlockPos b2) {
        return (b1.getX() == b2.getX() && Math.abs(b1.getZ() - b2.getZ()) == 1) ||
                (b1.getZ() == b2.getZ() && Math.abs(b1.getX() - b2.getX()) == 1);
    }

    public static boolean canSeeBlock(BlockPos blockChecked) {

        Vec3 vec3 = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
        //get required yaw have problems
        Vec3 vec31 = MathUtils.getVectorForRotation(AngleUtils.getRequiredPitch(blockChecked), AngleUtils.getRequiredYaw(blockChecked));
        Vec3 vec32 = vec3.addVector(vec31.xCoord * 4.5f, vec31.yCoord * 4.5f, vec31.zCoord * 4.5f);
        MovingObjectPosition objectPosition = mc.theWorld.rayTraceBlocks(vec3, vec32, false, false, true);
        return objectPosition.getBlockPos().equals(blockChecked);
    }



    public static boolean canReachBlock(BlockPos blockChecked) {
        return MathUtils.getDistanceBetweenTwoPoints(
                mc.thePlayer.posX, mc.thePlayer.posY + 1.62f, mc.thePlayer.posZ, blockChecked.getX() + 0.5f, blockChecked.getY() + 0.5f, blockChecked.getZ() + 0.5f) < 4.5f;
    }


    public static ArrayList<BlockSides> getAdjBlocksNotCovered(BlockPos blockToSearch) {
        ArrayList<BlockSides> blockSidesNotCovered = new ArrayList<>();

        if (isPassable(blockToSearch.up()))
            blockSidesNotCovered.add(BlockSides.up);
        if (isPassable(blockToSearch.down()))
            blockSidesNotCovered.add(BlockSides.down);
        if (isPassable(blockToSearch.add(1, 0, 0)))
            blockSidesNotCovered.add(BlockSides.posX);
        if (isPassable(blockToSearch.add(-1, 0, 0)))
            blockSidesNotCovered.add(BlockSides.negX);
        if (isPassable(blockToSearch.add(0, 0, 1)))
            blockSidesNotCovered.add(BlockSides.posZ);
        if (isPassable(blockToSearch.add(0, 0, -1)))
            blockSidesNotCovered.add(BlockSides.negZ);

        return blockSidesNotCovered;
    }


    public static boolean hasBlockInterfere(BlockPos b1, BlockPos b2) {
        for (BlockPos pos : getRasterizedBlocks(b1, b2)) {
            if (!BlockUtils.isPassable(pos))
                return true;
        }
        return false;
    }

    public static ArrayList<BlockPos> getRasterizedBlocks(BlockPos b1, BlockPos b2){
        ArrayList<BlockPos> lineBlock = new ArrayList<>();
        int x0 = b1.getX();
        int x1 = b2.getX();
        int z0 = b1.getZ();
        int z1 = b2.getZ();

        int dx = Math.abs(x1 - x0);
        int dz = Math.abs(z1 - z0);
        int sx = (x0 < x1) ? 1 : -1;
        int sy = (z0 < z1) ? 1 : -1;
        int err = dx - dz;

        while (true) {
            lineBlock.add(new BlockPos(x0, b1.getY(), z0));

            if ((x0 == x1) && (z0 == z1)) break;
            int e2 = 2 * err;

            if (e2 > -dz) {
                err -= dz;
                x0 += sx;
            } else if (e2 < dx) {
                err += dx;
                z0 += sy;
            }
        }
        return lineBlock;

    }

    public static boolean inCenterOfBlock(){
        return Math.abs(mc.thePlayer.posX % 1) == 0.5 && Math.abs(mc.thePlayer.posZ % 1) == 0.5;
    }

}
