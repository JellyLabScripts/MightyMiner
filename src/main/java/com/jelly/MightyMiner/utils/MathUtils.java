package com.jelly.MightyMiner.utils;

import net.minecraft.block.BlockSlab;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class MathUtils {


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
        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
    }







}
