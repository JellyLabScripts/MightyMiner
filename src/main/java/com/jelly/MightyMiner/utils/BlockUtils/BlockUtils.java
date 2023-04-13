package com.jelly.MightyMiner.utils.BlockUtils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jelly.MightyMiner.MightyMiner;
import com.jelly.MightyMiner.utils.PlayerUtils;
import com.jelly.MightyMiner.utils.Utils.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Tuple;
import net.minecraft.util.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.jelly.MightyMiner.utils.PlayerUtils.AnyBlockAroundVec3;
import static com.jelly.MightyMiner.utils.AngleUtils.*;


public class BlockUtils {

    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final Random rnd = new Random();

    public static final List<Block> walkables = Arrays.asList(
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
    );

    public static final List<Block> cannotWalkOn = Arrays.asList( // cannot be treated as full block
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
    );

    public enum BlockSides {
        up,
        down,
        posX,
        posZ,
        negX,
        negZ,
        NONE
    }

    private static final Comparator<Vec3> sortVec3ByRotation = (a, b) -> {
        if(getRotationCost(getRotation(a).getFirst(), getRotation(a).getSecond()) == getRotationCost(getRotation(b).getFirst(), getRotation(b).getSecond())) return 0;
        return (getRotationCost(getRotation(a).getFirst(), getRotation(a).getSecond()) > getRotationCost(getRotation(b).getFirst(), getRotation(b).getSecond())) ? 1 : -1;
    };

    // ;p
    private boolean isTitanium(BlockPos pos) {
        IBlockState state = mc.theWorld.getBlockState(pos);
        return (state.getBlock() == Blocks.stone && (state.getValue(BlockStone.VARIANT)).equals(BlockStone.EnumType.DIORITE_SMOOTH));
    }

    public static int getUnitX() {
        return getUnitX((mc.thePlayer.rotationYaw % 360 + 360) % 360);
    }

    public static int getUnitZ() {
        return getUnitZ((mc.thePlayer.rotationYaw % 360 + 360) % 360);
    }

    public static int getUnitX(double modYaw) {
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

    public static int getUnitZ(double modYaw) {

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

    public static IBlockState getBlockState(BlockPos pos) {
        return mc.theWorld.getBlockState(pos);
    }

    public static WorldClient getWorld() {
        return mc.theWorld;
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

    public static List<BlockPos> findBlock(int searchDiameter, Block... requiredBlock) {
        return findBlock(searchDiameter, null, 0, 256, requiredBlock);
    }

    public static ArrayList<BlockData<EnumDyeColor>> addData(ArrayList<Block> blocks){
        ArrayList<BlockData<EnumDyeColor>> requiredBlocksList = new ArrayList<>();
        for(Block block : blocks){
            requiredBlocksList.add(new BlockData<>(block, null));
        }
        return requiredBlocksList;
    }

    public static List<BlockPos> findBlock(int boxDiameter, ArrayList<BlockPos> forbiddenBlockPos, int minY, int maxY, ArrayList<BlockData<EnumDyeColor>> requiredBlocks) {
        return findBlock(new Box(-boxDiameter / 2, boxDiameter / 2, -boxDiameter / 2, boxDiameter / 2, -boxDiameter / 2, boxDiameter / 2)
                ,forbiddenBlockPos, minY, maxY, requiredBlocks);
    }
    @SafeVarargs
    public static List<BlockPos> findBlock(int boxDiameter, ArrayList<BlockPos> forbiddenBlockPos, int minY, int maxY, BlockData<EnumDyeColor>... requiredBlocks) {
        return findBlock(new Box(-boxDiameter / 2, boxDiameter / 2, -boxDiameter / 2, boxDiameter / 2, -boxDiameter / 2, boxDiameter / 2)
                ,forbiddenBlockPos, minY, maxY, Arrays.stream(requiredBlocks).collect(Collectors.toCollection(ArrayList::new)));
    }
    public static List<BlockPos> findBlock(int boxDiameter, ArrayList<BlockPos> forbiddenBlockPos, int minY, int maxY, Block... requiredBlocks) {
        return findBlock(new Box(-boxDiameter / 2, boxDiameter / 2, -boxDiameter / 2, boxDiameter / 2, -boxDiameter / 2, boxDiameter / 2)
                ,forbiddenBlockPos, minY, maxY, addData(Arrays.stream(requiredBlocks).collect(Collectors.toCollection(ArrayList::new))));
    }
    
    public static List<BlockPos> findBlock(Box searchBox, ArrayList<BlockPos> forbiddenBlockPos, int minY, int maxY, Block... requiredBlocks) {
        return findBlock(searchBox, forbiddenBlockPos, minY, maxY, addData(Arrays.stream(requiredBlocks).collect(Collectors.toCollection(ArrayList::new))));
    }


    public static List<BlockPos> findBlock(Box searchBox, ArrayList<BlockPos> forbiddenBlockPos, int minY, int maxY, ArrayList<BlockData<EnumDyeColor>> requiredBlock) {

        List<BlockPos> foundBlocks = new ArrayList<>();
        if(forbiddenBlockPos != null && !forbiddenBlockPos.isEmpty())
            forbiddenBlockPos.forEach(System.out::println);

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
    public static Block getRelativeBlock(float rightOffset, float upOffset, float frontOffset, float closetPlayerYaw) {
        return getBlock(getRelativeBlockPos(rightOffset, upOffset, frontOffset, closetPlayerYaw));
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

    public static boolean scanBox(OffsetBox box, List<Block> blocksAllowed, List<Block> blocksForbidden, float closetPlayerYaw) {
        BlockPos check;

        for (int i = -1; i < Math.abs(box.right_bound1 - box.right_bound2); i++) {
            for (int j = -1; j < Math.abs(box.up_bound1 - box.up_bound2); j++) {
                for (int k = -1; k < Math.abs(box.front_bound1 - box.front_bound2); k++) {
                    check = BlockUtils.getRelativeBlockPos(
                            i + 1 + Math.min(box.right_bound2, box.right_bound1),
                            j + 1 + Math.min(box.up_bound2, box.up_bound1),
                            k + 1 + Math.min(box.front_bound2, box.front_bound1),
                            closetPlayerYaw);
                    if(blocksAllowed != null && (!blocksAllowed.contains(getBlock(check)) || !blocksAllowed.contains(getBlock(check.up()))))
                        return true;
                    if(blocksForbidden != null && (blocksForbidden.contains(getBlock(check)) || !blocksForbidden.contains(getBlock(check.up()))))
                        return true;
                }
            }
        }
        return false;
    }

    // yes I know there are better approaches than this :skull: but I'm just lazy
    public static boolean isInsideBox(OffsetBox box, BlockPos pos, float closetPlayerYaw) {
        for (int i = -1; i < Math.abs(box.right_bound1 - box.right_bound2); i++) {
            for (int j = -1; j < Math.abs(box.up_bound1 - box.up_bound2); j++) {
                for (int k = -1; k < Math.abs(box.front_bound1 - box.front_bound2); k++) {
                    if(BlockUtils.getRelativeBlockPos(
                            i + 1 + Math.min(box.right_bound2, box.right_bound1),
                            j + 1 + Math.min(box.up_bound2, box.up_bound1),
                            k + 1 + Math.min(box.front_bound2, box.front_bound1),
                            closetPlayerYaw).equals(pos))
                        return true;
                }
            }
        }
        return false;
    }

    public static BlockPos getRelativeBlockPos(float rightOffset, float upOffset, float frontOffset, float closetPlayerYaw) {
        int unitX = getUnitX(closetPlayerYaw);
        int unitZ = getUnitZ(closetPlayerYaw);
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



    // blockpos at x/X (X represents player pos) -> returns true
    // x
    //xXx
    // x
    public static boolean isAxisAdjacent(BlockPos bp){
        return onTheSameXZ(bp.east(), BlockUtils.getPlayerLoc()) ||
                onTheSameXZ(bp.south(), BlockUtils.getPlayerLoc()) ||
                onTheSameXZ(bp.north(), BlockUtils.getPlayerLoc()) ||
                onTheSameXZ(bp.west(), BlockUtils.getPlayerLoc()) || onTheSameXZ(bp, BlockUtils.getPlayerLoc());
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
        return walkables.contains(block);
    }

    public static boolean isPassable(BlockPos block) {
        return isPassable(getBlock(block));
    }

    public static boolean canWalkOn(Block groundBlock) {
        return !cannotWalkOn.contains(groundBlock);
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

    public static boolean canMineBlock(BlockPos b){
        return !BlockUtils.getAllVisibilityLines(b, mc.thePlayer.getPositionVector().add(new Vec3(0, mc.thePlayer.getEyeHeight(), 0))).isEmpty();
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
        for (BlockPos pos : getAllBlocksInLine2d(b1, b2)) {
            if (!BlockUtils.isPassable(pos))
                return true;
        }
        return false;
    }

    public static ArrayList<BlockPos> getAllBlocksInLine2d(BlockPos b1, BlockPos b2){
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


    public static ArrayList<BlockPos> GetAllBlocksInline3d(BlockPos pos1, BlockPos pos2) {
        ArrayList<BlockPos> returnBlocks = new ArrayList<>();

        Vec3 startPos = new Vec3(pos1.getX() + 0.5, pos1.getY() + 1 + mc.thePlayer.getDefaultEyeHeight() - 0.125, pos1.getZ() + 0.5);
        Vec3 endPos = new Vec3(pos2.getX() + 0.5, pos2.getY() + 0.5, pos2.getZ() + 0.5);

        Vec3 direction = new Vec3(endPos.xCoord - startPos.xCoord, endPos.yCoord - startPos.yCoord, endPos.zCoord - startPos.zCoord);

        Tuple<Float, Float> rotation = getRotation(endPos, startPos);

        double maxDistance = startPos.distanceTo(endPos);

        double increment = 0.3f;

        double x_offset = -Math.sin(Math.toRadians(rotation.getFirst())) * Math.cos(Math.toRadians(rotation.getSecond()));
        double y_offset = -Math.sin(Math.toRadians(rotation.getSecond()));
        double z_offset = Math.cos(Math.toRadians(rotation.getFirst())) * Math.cos(Math.toRadians(rotation.getSecond()));

        Vec3 currentPos = startPos;


        while (currentPos.distanceTo(startPos) < maxDistance) {

            ArrayList<BlockPos> blocks = AnyBlockAroundVec3(currentPos, 0.15f);

            for (BlockPos pos : blocks) {

                Block block = mc.theWorld.getBlockState(pos).getBlock();

                // Add the block to the list if it hasn't been added already
                if (!returnBlocks.contains(pos) && !mc.theWorld.isAirBlock(pos) && !pos.equals(pos1) && !pos.equals(pos2) && block != Blocks.stained_glass && block != Blocks.stained_glass_pane) {
                    returnBlocks.add(pos);
                }
            }

            // Move along the line by the specified increment
            currentPos = currentPos.add(new Vec3(x_offset * increment, y_offset * increment, z_offset * increment));
        }
        return returnBlocks;
    }

    // 0 = always closet
    public static Vec3 getCloserVisibilityLine(BlockPos pos, int randomness){
        ArrayList<Vec3> lines = getAllVisibilityLines(pos, mc.thePlayer.getPositionVector().add(new Vec3(0, mc.thePlayer.getEyeHeight(), 0)));
        if(lines.isEmpty()){
            return null;
        }
        lines.sort(sortVec3ByRotation);
        return lines.get(randomness == 0 ? 0 : Math.min(lines.size() - 1, rnd.nextInt(randomness))); // rnd.nextInt(0) will throw IllegalArgument Exception
    }

    public static Vec3 getClosetVisibilityLine(BlockPos pos){
        return getCloserVisibilityLine(pos, 0);
    }

    private static float getRotationCost(float yaw, float pitch){
        return Math.abs(getAngleDifference(getActualRotationYaw(yaw), getActualRotationYaw())) + Math.abs(mc.thePlayer.rotationPitch - pitch);
    }
    public static Vec3 getRandomVisibilityLine(BlockPos pos) {
        BlockPos playerLoc = BlockUtils.getPlayerLoc();
        boolean lowerY = (pos.getY() < playerLoc.getY() && Math.abs(pos.getX() - playerLoc.getX()) <= 1 && Math.abs(pos.getZ() - playerLoc.getZ()) <= 1);
        ArrayList<Vec3> lines = getAllVisibilityLines(pos, mc.thePlayer.getPositionVector().add(new Vec3(0, mc.thePlayer.getEyeHeight(), 0)).subtract(new Vec3(0, lowerY ? MightyMiner.config.aotvMiningCobblestoneAccuracy : 0, 0)), lowerY);
        if (lines.isEmpty()) {
            return null;
        } else {
            return lines.get(new Random().nextInt(lines.size()));
        }
    }

    public static ArrayList<Vec3> getAllVisibilityLines(BlockPos pos, Vec3 fromEye) {
        return getAllVisibilityLines(pos, fromEye, false);
    }

    public static ArrayList<Vec3> getAllVisibilityLines(BlockPos pos, Vec3 fromEye, boolean lowerY) {
        ArrayList<Vec3> lines = new ArrayList<>();
        int accuracyChecks = MightyMiner.config.aotvMiningAccuracyChecks;
        float accuracy = 1f / accuracyChecks;
        float spaceFromEdge = lowerY ? 0.1f : MightyMiner.config.aotvMiningAccuracy;
        for (float x = pos.getX() + spaceFromEdge; x <= pos.getX() + (1f - spaceFromEdge); x += accuracy) {
            for (float y = pos.getY() + spaceFromEdge; y <= pos.getY() + (1f - spaceFromEdge); y += accuracy) {
                for (float z = pos.getZ() + spaceFromEdge; z <= pos.getZ() + (1f - spaceFromEdge); z += accuracy) {
                    Vec3 target = new Vec3(x, y, z);
                    if (fromEye.distanceTo(target) > 4.5f) {
                        continue;
                    }
                    BlockPos test = new BlockPos(target.xCoord, target.yCoord, target.zCoord);
                    MovingObjectPosition movingObjectPosition = mc.theWorld.rayTraceBlocks(fromEye, target, false, false, true);
                    if (movingObjectPosition != null) {
                        BlockPos obj = movingObjectPosition.getBlockPos();
                        if (obj.equals(test))
                            lines.add(target);
                    }
                }
            }
        }

        return lines;
    }



}
