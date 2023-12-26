package com.jelly.MightyMiner.utils;

import com.jelly.MightyMiner.utils.BlockUtils.BlockUtils;
import com.jelly.MightyMiner.utils.Utils.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

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

    public static float getActualRotationYaw() { //f3
        return getActualRotationYaw(mc.thePlayer.rotationYaw);
    }

    public static float getActualRotationYaw(float yaw) { //f3
        return yaw > 0 ?
                (yaw % 360 > 180 ? -(180 - (yaw % 360 - 180)) : yaw % 360) :
                (-yaw % 360 > 180 ? (180 - (-yaw % 360 - 180)) : -(-yaw % 360));
    }

    public static float antiClockwiseDifference(float initialYaw360, float targetYaw360) {
        return get360RotationYaw(initialYaw360 - targetYaw360);
    }

    public static float smallestAngleDifference(float initialYaw360, float targetYaw360) {
        return Math.min(clockwiseDifference(initialYaw360, targetYaw360), antiClockwiseDifference(initialYaw360, targetYaw360));
    }


    public static boolean shouldRotateClockwise(float start, float target) {
        return clockwiseDifference(get360RotationYaw(start), target) < 180;
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

    public static float getRequiredYawSide(BlockPos blockLookingAt) {

        double deltaX = blockLookingAt.getX() - mc.thePlayer.posX + 0.5d;
        double deltaZ = blockLookingAt.getZ() - mc.thePlayer.posZ + 0.5d;

        BlockUtils.BlockSides blockSideToMine = getOptimalSide(blockLookingAt);

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

        return  getRequiredYaw(deltaX, deltaZ);
    }

    public static Tuple<Float, Float> getRotation(Vec3 vec3) {
        double diffX = vec3.xCoord - mc.thePlayer.posX;
        double diffY = vec3.yCoord - mc.thePlayer.posY - mc.thePlayer.getEyeHeight();
        double diffZ = vec3.zCoord - mc.thePlayer.posZ;
        return getRotationTo(diffX, diffY, diffZ);
    }

    public static Tuple<Float, Float> getRotation(Vec3 from, Vec3 to) {
        double diffX = from.xCoord - to.xCoord;
        double diffY = from.yCoord - to.yCoord;
        double diffZ = from.zCoord - to.zCoord;
        return getRotationTo(diffX, diffY, diffZ);
    }

    private static Tuple<Float, Float> getRotationTo(double diffX, double diffY, double diffZ) {
        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float pitch = (float) -Math.atan2(dist, diffY);
        float yaw = (float) Math.atan2(diffZ, diffX);
        pitch = (float) wrapAngleTo180((pitch * 180F / Math.PI + 90) * -1);
        yaw = (float) wrapAngleTo180((yaw * 180 / Math.PI) - 90);

        return new Tuple<>(yaw, pitch);
    }

    public static float getRequiredYaw(double deltaX, double deltaZ) {
        if(deltaX == 0 && deltaZ < 0) // special case
            return -180f;

        return  (float) (Math.atan(-deltaX / deltaZ) * 180 / Math.PI) + ((deltaX > 0 && deltaZ < 0) ? -180 : 0) + ((deltaX < 0 && deltaZ < 0) ? 180 : 0);
    }

    public static float wrapAngleTo180(double angle) {
        return (float) (angle - Math.floor(angle / 360.0f + 0.5) * 360.0);
    }

    public static boolean isDiffLowerThan(float neededChangeYaw, float neededChangePitch, float diff) {
        float actualYaw = mc.thePlayer.rotationYaw;
        float actualPitch = mc.thePlayer.rotationPitch;
        return Math.abs(actualYaw - neededChangeYaw) < diff && Math.abs(actualPitch - neededChangePitch) < diff;
    }

    public static float getAngleDifference(float actualYaw1, float actualYaw2){
        if(actualYaw1 - actualYaw2 > 180) {
            return Math.abs(actualYaw1 - 360 - actualYaw2);
        } else if(actualYaw1 - actualYaw2 < -180){
            return Math.abs(actualYaw2 - 360 - actualYaw1);
        } else return Math.abs(actualYaw1 - actualYaw2);
    }

    public static double getRotationDifference(final Vec3 vec) {
        final Pair<Float, Float> rotation = getYawAndPitch(vec, true);

        return getRotationDifference(rotation, Pair.of(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch));
    }

    public static double getRotationDifference(final Pair<Float, Float> a, final Pair<Float, Float> b) {
        return Math.hypot(getAngleDifference(a.getKey(), b.getKey()), a.getValue() - b.getValue());
    }

    public static Pair<Float, Float> getYawAndPitch(final Vec3 vec, final boolean predict) {
        final Vec3 eyesPos = new Vec3(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);

        if(predict) {
            if(mc.thePlayer.onGround) {
                eyesPos.addVector(mc.thePlayer.motionX, 0.0, mc.thePlayer.motionZ);
            } else eyesPos.addVector(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
        }

        final double diffX = vec.xCoord - eyesPos.xCoord;
        final double diffY = vec.yCoord - eyesPos.yCoord;
        final double diffZ = vec.zCoord - eyesPos.zCoord;

        return Pair.of(AngleUtils.wrapAngleTo180((double) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F),
                AngleUtils.wrapAngleTo180((double) (-Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))))
        ));
    }

    public static float getRequiredPitchSide(BlockPos blockLookingAt) {
        double deltaX = blockLookingAt.getX() - mc.thePlayer.posX + 0.5d;
        double deltaZ = blockLookingAt.getZ() - mc.thePlayer.posZ + 0.5d;
        double deltaY = (blockLookingAt.getY() + 0.5d) - (mc.thePlayer.posY + 1.62d);

        BlockUtils.BlockSides blockSideToMine = getOptimalSide(blockLookingAt);

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


        return  getRequiredPitch(deltaX, deltaY, deltaZ);
    }

    public static float getRequiredPitchCenter(BlockPos blockLookingAt) {
        double deltaX = blockLookingAt.getX() - mc.thePlayer.posX + 0.5d;
        double deltaZ = blockLookingAt.getZ() - mc.thePlayer.posZ + 0.5d;
        double deltaY = (blockLookingAt.getY() + 0.5d) - (mc.thePlayer.posY + 1.62d);
        return  getRequiredPitch(deltaX, deltaY, deltaZ);
    }
    public static float getRequiredYawCenter(BlockPos blockLookingAt) {
        double deltaX = blockLookingAt.getX() - mc.thePlayer.posX + 0.5d;
        double deltaZ = blockLookingAt.getZ() - mc.thePlayer.posZ + 0.5d;
        return  getRequiredYaw(deltaX, deltaZ);
    }
    public static float getRequiredPitch(double deltaX, double deltaY, double deltaZ) {
        double deltaDis = MathUtils.getDistanceBetweenTwoPoints(deltaX, deltaZ);
        double pitch = -(Math.atan(deltaY / deltaDis) * 180 / Math.PI);
        if( (float) pitch > 90 ||  (float) pitch < -90){
            System.out.println(pitch + " " + deltaX + " " + deltaZ);
            return 0;
        }
        return  (float) pitch;
    }




    public static float getActualYawFrom360(float yaw360) {
        float currentYaw = yaw360;
        if(mc.thePlayer.rotationYaw > yaw360){
            while (mc.thePlayer.rotationYaw - currentYaw < 180 || mc.thePlayer.rotationYaw - currentYaw > 0){
                if(Math.abs(currentYaw + 360 - mc.thePlayer.rotationYaw) < Math.abs(currentYaw - mc.thePlayer.rotationYaw))
                    currentYaw = currentYaw + 360;
                else  break;
            }
        }
        if(mc.thePlayer.rotationYaw < yaw360){
            while (currentYaw - mc.thePlayer.rotationYaw > 180 || mc.thePlayer.rotationYaw - currentYaw < 0){
                if(Math.abs(currentYaw - 360 - mc.thePlayer.rotationYaw) < Math.abs(currentYaw - mc.thePlayer.rotationYaw))
                    currentYaw = currentYaw - 360;
                else  break;
            }
        }
        return currentYaw;


    }

    public static BlockUtils.BlockSides getOptimalSide(BlockPos blockPos){

        ArrayList<BlockUtils.BlockSides> blockSidesNotCovered = BlockUtils.getAdjBlocksNotCovered(blockPos);
        BlockUtils.BlockSides blockSideToMine = BlockUtils.BlockSides.NONE;
        if(blockSidesNotCovered.size() > 0 && !lookAtCenterBlocks.contains(BlockUtils.getBlock(blockPos))) {
            double lowestCost = 9999;
            blockSideToMine = blockSidesNotCovered.get(0);


            for (BlockUtils.BlockSides blockSide : blockSidesNotCovered) {
                double tempCost = 0;

                switch (blockSide) {
                    case posX:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                mc.thePlayer.posX, mc.thePlayer.posY + 1.62d, mc.thePlayer.posZ, blockPos.getX() + 0.5d, blockPos.getY(), blockPos.getZ());
                        break;
                    case negX:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                mc.thePlayer.posX, mc.thePlayer.posY + 1.62d, mc.thePlayer.posZ, blockPos.getX() - 0.5d, blockPos.getY(), blockPos.getZ());
                        break;
                    case posZ:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                mc.thePlayer.posX, mc.thePlayer.posY + 1.62d, mc.thePlayer.posZ, blockPos.getX(), blockPos.getY(), blockPos.getZ() + 0.5d);
                        break;
                    case negZ:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                mc.thePlayer.posX, mc.thePlayer.posY + 1.62d, mc.thePlayer.posZ, blockPos.getX(), blockPos.getY(), blockPos.getZ() - 0.5d);
                        break;
                    case up:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                mc.thePlayer.posX, mc.thePlayer.posY + 1.62d, mc.thePlayer.posZ, blockPos.getX(), blockPos.getY() + 0.5d, blockPos.getZ());
                        break;
                    case down:
                        tempCost = MathUtils.getDistanceBetweenTwoPoints(
                                mc.thePlayer.posX, mc.thePlayer.posY + 1.62d, mc.thePlayer.posZ, blockPos.getX(), blockPos.getY() - 0.5d, blockPos.getZ());
                        break;
                }
                if (tempCost < lowestCost) {
                    lowestCost = tempCost;
                    blockSideToMine = blockSide;
                }
            }
        }
        return blockSideToMine;
    }

    public static int getYawRotationTime(float target_yaw, int angle_per_unit, int time_per_unit, int min){
        return (int) Math.max(min, getAngleDifference(target_yaw, getActualRotationYaw()) / angle_per_unit * time_per_unit);
    }
    public static int getPitchRotationTime(float target_pitch, int angle_per_unit, int time_per_unit, int min){
        return (int) Math.max(min, Math.abs(target_pitch - mc.thePlayer.rotationPitch) / angle_per_unit * time_per_unit);
    }

    public static BlockPos bresenham(Vec3 start, Vec3 end) {
        int x1 = MathHelper.floor_double(end.xCoord);
        int y1 = MathHelper.floor_double(end.yCoord);
        int z1 = MathHelper.floor_double(end.zCoord);
        int x0 = MathHelper.floor_double(start.xCoord);
        int y0 = MathHelper.floor_double(start.yCoord);
        int z0 = MathHelper.floor_double(start.zCoord);

        if (mc.theWorld.getBlockState(new BlockPos(x0, y0, z0)).getBlock() != Blocks.air) {
            return new BlockPos(x0, y0, z0);
        }

        int iterations = 200;

        while (iterations-- >= 0) {
            if (x0 == x1 && y0 == y1 && z0 == z1) {
                return new BlockPos(end);
            }

            boolean hasNewX = true;
            boolean hasNewY = true;
            boolean hasNewZ = true;

            double newX = 999.0;
            double newY = 999.0;
            double newZ = 999.0;

            if (x1 > x0) {
                newX = (double) x0 + 1.0;
            } else if (x1 < x0) {
                newX = (double) x0 + 0.0;
            } else {
                hasNewX = false;
            }
            if (y1 > y0) {
                newY = (double) y0 + 1.0;
            } else if (y1 < y0) {
                newY = (double) y0 + 0.0;
            } else {
                hasNewY = false;
            }
            if (z1 > z0) {
                newZ = (double) z0 + 1.0;
            } else if (z1 < z0) {
                newZ = (double) z0 + 0.0;
            } else {
                hasNewZ = false;
            }

            double stepX = 999.0;
            double stepY = 999.0;
            double stepZ = 999.0;


            double dx = end.xCoord - start.xCoord;
            double dy = end.yCoord - start.yCoord;
            double dz = end.zCoord - start.zCoord;

            if (hasNewX) {
                stepX = (newX - start.xCoord) / dx;
            }
            if (hasNewY) {
                stepY = (newY - start.yCoord) / dy;
            }
            if (hasNewZ) {
                stepZ = (newZ - start.zCoord) / dz;
            }
            if (stepX == -0.0) {
                stepX = -1.0E-4;
            }
            if (stepY == -0.0) {
                stepY = -1.0E-4;
            }
            if (stepZ == -0.0) {
                stepZ = -1.0E-4;
            }

            EnumFacing enumfacing;
            if (stepX < stepY && stepX < stepZ) {
                enumfacing = x1 > x0 ? EnumFacing.WEST : EnumFacing.EAST;
                start = new Vec3(newX, start.yCoord + dy * stepX, start.zCoord + dz * stepX);
            } else if (stepY < stepZ) {
                enumfacing = y1 > y0 ? EnumFacing.DOWN : EnumFacing.UP;
                start = new Vec3(start.xCoord + dx * stepY, newY, start.zCoord + dz * stepY);
            } else {
                enumfacing = z1 > z0 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                start = new Vec3(start.xCoord + dx * stepZ, start.yCoord + dy * stepZ, newZ);
            }
            x0 = MathHelper.floor_double(start.xCoord) - (enumfacing == EnumFacing.EAST ? 1 : 0);
            y0 = MathHelper.floor_double(start.yCoord) - (enumfacing == EnumFacing.UP ? 1 : 0);
            z0 = MathHelper.floor_double(start.zCoord) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);

            if (mc.theWorld.getBlockState(new BlockPos(x0, y0, z0)).getBlock() != Blocks.air) {
                return new BlockPos(x0, y0, z0);
            }
        }

        return null;
    }

}
