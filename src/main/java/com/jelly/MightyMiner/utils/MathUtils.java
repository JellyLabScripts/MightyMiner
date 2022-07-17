package com.jelly.MightyMiner.utils;

import net.minecraft.util.BlockPos;

public class MathUtils {

    public static double getDistanceBetweenTwoBlock(BlockPos b1, BlockPos b2){
        return Math.sqrt((b1.getX() - b2.getX()) * (b1.getX() - b2.getX())
                + (b1.getY() - b2.getY()) * (b1.getY() - b2.getY())
                + (b1.getZ() - b2.getZ()) * (b1.getZ() - b2.getZ()));
    }
    public static int getBlockDistanceBetweenTwoBlock(BlockPos b1, BlockPos b2){
        return Math.abs(b1.getX() - b2.getX())
                + Math.abs((b1.getY() - b2.getY()))
                + Math.abs((b1.getZ() - b2.getZ()));
    }




}
