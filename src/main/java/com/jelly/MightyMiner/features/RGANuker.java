package com.jelly.MightyMiner.features;

import com.jelly.MightyMiner.MightyMiner;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/* ALL CREDIT GOES TO RGA*/
public class RGANuker {
    private ArrayList<BlockPos> broken = new ArrayList<>();
    private static int currentDamage;
    private static BlockPos closestStone;
    private static int ticks = 0;
    private static BlockPos gemstone;
    private static BlockPos lastGem;

    public static boolean enabled;


    List<Block> whiteList = new ArrayList<Block>(){
        {
            add(Blocks.coal_ore);
            add(Blocks.iron_ore);
            add(Blocks.gold_ore);
            add(Blocks.emerald_ore);
            add(Blocks.lapis_ore);
            add(Blocks.redstone_ore);
            add(Blocks.lit_redstone_ore);
            add(Blocks.diamond_ore);
            add(Blocks.netherrack);
        }
    };

    Minecraft mc = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {

        if(mc.thePlayer == null || mc.theWorld == null) return;
        if(!enabled) return;

        ticks++;
        if (MightyMiner.config.powAuraType == 0) {
            if (broken.size() > 10) {
                broken.clear();
            }
        } else {
            if (broken.size() > 6) {
                broken.clear();
            }
        }

        if (ticks > 30) {
            broken.clear();
            ticks = 0;
        }
        closestStone = closestStone();
        if (currentDamage > 200) {
            currentDamage = 0;
        }
        if (gemstone != null && mc.thePlayer != null) {
            if (lastGem != null && !lastGem.equals(gemstone)) {
                currentDamage = 0;
            }
            lastGem = gemstone;
            if (currentDamage == 0) {
                mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, gemstone, EnumFacing.DOWN));
            }
            swingItem();
            currentDamage++;
        }
        if (closestStone != null && gemstone == null) {
            currentDamage = 0;
            MovingObjectPosition fake = mc.objectMouseOver;
            fake.hitVec = new Vec3(closestStone);
            EnumFacing enumFacing = fake.sideHit;
            if (enumFacing != null && mc.thePlayer != null) {
                mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, closestStone, enumFacing));
            }
            swingItem();
            broken.add(closestStone);
        }

    }

    private BlockPos closestStone() {
        if (mc.theWorld == null) return null;
        if (mc.thePlayer == null) return null;
        int r = 4;
        BlockPos playerPos = mc.thePlayer.getPosition();
        playerPos.add(0, 1, 0);
        Vec3 playerVec = mc.thePlayer.getPositionVector();
        Vec3i vec3i = new Vec3i(r, 1 + MightyMiner.config.powAuraHeight, r);
        Vec3i vec3i2 = new Vec3i(r, 0, r);
        ArrayList<Vec3> stones = new ArrayList<>();
        ArrayList<Vec3> gemstones = new ArrayList<>();

        for (BlockPos blockPos : BlockPos.getAllInBox(playerPos.add(vec3i), playerPos.subtract(vec3i2))) {
            IBlockState blockState = mc.theWorld.getBlockState(blockPos);
            if (MightyMiner.config.powAuraType == 0) {
                if (blockState.getBlock() == Blocks.stone && !broken.contains(blockPos)) {
                    stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                }
                if (whiteList.contains(blockState.getBlock()) && !broken.contains(blockPos)) {
                    stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                }

            }
            if (MightyMiner.config.powAuraType == 1) {
                EnumFacing dir = mc.thePlayer.getHorizontalFacing();
                int x = (int) Math.floor(mc.thePlayer.posX);
                int z = (int) Math.floor(mc.thePlayer.posZ);
                boolean flag = false;
                switch (dir) {
                    case NORTH:
                        flag = blockPos.getZ() <= z && blockPos.getX() == x;
                        break;
                    case SOUTH:
                        flag = blockPos.getZ() >= z && blockPos.getX() == x;
                        break;
                    case WEST:
                        flag = blockPos.getX() <= x && blockPos.getZ() == z;
                        break;
                    case EAST:
                        flag = blockPos.getX() >= x && blockPos.getZ() == z;
                        break;
                }
                if (flag) {
                    if (isSlow(blockState)) {
                        gemstones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                    } else if (blockState.getBlock() == Blocks.stone && !broken.contains(blockPos)) {
                        stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                    }


                    if (whiteList.contains(blockState.getBlock()) && !broken.contains(blockPos)) {
                        stones.add(new Vec3(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5));
                    }
                }

            }
        }

        Vec3 closest = null;
        Vec3 closestgem = null;

        if (!stones.isEmpty()) {
            stones.sort(Comparator.comparingDouble(b -> b.distanceTo(playerVec)));
            closest = stones.get(0);
        }

        if(!gemstones.isEmpty()) {
            gemstones.sort(Comparator.comparingDouble(b -> b.distanceTo(playerVec)));
            closestgem = gemstones.get(0);
        }

        if (closestgem != null) {
            gemstone = new BlockPos(closestgem.xCoord, closestgem.yCoord, closestgem.zCoord);
        } else {
            gemstone = null;
        }
        if (closest != null && stones.get(0).distanceTo(playerVec) < 5) {
            return new BlockPos(closest.xCoord, closest.yCoord, closest.zCoord);
        }
        return null;
    }

    private boolean isSlow(IBlockState blockState) {
        return (blockState.getBlock() == Blocks.prismarine || blockState.getBlock() == Blocks.wool || blockState.getBlock() == Blocks.stained_hardened_clay
            || blockState.getBlock() == Blocks.gold_block ||
                (blockState.getBlock() == Blocks.stone && blockState.getValue(BlockStone.VARIANT) == BlockStone.EnumType.DIORITE_SMOOTH) ||
                blockState.getBlock() == Blocks.stained_glass_pane || blockState.getBlock() == Blocks.stained_glass);
    }


    public static void swingItem() {
        MovingObjectPosition movingObjectPosition = Minecraft.getMinecraft().objectMouseOver;
        if (movingObjectPosition != null && movingObjectPosition.entityHit == null) {
            Minecraft.getMinecraft().thePlayer.swingItem();
        }
    }



}
