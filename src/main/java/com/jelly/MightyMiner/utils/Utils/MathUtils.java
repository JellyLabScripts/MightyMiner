package com.jelly.MightyMiner.utils.Utils;

import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import net.minecraft.block.BlockSlab;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MathUtils {
    public static final Random random = new Random();

    public static float randomFloat() {
        return (System.currentTimeMillis() % 2L == 0L) ? random.nextFloat() : (-random.nextFloat());
    }

    public static int randomNum(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }


    public static double getDistanceBetweenTwoBlock(BlockPos b1, BlockPos b2){
        return Math.sqrt((b1.getX() - b2.getX()) * (b1.getX() - b2.getX())
                + (b1.getY() - b2.getY()) * (b1.getY() - b2.getY())
                + (b1.getZ() - b2.getZ()) * (b1.getZ() - b2.getZ()));
    }
    public static double getDistanceBetweenTwoPoints(double x1, double y1, double z1, double x2, double y2, double z2){
        return Math.sqrt(square(x1 - x2) + square(y1 - y2) + square(z1 - z2));
    }
    public static double getDistanceBetweenTwoPoints(double xLength, double yLength){
        return Math.sqrt(square(xLength) + square(yLength));
    }
    public static int getBlockDistanceBetweenTwoBlock(BlockPos b1, BlockPos b2){
        return Math.abs(b1.getX() - b2.getX())
                + Math.abs((b1.getY() - b2.getY()))
                + Math.abs((b1.getZ() - b2.getZ()));
    }
    public static double getHeuristicCostBetweenTwoBlock(BlockPos b1, BlockPos b2){
        return  (Math.sqrt(
                square(b1.getX() - b2.getX()) * 0.5d
                + square( b1.getY() - b2.getY()) * (BlockUtils.getBlockCached(b1) instanceof BlockSlab ? 0.5 : 2) * (BlockUtils.getBlockCached(b2) instanceof BlockSlab ? 0.5 : 2)
                + square(b1.getZ() - b2.getZ()) * 0.5d));
    }

    public static double square(double d){
        return d * d;
    }

    public static Vec3 getVectorForRotation(float pitch, float yaw)
    {
        float f = MathHelper.cos((float) (-yaw * Math.PI/180f - Math.PI));
        float f1 = MathHelper.sin((float) (-yaw * Math.PI/180f - Math.PI));
        float f2 = -MathHelper.cos((float) (-pitch * Math.PI/180f));
        float f3 = MathHelper.sin((float) (-pitch * Math.PI/180f));
        return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
    }


}
