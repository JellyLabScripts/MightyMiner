package com.jelly.MightyMinerV2.Util;

import com.jelly.MightyMinerV2.Util.helper.Angle;
import com.jelly.MightyMinerV2.Util.helper.heap.MinHeap;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.*;


public class BlockUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

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

    public static BlockPos getBlockLookingAt(float distance) {
        Vec3 playerEye = mc.thePlayer.getPositionEyes(1);
        Vec3 lookVec = mc.thePlayer.getLookVec();
        Vec3 endPos = playerEye.addVector(lookVec.xCoord * distance, lookVec.yCoord * distance, lookVec.zCoord * distance);
        MovingObjectPosition result = mc.theWorld.rayTraceBlocks(playerEye, endPos);
        if (result != null && result.typeOfHit == MovingObjectType.BLOCK) {
            return result.getBlockPos();
        }
        return null;
    }

    // Priority - [GrayWool, Prismarine, BlueWool, Titanium]
    // The higher the priority, the higher the chance of it getting picked
    public static List<BlockPos> getValidMithrilPositions(int[] priority) {
        final MinHeap blocks = new MinHeap(500);
        final BlockPos playerHeadPos = PlayerUtil.getBlockStandingOn().add(0, 2, 0);
        final Vec3 posEyes = mc.thePlayer.getPositionEyes(1);
        for (int x = -4; x < 5; x++) {
            for (int z = -4; z < 5; z++) {
                for (int y = -3; y < 5; y++) {
                    final BlockPos pos = playerHeadPos.add(x, y, z);
                    if (!hasVisibleSide(pos)) continue;

                    final double hardness = getBlockStrength(mc.theWorld.getBlockState(pos));
                    if (hardness < 500 || hardness > 2000 || hardness == 600) continue;

                    final double distance = posEyes.distanceTo(new Vec3(pos));
                    if (distance > 4) continue;

                    final Angle change = AngleUtil.getNeededChange(AngleUtil.getPlayerAngle(), AngleUtil.getRotation(pos));
                    final double angleChange = Math.sqrt(change.getYaw() * change.getYaw() + change.getPitch() * change.getPitch());

                    // Todo: Cost requires more testing.
                    blocks.add(pos, angleChange * 0.5 + hardness / (50.0 * priority[getPriorityIndex((int) hardness)]) + distance);
                }
            }
        }
        return blocks.getBlocks();
    }

    private static int getPriorityIndex(final int hardness) {
        switch (hardness) {
            case 500:
                return 0;
            case 800:
                return 1;
            case 1500:
                return 2;
            default:
                return 3;
        }
    }

    public static int getBlockStrength(IBlockState blockState) {
        if (blockState == null) {
            return 30;
        }

        Block block = blockState.getBlock();

        if (block == Blocks.diamond_block) {
            return 50;
        } else if (block == Blocks.gold_block) {
            return 600;
        } else if (block == Blocks.sponge) {
            return 500;
        } else if (block == Blocks.stone) {
            switch (blockState.getValue(BlockStone.VARIANT)) {
                case STONE:
                    return 50;
                case DIORITE_SMOOTH:
                    return 2000;
                default:
                    break;
            }
        } else if (block == Blocks.wool) {
            switch (blockState.getValue(BlockColored.COLOR)) {
                case GRAY:
                    return 500;
                case LIGHT_BLUE:
                    return 1500;
                default:
                    break;
            }
        } else if (block == Blocks.stained_hardened_clay) {
            if (blockState.getValue(BlockColored.COLOR) == EnumDyeColor.CYAN) {
                return 500;
            }
        } else if (block == Blocks.prismarine) {
            switch (blockState.getValue(BlockPrismarine.VARIANT)) {
                case ROUGH:
                case DARK:
                case BRICKS:
                    return 800;
                default:
                    break;
            }
        } else if (block instanceof BlockGlass) {
            switch (blockState.getValue(BlockColored.COLOR)) {
                case RED:
                    return 2500;
                case PURPLE:
                case BLUE:
                case ORANGE:
                case LIME:
                    return 3200;
                case WHITE:
                case YELLOW:
                    return 4000;
                case MAGENTA:
                    return 5000;
//                case BLUE: - Aquamarine - Do Something about this
                case BLACK:
                case BROWN:
                case GREEN:
                    return 5200;
                default:
                    break;
            }
        } else {
            return 30;
        }

        return 5000;
    }

    public static float getMiningTime(float getBlockStrength, int miningSpeed) {
        return (getBlockStrength * 30) / miningSpeed;
    }

    public static Vec3 getSidePos(BlockPos block, EnumFacing face) {
        final float[] offset = BLOCK_SIDES.get(face);
        return new Vec3(block.getX() + offset[0], block.getY() + offset[1], block.getZ() + offset[2]);
    }

    public static boolean canSeeSide(BlockPos block, EnumFacing side) {
        return RaytracingUtil.canSeePoint(getSidePos(block, side));
    }

    public static List<EnumFacing> getAllVisibleSides(BlockPos block) {
        final List<EnumFacing> sides = new ArrayList<>();
        for (EnumFacing face : BLOCK_SIDES.keySet()) {
            if (canSeeSide(block, face)) sides.add(face);
        }
        return sides;
    }

    public static EnumFacing getClosestVisibleSide(BlockPos block) {
        if (!mc.theWorld.isBlockFullCube(block)) return null;
        final Vec3 eyePos = mc.thePlayer.getPositionEyes(1);
        double dist = Double.MAX_VALUE;
        EnumFacing face = null;
        for (EnumFacing side : BLOCK_SIDES.keySet()) {
            final double distanceToThisSide = eyePos.distanceTo(getSidePos(block, side));
            if (canSeeSide(block, side) && distanceToThisSide < dist) {
                if (side == null && face != null) continue;
                dist = distanceToThisSide;
                face = side;
            }
        }
        return face;
    }

    public static boolean hasVisibleSide(BlockPos block) {
        if (!mc.theWorld.isBlockFullCube(block)) return false;
        final Vec3 eyePos = mc.thePlayer.getPositionEyes(1);
        for (EnumFacing side : BLOCK_SIDES.keySet()) {
            if (canSeeSide(block, side)) return true;
        }
        return false;
    }
}
