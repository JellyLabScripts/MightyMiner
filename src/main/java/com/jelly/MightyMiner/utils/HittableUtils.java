package com.jelly.MightyMiner.utils;

import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.Utils.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

import javax.swing.text.html.parser.Entity;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class HittableUtils {
    private static Minecraft mc = Minecraft.getMinecraft();

    public static Vec3 getHitVec(BlockPos blockPos, float accuracy) {
        double minX = BlockUtils.getBlock(blockPos).getBlockBoundsMinX();
        double minY = BlockUtils.getBlock(blockPos).getBlockBoundsMinX();
        double minZ = BlockUtils.getBlock(blockPos).getBlockBoundsMinY();
        double maxX = BlockUtils.getBlock(blockPos).getBlockBoundsMaxX();
        double maxY = BlockUtils.getBlock(blockPos).getBlockBoundsMaxY();
        double maxZ = BlockUtils.getBlock(blockPos).getBlockBoundsMaxZ();


        for (double x = minX * accuracy; x < maxX * accuracy; x++) {
            for (double y = minY * accuracy; y < maxY * accuracy; y++) {
                for (double z = minZ * accuracy; z < maxZ * accuracy; z++) {
                    if (VectorUtils.isRayTraceableLook(new Vec3(x/accuracy, y/accuracy, z/accuracy).add(new Vec3(blockPos)), blockPos, 61f)) {
                        return new Vec3(x/accuracy, y/accuracy, z/accuracy).add(new Vec3(blockPos));
                    }
                }
            }
        }
        return null;
    }

    public static ArrayList<Vec3> getAllHitVec(BlockPos blockPos, float accuracy) {
        double minX = BlockUtils.getBlock(blockPos).getBlockBoundsMinX();
        double minY = BlockUtils.getBlock(blockPos).getBlockBoundsMinX();
        double minZ = BlockUtils.getBlock(blockPos).getBlockBoundsMinY();
        double maxX = BlockUtils.getBlock(blockPos).getBlockBoundsMaxX();
        double maxY = BlockUtils.getBlock(blockPos).getBlockBoundsMaxY();
        double maxZ = BlockUtils.getBlock(blockPos).getBlockBoundsMaxZ();

        ArrayList<Vec3> hitVec = new ArrayList<>();

        for (double x = minX * accuracy; x < maxX * accuracy; x++) {
            for (double y = minY * accuracy; y < maxY * accuracy; y++) {
                for (double z = minZ * accuracy; z < maxZ * accuracy; z++) {
                    if (VectorUtils.isRayTraceableLook(new Vec3(x/accuracy, y/accuracy, z/accuracy).add(new Vec3(blockPos)), blockPos, 61f)) {
                        hitVec.add(new Vec3(x/accuracy, y/accuracy, z/accuracy).add(new Vec3(blockPos)));
                    }
                }
            }
        }
        return hitVec;
    }

    public static Vec3 getRandomHitVec(BlockPos blockPos, float accuracy) {
        ArrayList<Vec3> hitVec = getAllHitVec(blockPos, accuracy);
        if (hitVec.size() > 1) {
            int randInt = MathUtils.randomNum(0, hitVec.size());
            return hitVec.get(randInt);
        } else if (hitVec.size() == 1) {
            return hitVec.get(0);
        } else {
            return null;
        }
    }

    public static boolean isHittable(BlockPos blockPos, float accuracy) {
        double minX = BlockUtils.getBlock(blockPos).getBlockBoundsMinX();
        double minY = BlockUtils.getBlock(blockPos).getBlockBoundsMinX();
        double minZ = BlockUtils.getBlock(blockPos).getBlockBoundsMinY();
        double maxX = BlockUtils.getBlock(blockPos).getBlockBoundsMaxX();
        double maxY = BlockUtils.getBlock(blockPos).getBlockBoundsMaxY();
        double maxZ = BlockUtils.getBlock(blockPos).getBlockBoundsMaxZ();


        for (double x = minX * accuracy; x < maxX * accuracy; x++) {
            for (double y = minY * accuracy; y < maxY * accuracy; y++) {
                for (double z = minZ * accuracy; z < maxZ * accuracy; z++) {
                    if (VectorUtils.isRayTraceableLook(new Vec3(x/accuracy, y/accuracy, z/accuracy).add(new Vec3(blockPos)), blockPos, 61f)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static ArrayList<BlockPos> rayTrace360(float range, float accuracy) {
        long starTime = System.currentTimeMillis();
        ArrayList<BlockPos> blockPosArrayList = new ArrayList<>();
        for (float yaw = 0; yaw < 360; yaw++) {
            for (float pitch = 0; pitch < 90; pitch++) {
                Vec3 vec3 = PlayerUtils.playerEyePosVec();
                Vec3 vec4 = VectorUtils.getVectorForRotation(pitch - 60, yaw);
                MovingObjectPosition ray = VectorUtils.fastRayTrace(vec3, vec3.addVector(vec4.xCoord * range, vec4.yCoord * range, vec4.zCoord * range));
                if (ray != null && !blockPosArrayList.contains(ray.getBlockPos())) {
                    blockPosArrayList.add(ray.getBlockPos());
                }
            }
        }
        LogUtils.debugLog(System.currentTimeMillis() - starTime + "");
        return blockPosArrayList;
    }

    public static ArrayList<BlockPos> rayTrace360(Vec3 vec3, float range, float accuracy) {
        long startTime = System.currentTimeMillis();
        ArrayList<BlockPos> blockPosArrayList = new ArrayList<>();
        for (float yaw = 0; yaw < 360; yaw++) {
            for (float pitch = 0; pitch < 180; pitch++) {
                Vec3 vec4 = VectorUtils.getVectorForRotation(pitch - 90, yaw);
                MovingObjectPosition ray = VectorUtils.fastRayTrace(vec3, vec3.addVector(vec4.xCoord * range, vec4.yCoord * range, vec4.zCoord * range), false);
                if (ray != null &&  !blockPosArrayList.contains(ray.getBlockPos())) {
                    blockPosArrayList.add(ray.getBlockPos());
                }
            }
        }
        //LogUtils.debugLog(System.currentTimeMillis() - startTime + "");
        return blockPosArrayList;
    }
}
