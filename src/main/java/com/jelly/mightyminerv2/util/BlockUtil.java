package com.jelly.mightyminerv2.util;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.pathfinder.helper.BlockStateAccessor;
import com.jelly.mightyminerv2.pathfinder.movement.CalculationContext;
import com.jelly.mightyminerv2.pathfinder.movement.MovementHelper;
import com.jelly.mightyminerv2.util.helper.Angle;
import com.jelly.mightyminerv2.util.helper.heap.MinHeap;
import kotlin.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.*;
import java.util.stream.Collectors;


public class BlockUtil {

    // Credit: GTC
    public static final Map<EnumFacing, float[]> BLOCK_SIDES = new HashMap<EnumFacing, float[]>() {{
        put(EnumFacing.DOWN, new float[]{0.5f, 0.01f, 0.5f});
        put(EnumFacing.UP, new float[]{0.5f, 0.99f, 0.5f});
        put(EnumFacing.WEST, new float[]{0.01f, 0.5f, 0.5f});
        put(EnumFacing.EAST, new float[]{0.99f, 0.5f, 0.5f});
        put(EnumFacing.NORTH, new float[]{0.5f, 0.5f, 0.01f});
        put(EnumFacing.SOUTH, new float[]{0.5f, 0.5f, 0.99f});
        put(null, new float[]{0.5f, 0.5f, 0.5f}); // Handles the null case
    }};
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static BlockPos getBlockLookingAt() {
        return mc.objectMouseOver.getBlockPos();
    }

    public static List<BlockPos> getWalkableBlocksAround(BlockPos playerPos) {
        List<BlockPos> walkableBlocks = new ArrayList<>();
        BlockStateAccessor bsa = new BlockStateAccessor(mc.theWorld);
        int yOffset = MovementHelper.INSTANCE.isBottomSlab(bsa.get(playerPos.getX(), playerPos.getY(), playerPos.getZ())) ? -1 : 0;

        for (int i = -1; i <= 1; i++) {
            for (int j = yOffset; j <= 0; j++) {
                for (int k = -1; k <= 1; k++) {
                    int x = playerPos.getX() + i;
                    int y = playerPos.getY() + j;
                    int z = playerPos.getZ() + k;

                    if (MovementHelper.INSTANCE.canStandOn(bsa, x, y, z, bsa.get(x, y, z)) &&
                            MovementHelper.INSTANCE.canWalkThrough(bsa, x, y + 1, z, bsa.get(x, y + 1, z)) &&
                            MovementHelper.INSTANCE.canWalkThrough(bsa, x, y + 2, z, bsa.get(x, y + 2, z))) {
                        walkableBlocks.add(new BlockPos(x, y, z));
                    }
                }
            }
        }
        return walkableBlocks;
    }

    /**
     * Finds mineable blocks that are accessible by walking from the player's current position.
     * Implementation that scans blocks around all walkable positions.
     *
     * @param blockPriorities Map of block state IDs to their priority values (higher priority = higher chance of selection, 0 = no chance of selection)
     * @param blockToIgnore BlockPos that should be ignored in the search
     * @param miningSpeed Player's current mining speed
     * @return List of BlockPos sorted by mining efficiency (most efficient first)
     */
    public static List<BlockPos> findMineableBlocksFromAccessiblePositions(
            Map<Integer, Integer> blockPriorities, BlockPos blockToIgnore, int miningSpeed) {

        final MinHeap<BlockPos> blocks = new MinHeap<>(500);
        final Set<Long> visitedPositions = new HashSet<>(1000);

        final BlockPos playerBlock = PlayerUtil.getBlockStandingOn();
        final List<BlockPos> walkableBlocks = getWalkableBlocksAround(playerBlock);

        if (blockToIgnore != null) {
            visitedPositions.add(longHash(blockToIgnore.getX(), blockToIgnore.getY(), blockToIgnore.getZ()));
        }

        for (final BlockPos blockPos : walkableBlocks) {

            final Vec3 eye = new Vec3(blockPos.getX() + 0.5d, blockPos.getY() + mc.thePlayer.eyeHeight, blockPos.getZ() + 0.5d);

            MinHeap<BlockPos> batch = findMineableBlocksAroundPoint(eye, blockPriorities, visitedPositions, miningSpeed);
            for (BlockPos pos : batch.getBlocks()) {
                double cost = batch.getCost(pos);
                blocks.add(pos, cost);
            }
        }
        
        return blocks.getBlocks();
    }

    /**
     * Finds mineable blocks in a fixed area around the player's head position.
     * Implementation that scans blocks in a simple cubic area.
     *
     * @param blockPriorities Map of block state IDs to their priority values (higher priority = higher chance of selection, 0 = no chance of selection)
     * @param blockToIgnore BlockPos that should be ignored in the search
     * @param miningSpeed Player's current mining speed
     * @return List of BlockPos sorted by mining efficiency (most efficient first)
     */
    public static List<BlockPos> findMineableBlocksAroundHead(
            Map<Integer, Integer> blockPriorities, BlockPos blockToIgnore, int miningSpeed) {
        final Vec3 eye = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.eyeHeight, mc.thePlayer.posZ);

        final Set<Long> blocksToIgnore = new HashSet<>();
        if (blockToIgnore != null) {
            blocksToIgnore.add(longHash(blockToIgnore.getX(), blockToIgnore.getY(), blockToIgnore.getZ()));
        }

        return findMineableBlocksAroundPoint(eye, blockPriorities, blocksToIgnore, miningSpeed).getBlocks();
    }


    public static MinHeap<BlockPos> findMineableBlocksAroundPoint(Vec3 point, Map<Integer, Integer> blockPriorities, Set<Long> blocksToIgnore, int miningSpeed) {
        final MinHeap<BlockPos> blocks = new MinHeap<>(500);

        final int HORIZONTAL_RADIUS = 5;
        final int VERTICAL_LOWER = -3;
        final int VERTICAL_UPPER = 4;
        final float MAX_DISTANCE = 3.5f;  // Maximum reach for player is 4, 0.5 for buffer

        // Pre-calculations
        final float miningCoefficient = 500.0f / miningSpeed;
        final float angleCoefficient = MightyMinerConfig.devMithRot;

        // Calculate bounds for the block
        final double baseX = point.xCoord;
        final double baseY = point.yCoord;
        final double baseZ = point.zCoord;

        // Process the blocks in an optimized order (Y first for better cache locality)
        for (int y = VERTICAL_LOWER; y <= VERTICAL_UPPER; y++) {
            final double actualY = baseY + y;

            for (int x = -HORIZONTAL_RADIUS; x <= HORIZONTAL_RADIUS; x++) {
                final double actualX = baseX + x;

                for (int z = -HORIZONTAL_RADIUS; z <= HORIZONTAL_RADIUS; z++) {
                    final double actualZ = baseZ + z;

                    final BlockPos pos = new BlockPos(actualX, actualY, actualZ);

                    // Skip if in ignore
                    final long hash = longHash(pos.getX(), pos.getY(), pos.getZ());
                    if (blocksToIgnore.contains(hash)) {
                        continue;
                    }

                    // Mark as visited immediately
                    blocksToIgnore.add(hash);

                    // The maximum reach for player is 4 blocks
                    final double dx = baseX - actualX;
                    final double dy = baseY - actualY;
                    final double dz = baseZ - actualZ;
                    final double distSq = dx * dx + dy * dy + dz * dz;

                    if (distSq > MAX_DISTANCE * MAX_DISTANCE) {
                        continue;
                    }

                    // Check if it's a target block
                    final int stateID = Block.getStateId(mc.theWorld.getBlockState(pos));
                    if(!blockPriorities.containsKey(stateID))
                        continue;

                    final int blockPriority = blockPriorities.get(stateID);

                    // 0 means no chance for selection
                    if (blockPriority == 0)
                        continue;

                    // Check visibility
                    if (!hasVisibleSide(pos)) {
                        continue;
                    }

                    // Calculate mining cost components
                    final double hardness = getBlockStrength(stateID);
                    final float angleChange = AngleUtil.getNeededChange(AngleUtil.getPlayerAngle(), AngleUtil.getRotation(pos)).lengthSqrt();

                    // Calculate final cost and add to heap
                    final float miningCost = (float) ((hardness * miningCoefficient) + (angleChange * angleCoefficient)) / blockPriority;

                    blocks.add(pos, miningCost);
                }
            }
        }
        return blocks;
    }


    public static int getBlockStrength(int stateID) {

        switch (stateID) {
            case 57:  // Diamond Block
            case 41:  // Gold Block
            case 152: // Redstone Block
            case 22:  // Lapis Block
            case 133: // Emerald Block
            case 42:  // Iron Block
            case 173: // Coal Block
                return 600;

            case 19:  // Sponge
                return 500;

            case 1:   // Stone - strength of hardstone
                return 50;

            case 16385: // polished diorite
                return 2000;
            case 28707: // gray wool
                return 500;
            case 12323: // light blue wool
                return 1500;
            case 37023: // cyan stained clay
                return 500;

            case 168: // Prismarine
            case 4264: // dark prismrine
            case 8360: // brick prismarine
                return 800;

            case 95:    // opal
            case 160:
            case 16544: // topaz
            case 16479:
                return 3800;
            case 4191:  // amber
            case 4256:
            case 12383: // sapphire
            case 12448:
            case 20575: // jade
            case 20640:
            case 41055: // amethyst
            case 41120:
                return 3000;
            case 8287:  // jasper
            case 8352:
                return 4800;
            case 45151: // aquamarine
            case 45216:
            case 53343: // peridot
            case 53408:
            case 61535: // onyx
            case 61600:
            case 49247: // citrine
            case 49312:
                return 5200;
            case 57504: // ruby
            case 57439:
                return 2300;
            default:
                break;
        }

        return 5000;
    }

    public static int getMiningTime(int stateId, final int miningSpeed) {
        return (int) Math.ceil((getBlockStrength(stateId) * 30) / (float) miningSpeed) + MightyMinerConfig.tickGlideOffset;
    }

    public static Vec3 getSidePos(BlockPos block, EnumFacing face) {
        final float[] offset = BLOCK_SIDES.get(face);
        return new Vec3(block.getX() + offset[0], block.getY() + offset[1], block.getZ() + offset[2]);
    }

    public static boolean canSeeSide(BlockPos block, EnumFacing side) {
        return RaytracingUtil.canSeePoint(getSidePos(block, side));
    }

    public static boolean canSeeSide(Vec3 from, BlockPos block, EnumFacing side) {
        return RaytracingUtil.canSeePoint(from, getSidePos(block, side));
    }

    public static List<EnumFacing> getAllVisibleSides(BlockPos block) {
        final List<EnumFacing> sides = new ArrayList<>();
        for (EnumFacing face : BLOCK_SIDES.keySet()) {
            if (face != null && !mc.theWorld.getBlockState(block).getBlock()
                    .shouldSideBeRendered(mc.theWorld, block.offset(face), face)) {
                continue;
            }
            if (canSeeSide(block, face)) {
                sides.add(face);
            }
        }
        return sides;
    }

    public static List<EnumFacing> getAllVisibleSides(Vec3 from, BlockPos block) {
        final List<EnumFacing> sides = new ArrayList<>();
        for (EnumFacing face : BLOCK_SIDES.keySet()) {
            if (face != null && !mc.theWorld.getBlockState(block).getBlock().shouldSideBeRendered(mc.theWorld, block.offset(face), face)) {
                continue;
            }
            if (canSeeSide(from, block, face)) {
                sides.add(face);
            }
        }
        return sides;
    }

    public static Vec3 getClosestVisibleSidePos(BlockPos block) {
        EnumFacing face = null;
        if (mc.theWorld.isBlockFullCube(block)) {
            final Vec3 eyePos = mc.thePlayer.getPositionEyes(1);
            double dist = Double.MAX_VALUE;
            for (EnumFacing side : BLOCK_SIDES.keySet()) {
                if (side != null && !mc.theWorld.getBlockState(block).getBlock().shouldSideBeRendered(mc.theWorld, block.offset(side), side)) {
                    continue;
                }
                final double distanceToThisSide = eyePos.distanceTo(getSidePos(block, side));
                if (canSeeSide(block, side) && distanceToThisSide < dist) {
                    if (side == null && face != null) {
                        continue;
                    }
                    dist = distanceToThisSide;
                    face = side;
                }
            }
        }
        final float[] offset = BLOCK_SIDES.get(face);
        return new Vec3(block.getX() + offset[0], block.getY() + offset[1], block.getZ() + offset[2]);
    }


    public static Vec3 getClosestVisibleSidePos(Vec3 from, BlockPos block) {
        EnumFacing face = null;
        if (mc.theWorld.isBlockFullCube(block)) {
            double dist = Double.MAX_VALUE;
            for (EnumFacing side : BLOCK_SIDES.keySet()) {
                if (side != null && !mc.theWorld.getBlockState(block).getBlock().shouldSideBeRendered(mc.theWorld, block.offset(side), side)) {
                    continue;
                }
                final double distanceToThisSide = from.distanceTo(getSidePos(block, side));
                if (canSeeSide(from, block, side) && distanceToThisSide < dist) {
                    if (side == null && face != null) {
                        continue;
                    }
                    dist = distanceToThisSide;
                    face = side;
                }
            }
        }
        final float[] offset = BLOCK_SIDES.get(face);
        return new Vec3(block.getX() + offset[0], block.getY() + offset[1], block.getZ() + offset[2]);
    }

    public static EnumFacing getClosestVisibleSide(BlockPos block) {
        if (!mc.theWorld.isBlockFullCube(block)) {
            return null;
        }
        final Vec3 eyePos = mc.thePlayer.getPositionEyes(1);
        double dist = Double.MAX_VALUE;
        EnumFacing face = null;
        for (EnumFacing side : BLOCK_SIDES.keySet()) {
            if (side != null && !mc.theWorld.getBlockState(block).getBlock()
                    .shouldSideBeRendered(mc.theWorld, block.offset(side), side)) {
                continue;
            }
            final double distanceToThisSide = eyePos.distanceTo(getSidePos(block, side));
            if (canSeeSide(block, side) && distanceToThisSide < dist) {
                if (side == null && face != null) {
                    continue;
                }
                dist = distanceToThisSide;
                face = side;
            }
        }
        return face;
    }

    public static EnumFacing getClosestVisibleSide(Vec3 from, BlockPos block) {
        if (!mc.theWorld.isBlockFullCube(block)) {
            return null;
        }
        double dist = Double.MAX_VALUE;
        EnumFacing face = null;
        for (EnumFacing side : BLOCK_SIDES.keySet()) {
            if (side != null && !mc.theWorld.getBlockState(block).getBlock().shouldSideBeRendered(mc.theWorld, block.offset(side), side)) {
                continue;
            }
            final double distanceToThisSide = from.distanceTo(getSidePos(block, side));
            if (canSeeSide(from, block, side) && distanceToThisSide < dist) {
                if (side == null && face != null) {
                    continue;
                }
                dist = distanceToThisSide;
                face = side;
            }
        }
        return face;
    }

    public static boolean hasVisibleSide(BlockPos block) {
        if (!mc.theWorld.isBlockFullCube(block)) {
            return false;
        }
        for (EnumFacing side : EnumFacing.values()) {
            if (side != null && !mc.theWorld.getBlockState(block).getBlock().shouldSideBeRendered(mc.theWorld, block.offset(side), side)) {
                continue;
            }
            if (canSeeSide(block, side)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasVisibleSide(Vec3 from, BlockPos block) {
        if (!mc.theWorld.isBlockFullCube(block)) {
            return false;
        }
        for (EnumFacing side : EnumFacing.values()) {
            if (!mc.theWorld.getBlockState(block).getBlock().shouldSideBeRendered(mc.theWorld, block.offset(side), side)) {
                continue;
            }
            if (canSeeSide(from, block, side)) {
                return true;
            }
        }
        return false;
    }

    public static List<Vec3> bestPointsOnBestSide(final BlockPos block) {
        return pointsOnBlockSide(block, getClosestVisibleSide(block)).stream()
                .filter(RaytracingUtil::canSeePoint)
                .sorted(Comparator.comparingDouble(i -> AngleUtil.getNeededChange(AngleUtil.getPlayerAngle(), AngleUtil.getRotation(i)).getValue()))
                .collect(Collectors.toList());
    }

    public static List<Vec3> bestPointsOnBestSide(Vec3 from, final BlockPos block) {
        return pointsOnBlockSide(block, getClosestVisibleSide(from, block)).stream()
                .filter(it -> RaytracingUtil.canSeePoint(from, it))
                .sorted(Comparator.comparingDouble(i -> AngleUtil.getNeededChange(AngleUtil.getPlayerAngle(), AngleUtil.getRotation(from, i)).getValue()))
                .collect(Collectors.toList());
    }

    public static List<Vec3> bestPointsOnVisibleSides(final BlockPos block) {
        return pointsOnVisibleSides(block).stream()
                .filter(RaytracingUtil::canSeePoint)
                .sorted(Comparator.comparingDouble(mc.thePlayer.getPositionEyes(1)::distanceTo))
                .collect(Collectors.toList());
    }

    public static List<Vec3> bestPointsOnVisibleSides(Vec3 from, final BlockPos block) {
        return pointsOnVisibleSides(block).stream()
                .filter(it -> RaytracingUtil.canSeePoint(from, it))
                .sorted(Comparator.comparingDouble(from::distanceTo))
                .collect(Collectors.toList());
    }

    // Should not use this because it won't ensure the points can be looked at
    private static List<Vec3> pointsOnVisibleSides(final BlockPos block) {
        final List<Vec3> points = new ArrayList<>();
        for (EnumFacing side : getAllVisibleSides(block)) {
            points.addAll(pointsOnBlockSide(block, side));
        }
        return points;
    }

    private static List<Vec3> pointsOnVisibleSides(Vec3 from, final BlockPos block) {
        final List<Vec3> points = new ArrayList<>();
        for (EnumFacing side : getAllVisibleSides(from, block)) {
            points.addAll(pointsOnBlockSide(block, side));
        }
        return points;
    }

    // Credits to GTC <3
    private static List<Vec3> pointsOnBlockSide(final BlockPos block, final EnumFacing side) {
        final Set<Vec3> points = new HashSet<>();

        if (side != null) {
            float[] it = BLOCK_SIDES.get(side);
            for (int i = 0; i < 20; i++) {
                float x = it[0];
                float y = it[1];
                float z = it[2];
                if (x == 0.5f) {
                    x = randomVal();
                }
                if (y == 0.5f) {
                    y = randomVal();
                }
                if (z == 0.5f) {
                    z = randomVal();
                }
                Vec3 point = new Vec3(block).addVector(x, y, z);
                points.add(point);
            }
        } else {
            for (float[] bside : BLOCK_SIDES.values()) {
                for (int i = 0; i < 20; i++) {
                    float x = bside[0];
                    float y = bside[1];
                    float z = bside[2];
                    if (x == 0.5f) {
                        x = randomVal();
                    }
                    if (y == 0.5f) {
                        y = randomVal();
                    }
                    if (z == 0.5f) {
                        z = randomVal();
                    }
                    Vec3 point = new Vec3(block).addVector(x, y, z);
                    points.add(point);
                }
            }
        }
        return new ArrayList<>(points);
    }

    private static float randomVal() {
        return (new Random().nextInt(6) + 2) / 10.0f;
    }

    public static boolean canWalkBetween(CalculationContext ctx, BlockPos start, BlockPos end) {
        int ey = end.getY();
        int ex = end.getX();
        int ez = end.getZ();
        IBlockState endState = ctx.get(ex, ey, ez);
        if (!MovementHelper.INSTANCE.canStandOn(ctx.getBsa(), ex, ey, ez, endState)) {
//      Logger.sendLog("Cannot stand on x: " + ex + ", y: " + ey + ", z: " + ez);
            return false;
        }
        if (!MovementHelper.INSTANCE.canWalkThrough(ctx.getBsa(), ex, ey + 1, ez, ctx.get(ex, ey + 1, ez))) {
//      Logger.sendLog("Cannot walk throug x: " + ex + ", y: " + (ey + 1) + ", z: " + ez);
            return false;
        }
        if (!MovementHelper.INSTANCE.canWalkThrough(ctx.getBsa(), ex, ey + 2, ez, ctx.get(ex, ey + 2, ez))) {
//      Logger.sendLog("Cannot walk throug x: " + ex + ", y: " + (ey + 2) + ", z: " + ez);
            return false;
        }
        return !com.jelly.mightyminerv2.pathfinder.util.BlockUtil.INSTANCE.bresenham(ctx, start, end);
//    int sy = start.getY();
//    if(ey - sy > 1){
////      Logger.sendLog("ey - sy > 1");
//      return true;
//    }
//
//    int sx = start.getX();
//    int sz = start.getZ();
//    IBlockState fromState = ctx.get(sx, sy, sz);
//
//    boolean srcSmall = MovementHelper.INSTANCE.isBottomSlab(fromState);
//    boolean destSmall = MovementHelper.INSTANCE.isBottomSlab(endState);
//    boolean destSmallStair = MovementHelper.INSTANCE.isValidStair(endState, ex - sx, ez - sz);
////    Logger.sendLog("SrcSmall: " + srcSmall + ", DestSmall: " + destSmall + ", DestSmallStair: " + destSmallStair);
//    if (!srcSmall == !(destSmall || destSmallStair) && !srcSmall && !destSmallStair) {
//      return true;
//    }
//    return false;
    }

    public static boolean canWalkBetween(CalculationContext ctx, Vec3 start, Vec3 end) {
        int ey = MathHelper.floor_double(end.yCoord);
        int ex = MathHelper.floor_double(end.xCoord);
        int ez = MathHelper.floor_double(end.zCoord);
//    int ex = end.getX();
//    int ez = end.getZ();
        IBlockState endState = ctx.get(ex, ey, ez);
        if (!MovementHelper.INSTANCE.canStandOn(ctx.getBsa(), ex, ey, ez, endState)) {
//      Logger.sendLog("Cannot stand on x: " + ex + ", y: " + ey + ", z: " + ez);
            return false;
        }
        if (!MovementHelper.INSTANCE.canWalkThrough(ctx.getBsa(), ex, ey + 1, ez, ctx.get(ex, ey + 1, ez))) {
//      Logger.sendLog("Cannot walk throug x: " + ex + ", y: " + (ey + 1) + ", z: " + ez);
            return false;
        }
        if (!MovementHelper.INSTANCE.canWalkThrough(ctx.getBsa(), ex, ey + 2, ez, ctx.get(ex, ey + 2, ez))) {
//      Logger.sendLog("Cannot walk throug x: " + ex + ", y: " + (ey + 2) + ", z: " + ez);
            return false;
        }
        return !com.jelly.mightyminerv2.pathfinder.util.BlockUtil.INSTANCE.bresenham(ctx, start, end);
    }

    public static boolean canStandOn(BlockPos pos) {
        BlockStateAccessor bsa = new BlockStateAccessor(mc.theWorld);
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        return MovementHelper.INSTANCE.canStandOn(bsa, x, y, z, bsa.get(x, y, z)) &&
                MovementHelper.INSTANCE.canWalkThrough(bsa, x, y + 1, z, bsa.get(x, y + 1, z)) &&
                MovementHelper.INSTANCE.canWalkThrough(bsa, x, y + 2, z, bsa.get(x, y + 2, z));
    }


    // Stole from baritoe
    public static long longHash(int x, int y, int z) {
        long hash = 3241;
        hash = 3457689L * hash + x;
        hash = 8734625L * hash + y;
        hash = 2873465L * hash + z;
        return hash;
    }
}
