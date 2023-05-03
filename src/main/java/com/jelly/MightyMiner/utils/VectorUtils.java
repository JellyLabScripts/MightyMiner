package com.jelly.MightyMiner.utils;

import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.Utils.MathUtils;
import net.minecraft.init.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.util.MathHelper;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.AxisAlignedBB;
import java.util.List;
import java.util.ArrayList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.apache.commons.lang3.tuple.Pair;

public class VectorUtils {

    public static Pair<Float, Float> vec3ToRotation(final Vec3 vec) {
        final double diffX = vec.xCoord - PlayerUtils.playerPosVec().xCoord;
        final double diffY = vec.yCoord - PlayerUtils.playerPosVec().yCoord - PlayerUtils.playerEyeHeight();
        final double diffZ = vec.zCoord - PlayerUtils.playerPosVec().zCoord;
        final double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float pitch = (float)(-Math.atan2(dist, diffY));
        float yaw = (float)Math.atan2(diffZ, diffX);
        pitch = (float)AngleUtils.wrapAngleTo180((pitch * 180.0f / 3.141592653589793 + 90.0) * -1.0);
        yaw = (float)AngleUtils.wrapAngleTo180(yaw * 180.0f / 3.141592653589793 - 90.0);
        return Pair.of(yaw, pitch);
    }
    public static Vec3 scaleVec(final Vec3 vec, final float scale) {
        return new Vec3(vec.xCoord * scale, vec.yCoord * scale, vec.zCoord * scale);
    }

    public static Vec3 scaleVec(final Vec3 vec, final double scale) {
        return new Vec3(vec.xCoord * scale, vec.yCoord * scale, vec.zCoord * scale);
    }

    public static Vec3 divideVec(final Vec3 vec, final float scale) {
        return new Vec3(vec.xCoord / scale, vec.yCoord / scale, vec.zCoord / scale);
    }

    public static Vec3 divideVec(final Vec3 vec, final double scale) {
        return new Vec3(vec.xCoord / scale, vec.yCoord / scale, vec.zCoord / scale);
    }

    public static Vec3 offsetVec(final Vec3 original, final double offset) {
        return new Vec3(original.xCoord + offset, original.yCoord + offset, original.zCoord + offset);
    }

    public static Vec3 offsetVec(final Vec3 original, final double xOffset, final double yOffset, final double zOffset) {
        return new Vec3(original.xCoord + xOffset, original.yCoord + yOffset, original.zCoord + zOffset);
    }

    public static MovingObjectPosition rayTrace(final float range) {
        final Vec3 vec3 = PlayerUtils.playerEyePosVec();
        final Vec3 vec4 = PlayerUtils.playerLookVec();
        return fastRayTrace(vec3, vec3.addVector(vec4.xCoord * range, vec4.yCoord * range, vec4.zCoord * range));
    }

    public static MovingObjectPosition rayTraceStopLiquid(final float range) {
        final Vec3 vec3 = PlayerUtils.playerEyePosVec();
        final Vec3 vec4 = PlayerUtils.playerLookVec();
        return fastRayTrace(vec3, vec3.addVector(vec4.xCoord * range, vec4.yCoord * range, vec4.zCoord * range), false);
    }

    public static MovingObjectPosition rayTraceLook(final Vec3 target, final float range) {
        final Vec3 pos = PlayerUtils.playerEyePosVec();
        if (pos.squareDistanceTo(target) > range * range) {
            return null;
        }
        return fastRayTrace(pos, target);
    }

    public static MovingObjectPosition rayTraceLookStopLiquid(final Vec3 target, final float range) {
        final Vec3 pos = PlayerUtils.playerEyePosVec();
        if (pos.squareDistanceTo(target) > range * range) {
            return null;
        }
        return fastRayTrace(pos, target, false);
    }

    public static MovingObjectPosition rayTraceLook(final Vec3 position, final Vec3 target, final float range) {
        if (position.squareDistanceTo(target) > range * range) {
            return null;
        }
        return fastRayTrace(position, target);
    }

    public static MovingObjectPosition rayTraceLook(final Vec3 position, final Vec3 target) {
        return fastRayTrace(position, target);
    }

    public static MovingObjectPosition rayTraceLook(final Vec3 target) {
        return fastRayTrace(PlayerUtils.playerEyePosVec(), target);
    }

    public static boolean isRayTraceableLook(final Vec3 target, final BlockPos goal, final float range) {
        final Vec3 vec3 = PlayerUtils.playerEyePosVec();
        return vec3.squareDistanceTo(target) <= range * range && rayTraceable(vec3, target, goal);
    }

    public static boolean isRayTraceableLook(final Vec3 position, final Vec3 target, final BlockPos goal, final float range) {
        return position.squareDistanceTo(target) <= range * range && rayTraceable(position, target, goal);
    }

    public static boolean isRayTraceableLook(final Vec3 position, final Vec3 target, final BlockPos goal) {
        return rayTraceable(position, target, goal);
    }

    public static boolean isRayTraceableLook(final Vec3 target, final BlockPos goal) {
        return rayTraceable(PlayerUtils.playerEyePosVec(), target, goal);
    }

    public static MovingObjectPosition rayTraceTopLayer(final BlockPos pos) {
        Vec3 vec = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        MovingObjectPosition position = rayTraceLook(vec, 4.5f);
        if (position != null && position.getBlockPos().equals((Object)pos)) {
            return position;
        }
        for (int x = 1; x < 6; ++x) {
            for (int z = 1; z < 6; ++z) {
                vec = new Vec3(pos.getX() + x / 5.0f - 0.1, pos.getY() + 0.99, pos.getZ() + z / 5.0f - 0.1);
                position = rayTraceLook(vec, 4.5f);
                if (position != null && position.getBlockPos().equals((Object)pos)) {
                    return position;
                }
            }
        }
        return null;
    }

    public static Vec3 getHittableHitVec(final BlockPos pos) {
        Vec3 vec = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        MovingObjectPosition position = rayTraceLook(vec, 4.5f);
        if (position != null && position.getBlockPos().equals((Object)pos)) {
            return position.hitVec;
        }
        for (int x = 1; x < 5; ++x) {
            for (int y = 1; y < 5; ++y) {
                for (int z = 1; z < 5; ++z) {
                    vec = new Vec3(pos.getX() + x / 4.0f - 0.125, pos.getY() + y / 4.0f - 0.125, pos.getZ() + z / 4.0f - 0.125);
                    position = rayTraceLook(vec, 4.5f);
                    if (position != null && position.getBlockPos().equals((Object)pos)) {
                        return position.hitVec;
                    }
                }
            }
        }
        return null;
    }

    public static Vec3 getVeryAccurateHittableHitVec(final BlockPos pos) {
        Vec3 vec = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        MovingObjectPosition position = rayTraceLook(vec);
        if (position != null && position.getBlockPos().equals((Object)pos)) {
            return position.hitVec;
        }
        for (int x = 1; x < 9; ++x) {
            for (int y = 1; y < 9; ++y) {
                for (int z = 1; z < 9; ++z) {
                    vec = new Vec3(pos.getX() + x / 8.0f - 0.0625, pos.getY() + y / 8.0f - 0.0625, pos.getZ() + z / 8.0f - 0.0625);
                    position = rayTraceLook(vec);
                    if (position != null && position.getBlockPos().equals((Object)pos)) {
                        return position.hitVec;
                    }
                }
            }
        }
        return null;
    }

    public static MovingObjectPosition getHittableMovingObjectPosition(final BlockPos pos) {
        Vec3 vec = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        MovingObjectPosition position = rayTraceLook(vec, 4.5f);
        if (position != null && position.getBlockPos().equals((Object)pos)) {
            return position;
        }
        for (int x = 1; x < 5; ++x) {
            for (int y = 1; y < 5; ++y) {
                for (int z = 1; z < 5; ++z) {
                    vec = new Vec3(pos.getX() + x / 4.0f - 0.125, pos.getY() + y / 4.0f - 0.125, pos.getZ() + z / 4.0f - 0.125);
                    position = rayTraceLook(vec, 4.5f);
                    if (position != null && position.getBlockPos().equals((Object)pos)) {
                        return position;
                    }
                }
            }
        }
        return null;
    }

    public static Vec3 getClosestHittablePosition(final BlockPos pos, final Vec3 goal) {
        double bestDist = 9.9999999E7;
        Vec3 bestHit = null;
        for (int x = 1; x < 5; ++x) {
            for (int y = 1; y < 5; ++y) {
                for (int z = 1; z < 5; ++z) {
                    final Vec3 vec = new Vec3(pos.getX() + x / 4.0f - 0.125, pos.getY() + y / 4.0f - 0.125, pos.getZ() + z / 4.0f - 0.125);
                    final MovingObjectPosition position = rayTraceLook(vec, 4.5f);
                    if (position != null && position.getBlockPos().equals((Object)pos)) {
                        final double dist = position.hitVec.distanceTo(goal);
                        if (dist < bestDist) {
                            bestDist = dist;
                            bestHit = position.hitVec;
                        }
                    }
                }
            }
        }
        return bestHit;
    }

    public static Vec3 getClosestHittableToMiddle(final BlockPos pos) {
        final Vec3 middle = new Vec3(pos.getX() + 0.5 + MathUtils.randomFloat() / 4.0f, pos.getY() + 0.5 + MathUtils.randomFloat() / 4.0f, pos.getZ() + 0.5 + MathUtils.randomFloat() / 4.0f);
        double bestDist = 9.9999999E7;
        Vec3 bestHit = null;
        for (int x = 1; x < 6; ++x) {
            for (int y = 1; y < 6; ++y) {
                for (int z = 1; z < 6; ++z) {
                    final Vec3 vec = new Vec3((double)(pos.getX() + x / 5.0f - 0.1234f), (double)(pos.getY() + y / 5.0f - 0.1234f), (double)(pos.getZ() + z / 5.0f - 0.1234f));
                    final MovingObjectPosition position = rayTraceLook(vec, 4.5f);
                    if (position != null && position.getBlockPos().equals((Object)pos)) {
                        final double dist = position.hitVec.distanceTo(middle);
                        if (dist < bestDist) {
                            bestDist = dist;
                            bestHit = position.hitVec;
                        }
                    }
                }
            }
        }
        return bestHit;
    }

    public static Vec3 getRandomHittable(final BlockPos pos) {
        final List<Vec3> vecs = new ArrayList<Vec3>();
        for (int x = 1; x < 6; ++x) {
            for (int y = 1; y < 6; ++y) {
                for (int z = 1; z < 6; ++z) {
                    final Vec3 vec = new Vec3(pos.getX() + x / 5.0f - 0.1, pos.getY() + y / 5.0f - 0.1, pos.getZ() + z / 5.0f - 0.1);
                    final MovingObjectPosition position = rayTraceLook(vec, 4.5f);
                    if (position != null && position.getBlockPos().equals((Object)pos)) {
                        vecs.add(position.hitVec);
                    }
                }
            }
        }
        return (vecs.size() == 0) ? null : vecs.get(MathUtils.random.nextInt(vecs.size()));
    }

    public static Vec3 getRandomHittable(final BlockPos pos, final AxisAlignedBB aabb) {
        final List<Vec3> vecs = new ArrayList<Vec3>();
        for (int x = 1; x < 6; ++x) {
            for (int y = 1; y < 6; ++y) {
                for (int z = 1; z < 6; ++z) {
                    final Vec3 vec = new Vec3(pos.getX() + x / 5.0f - 0.1, pos.getY() + y / 5.0f - 0.1, pos.getZ() + z / 5.0f - 0.1);
                    if (aabb.isVecInside(vec)) {
                        final MovingObjectPosition position = rayTraceLook(vec, 4.5f);
                        if (position != null && position.getBlockPos().equals((Object)pos)) {
                            vecs.add(position.hitVec);
                        }
                    }
                }
            }
        }
        return (vecs.size() == 0) ? null : vecs.get(MathUtils.random.nextInt(vecs.size()));
    }

    public static Vec3 getRandomHittableStopLiquid(final BlockPos pos, final AxisAlignedBB aabb) {
        final List<Vec3> vecs = new ArrayList<Vec3>();
        for (int x = 1; x < 6; ++x) {
            for (int y = 1; y < 6; ++y) {
                for (int z = 1; z < 6; ++z) {
                    final Vec3 vec = new Vec3(pos.getX() + x / 5.0f - 0.1, pos.getY() + y / 5.0f - 0.1, pos.getZ() + z / 5.0f - 0.1);
                    if (aabb.isVecInside(vec)) {
                        final MovingObjectPosition position = rayTraceLookStopLiquid(vec, 100.0f);
                        if (position != null && position.getBlockPos().equals((Object)pos)) {
                            vecs.add(position.hitVec);
                        }
                    }
                }
            }
        }
        return (vecs.size() == 0) ? null : vecs.get(MathUtils.random.nextInt(vecs.size()));
    }

    public static ArrayList<Vec3> getAllHittablePosition(final BlockPos pos) {
        final ArrayList<Vec3> hittables = new ArrayList<Vec3>();
        for (int x = 1; x < 5; ++x) {
            for (int y = 1; y < 5; ++y) {
                for (int z = 1; z < 5; ++z) {
                    final Vec3 vec = new Vec3(pos.getX() + x / 4.0f - 0.125, pos.getY() + y / 4.0f - 0.125, pos.getZ() + z / 4.0f - 0.125);
                    final MovingObjectPosition position = rayTraceLook(vec, 4.5f);
                    if (position != null && position.getBlockPos().equals((Object)pos)) {
                        hittables.add(vec);
                    }
                }
            }
        }
        return hittables;
    }

    public static ArrayList<Vec3> getAllVeryAccurateHittablePosition(final BlockPos pos) {
        final ArrayList<Vec3> hittables = new ArrayList<>();
        for (int x = 1; x < 9; ++x) {
            for (int y = 1; y < 9; ++y) {
                for (int z = 1; z < 9; ++z) {
                    final Vec3 vec = new Vec3(pos.getX() + x / 8.0f - 0.0625, pos.getY() + y / 8.0f - 0.0625, pos.getZ() + z / 8.0f - 0.0625);
                    final MovingObjectPosition position = rayTraceLook(vec, 4.5f);
                    if (position != null && position.getBlockPos().equals((Object)pos)) {
                        hittables.add(position.hitVec);
                    }
                }
            }
        }
        return hittables;
    }

    public static ArrayList<Pair<BlockPos, ArrayList<Vec3>>> getAllVisibleWhitelistedBlocksWithAllHittablePos(int range, ArrayList<IBlockState> whitelist) {
        ArrayList<Pair<BlockPos, ArrayList<Vec3>>> allVisibleBlocksWithAllHittablePos = new ArrayList<>();
        BlockPos playerEyes = new BlockPos(PlayerUtils.playerEyePosVec());
        BlockPos rangeVec = new BlockPos(range, range, range);
        BlockPos from = playerEyes.subtract(rangeVec);
        BlockPos to = playerEyes.add(rangeVec);
        for (BlockPos blockPos: BlockPos.getAllInBox(from, to)) {
            ArrayList<Vec3> allHittablePos = getAllVeryAccurateHittablePosition(blockPos);
            if (whitelist.contains(BlockUtils.getBlockState(blockPos)) && allHittablePos.size() > 0) {
                allVisibleBlocksWithAllHittablePos.add(Pair.of(blockPos, allHittablePos));
            }
        }
        return allVisibleBlocksWithAllHittablePos;
    }

    public static ArrayList<Pair<BlockPos, ArrayList<Vec3>>> getAllVisibleBlocksWithAllHittablePos(int range) {
        ArrayList<Pair<BlockPos, ArrayList<Vec3>>> allVisibleBlocksWithAllHittablePos = new ArrayList<>();
        BlockPos playerEyes = new BlockPos(PlayerUtils.playerEyePosVec());
        BlockPos rangeVec = new BlockPos(range, range, range);
        BlockPos from = playerEyes.subtract(rangeVec);
        BlockPos to = playerEyes.add(rangeVec);
        for (BlockPos blockPos: BlockPos.getAllInBox(from, to)) {
            ArrayList<Vec3> allHittablePos = getAllVeryAccurateHittablePosition(blockPos);
            if (allHittablePos.size() > 0) {
                allVisibleBlocksWithAllHittablePos.add(Pair.of(blockPos, allHittablePos));
            }
        }
        return allVisibleBlocksWithAllHittablePos;
    }

    public static ArrayList<Vec3> sortClosestToFurthest(ArrayList<Vec3> positions, Vec3 referencePoint) {
        Vec3 smaller;
        Vec3 bigger;
        boolean run = true;


        for (int i = 0; i < positions.size() && run; i++) {
            run = false;

            for (int y = 0; y < positions.size()-1; y++) {
                if(positions.get(y).distanceTo(referencePoint) > positions.get(y + 1).distanceTo(referencePoint)) {
                    bigger = positions.get(y);
                    smaller = positions.get(y + 1);
                    positions.set(y, smaller);
                    positions.set(y + 1, bigger);
                    run = true;
                }
            }
        }

        return positions;
    }

    public static ArrayList<BlockPos> getAllVisibleWhitelistedBlocks(ArrayList<IBlockState> whitelist) {
        ArrayList<BlockPos> visibleWhitelistedBlocks = new ArrayList<>();

        Vec3 playerVec = PlayerUtils.playerEyePosVec();

        int distance = 4;

        Vec3 rangeVec = new Vec3(distance, distance, distance);
        Vec3 from = playerVec.subtract(rangeVec);
        Vec3 to = playerVec.add(rangeVec);

        for (BlockPos blockPos: BlockPos.getAllInBox(new BlockPos(from), new BlockPos(to))) {
            if (new Vec3(blockPos).distanceTo(playerVec) < distance && whitelist.contains(BlockUtils.getBlockState(blockPos)) && isHittable(blockPos)) {
                visibleWhitelistedBlocks.add(blockPos);
            }
        }

        return visibleWhitelistedBlocks;
    }

    public static ArrayList<Vec3> getAllHittableHitVec(final BlockPos pos) {
        final ArrayList<Vec3> hittables = new ArrayList<Vec3>();
        for (int x = 1; x < 5; ++x) {
            for (int y = 1; y < 5; ++y) {
                for (int z = 1; z < 5; ++z) {
                    final Vec3 vec = new Vec3(pos.getX() + x / 4.0f - 0.125, pos.getY() + y / 4.0f - 0.125, pos.getZ() + z / 4.0f - 0.125);
                    final MovingObjectPosition position = rayTraceLook(vec, 4.5f);
                    if (position != null && position.getBlockPos().equals((Object)pos)) {
                        hittables.add(position.hitVec);
                    }
                }
            }
        }
        return hittables;
    }

    public static boolean isHittable(final BlockPos pos) {
        if (isRayTraceableLook(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), pos, 4.5f)) {
            return true;
        }
        for (int x = 1; x < 5; ++x) {
            for (int y = 1; y < 5; ++y) {
                for (int z = 1; z < 5; ++z) {
                    if (isRayTraceableLook(new Vec3(pos.getX() + x / 4.0f - 0.125, pos.getY() + y / 4.0f - 0.125, pos.getZ() + z / 4.0f - 0.125), pos, 4.5f)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static MovingObjectPosition calculateInterceptAABBLook(final AxisAlignedBB aabb, final Vec3 look) {
        return aabb.calculateIntercept(PlayerUtils.playerEyePosVec(), look);
    }

    public static MovingObjectPosition calculateInterceptAABBLook(final AxisAlignedBB aabb, final Vec3 look, final float range) {
        final Vec3 position = PlayerUtils.playerEyePosVec();
        if (position.squareDistanceTo(look) > range * range) {
            return null;
        }
        return aabb.calculateIntercept(position, look);
    }

    public static MovingObjectPosition calculateInterceptAABB(final AxisAlignedBB aabb, final float range) {
        final Vec3 vec3 = PlayerUtils.playerEyePosVec();
        final Vec3 vec4 = PlayerUtils.playerLookVec();
        return aabb.calculateIntercept(vec3, vec3.addVector(vec4.xCoord * range, vec4.yCoord * range, vec4.zCoord * range));
    }

    public static MovingObjectPosition calculateInterceptAABB(final AxisAlignedBB aabb) {
        final Vec3 vec3 = PlayerUtils.playerEyePosVec();
        final Vec3 vec4 = PlayerUtils.playerLookVec();
        return aabb.calculateIntercept(vec3, vec3.addVector(vec4.xCoord * 100.0, vec4.yCoord * 100.0, vec4.zCoord * 100.0));
    }

    public static EnumFacing calculateEnumfacingLook(final AxisAlignedBB aabb, final Vec3 look) {
        final MovingObjectPosition position = calculateInterceptAABBLook(aabb, look);
        return (position != null) ? position.sideHit : null;
    }

    public static EnumFacing calculateEnumfacing(final AxisAlignedBB aabb) {
        final MovingObjectPosition position = calculateInterceptAABB(aabb);
        return (position != null) ? position.sideHit : null;
    }

    private static MovingObjectPosition fastRayTrace(final Vec3 vec31, final Vec3 vec32) {
        return fastRayTrace(vec31, vec32, true);
    }

    public static MovingObjectPosition fastRayTrace(Vec3 vec31, final Vec3 vec32, final boolean hitLiquid) {
        final int i = MathHelper.floor_double(vec32.xCoord);
        final int j = MathHelper.floor_double(vec32.yCoord);
        final int k = MathHelper.floor_double(vec32.zCoord);
        int l = MathHelper.floor_double(vec31.xCoord);
        int i2 = MathHelper.floor_double(vec31.yCoord);
        int j2 = MathHelper.floor_double(vec31.zCoord);
        BlockPos blockpos = new BlockPos(l, i2, j2);
        final IBlockState iblockstate = BlockUtils.getBlockState(blockpos);
        final Block block = iblockstate.getBlock();
        if (block.canCollideCheck(iblockstate, hitLiquid)) {
            final MovingObjectPosition movingobjectposition = block.collisionRayTrace((World)BlockUtils.getWorld(), blockpos, vec31, vec32);
            if (movingobjectposition != null) {
                return movingobjectposition;
            }
        }
        int k2 = 200;
        while (k2-- >= 0) {
            if (l == i && i2 == j && j2 == k) {
                return null;
            }
            boolean flag2 = true;
            boolean flag3 = true;
            boolean flag4 = true;
            double d0 = 999.0;
            double d2 = 999.0;
            double d3 = 999.0;
            if (i > l) {
                d0 = l + 1.0;
            }
            else if (i < l) {
                d0 = l + 0.0;
            }
            else {
                flag2 = false;
            }
            if (j > i2) {
                d2 = i2 + 1.0;
            }
            else if (j < i2) {
                d2 = i2 + 0.0;
            }
            else {
                flag3 = false;
            }
            if (k > j2) {
                d3 = j2 + 1.0;
            }
            else if (k < j2) {
                d3 = j2 + 0.0;
            }
            else {
                flag4 = false;
            }
            double d4 = 999.0;
            double d5 = 999.0;
            double d6 = 999.0;
            final double d7 = vec32.xCoord - vec31.xCoord;
            final double d8 = vec32.yCoord - vec31.yCoord;
            final double d9 = vec32.zCoord - vec31.zCoord;
            if (flag2) {
                d4 = (d0 - vec31.xCoord) / d7;
            }
            if (flag3) {
                d5 = (d2 - vec31.yCoord) / d8;
            }
            if (flag4) {
                d6 = (d3 - vec31.zCoord) / d9;
            }
            if (d4 == -0.0) {
                d4 = -1.0E-4;
            }
            if (d5 == -0.0) {
                d5 = -1.0E-4;
            }
            if (d6 == -0.0) {
                d6 = -1.0E-4;
            }
            if (d4 < d5 && d4 < d6) {
                vec31 = new Vec3(d0, vec31.yCoord + d8 * d4, vec31.zCoord + d9 * d4);
                if (i > l) {
                    l = MathHelper.floor_double(vec31.xCoord);
                }
                else {
                    l = MathHelper.floor_double(vec31.xCoord) - 1;
                }
                i2 = MathHelper.floor_double(vec31.yCoord);
                j2 = MathHelper.floor_double(vec31.zCoord);
            }
            else if (d5 < d6) {
                vec31 = new Vec3(vec31.xCoord + d7 * d5, d2, vec31.zCoord + d9 * d5);
                if (j > i2) {
                    i2 = MathHelper.floor_double(vec31.yCoord);
                }
                else {
                    i2 = MathHelper.floor_double(vec31.yCoord) - 1;
                }
                l = MathHelper.floor_double(vec31.xCoord);
                j2 = MathHelper.floor_double(vec31.zCoord);
            }
            else {
                vec31 = new Vec3(vec31.xCoord + d7 * d6, vec31.yCoord + d8 * d6, d3);
                if (k > j2) {
                    j2 = MathHelper.floor_double(vec31.zCoord);
                }
                else {
                    j2 = MathHelper.floor_double(vec31.zCoord) - 1;
                }
                l = MathHelper.floor_double(vec31.xCoord);
                i2 = MathHelper.floor_double(vec31.yCoord);
            }
            blockpos = new BlockPos(l, i2, j2);
            final IBlockState iblockstate2 = BlockUtils.getBlockState(blockpos);
            final Block block2 = iblockstate2.getBlock();
            if (!block2.canCollideCheck(iblockstate2, hitLiquid)) {
                continue;
            }
            final MovingObjectPosition movingobjectposition2 = block2.collisionRayTrace((World)BlockUtils.getWorld(), blockpos, vec31, vec32);
            if (movingobjectposition2 != null) {
                return movingobjectposition2;
            }
        }
        return null;
    }

    private static boolean rayTraceable(Vec3 vec31, final Vec3 vec32, final BlockPos goal) {
        final int i = MathHelper.floor_double(vec32.xCoord);
        final int j = MathHelper.floor_double(vec32.yCoord);
        final int k = MathHelper.floor_double(vec32.zCoord);
        int l = MathHelper.floor_double(vec31.xCoord);
        int i2 = MathHelper.floor_double(vec31.yCoord);
        int j2 = MathHelper.floor_double(vec31.zCoord);
        BlockPos blockpos = new BlockPos(l, i2, j2);
        final IBlockState iblockstate = BlockUtils.getBlockState(blockpos);
        final Block block = iblockstate.getBlock();
        if (block.canCollideCheck(iblockstate, false)) {
            final MovingObjectPosition movingobjectposition = block.collisionRayTrace((World)BlockUtils.getWorld(), blockpos, vec31, vec32);
            if (movingobjectposition != null) {
                return blockpos == goal || (l == goal.getX() && i2 == goal.getY() && j2 == goal.getZ());
            }
        }
        int k2 = 200;
        while (k2-- >= 0) {
            if (l == i && i2 == j && j2 == k) {
                return false;
            }
            boolean flag2 = true;
            boolean flag3 = true;
            boolean flag4 = true;
            double d0 = 999.0;
            double d2 = 999.0;
            double d3 = 999.0;
            if (i > l) {
                d0 = l + 1.0;
            }
            else if (i < l) {
                d0 = l + 0.0;
            }
            else {
                flag2 = false;
            }
            if (j > i2) {
                d2 = i2 + 1.0;
            }
            else if (j < i2) {
                d2 = i2 + 0.0;
            }
            else {
                flag3 = false;
            }
            if (k > j2) {
                d3 = j2 + 1.0;
            }
            else if (k < j2) {
                d3 = j2 + 0.0;
            }
            else {
                flag4 = false;
            }
            double d4 = 999.0;
            double d5 = 999.0;
            double d6 = 999.0;
            final double d7 = vec32.xCoord - vec31.xCoord;
            final double d8 = vec32.yCoord - vec31.yCoord;
            final double d9 = vec32.zCoord - vec31.zCoord;
            if (flag2) {
                d4 = (d0 - vec31.xCoord) / d7;
            }
            if (flag3) {
                d5 = (d2 - vec31.yCoord) / d8;
            }
            if (flag4) {
                d6 = (d3 - vec31.zCoord) / d9;
            }
            if (d4 == -0.0) {
                d4 = -1.0E-4;
            }
            if (d5 == -0.0) {
                d5 = -1.0E-4;
            }
            if (d6 == -0.0) {
                d6 = -1.0E-4;
            }
            EnumFacing enumfacing;
            if (d4 < d5 && d4 < d6) {
                vec31 = new Vec3(d0, vec31.yCoord + d8 * d4, vec31.zCoord + d9 * d4);
                if (i > l) {
                    enumfacing = EnumFacing.WEST;
                    l = MathHelper.floor_double(vec31.xCoord);
                }
                else {
                    enumfacing = EnumFacing.EAST;
                    l = MathHelper.floor_double(vec31.xCoord) - 1;
                }
                i2 = MathHelper.floor_double(vec31.yCoord);
                j2 = MathHelper.floor_double(vec31.zCoord);
            }
            else if (d5 < d6) {
                vec31 = new Vec3(vec31.xCoord + d7 * d5, d2, vec31.zCoord + d9 * d5);
                if (j > i2) {
                    enumfacing = EnumFacing.DOWN;
                    i2 = MathHelper.floor_double(vec31.yCoord);
                }
                else {
                    enumfacing = EnumFacing.UP;
                    i2 = MathHelper.floor_double(vec31.yCoord) - 1;
                }
                l = MathHelper.floor_double(vec31.xCoord);
                j2 = MathHelper.floor_double(vec31.zCoord);
            }
            else {
                vec31 = new Vec3(vec31.xCoord + d7 * d6, vec31.yCoord + d8 * d6, d3);
                if (k > j2) {
                    enumfacing = EnumFacing.NORTH;
                    j2 = MathHelper.floor_double(vec31.zCoord);
                }
                else {
                    enumfacing = EnumFacing.SOUTH;
                    j2 = MathHelper.floor_double(vec31.zCoord) - 1;
                }
                l = MathHelper.floor_double(vec31.xCoord);
                i2 = MathHelper.floor_double(vec31.yCoord);
            }
            blockpos = new BlockPos(l, i2, j2);
            final IBlockState iblockstate2 = BlockUtils.getBlockState(blockpos);
            final Block block2 = iblockstate2.getBlock();
            if (!block2.canCollideCheck(iblockstate2, false)) {
                continue;
            }
            final MovingObjectPosition movingobjectposition2 = block2.collisionRayTrace((World)BlockUtils.getWorld(), blockpos, vec31, vec32);
            if (movingobjectposition2 != null) {
                return (blockpos == goal || (l == goal.getX() && i2 == goal.getY() && j2 == goal.getZ())) && BlockUtils.getBlockState(blockpos.offset(enumfacing)).getBlock() == Blocks.air;
            }
        }
        return false;
    }

    private static boolean rayTraceable(final Vec3 vec31, final Vec3 vec32) {
        return rayTraceable(vec31, vec32, new BlockPos(vec32));
    }
}
