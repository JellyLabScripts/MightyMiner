package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.feature.AbstractFeature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import cc.polyfrost.oneconfig.utils.Multithreading;

import java.lang.reflect.Field;
import java.util.Random;

public class FastBreak extends AbstractFeature {

    private static final FastBreak instance = new FastBreak();

    private final Random random = new Random();
    private boolean legitMode = false;
    private double activationChance = 1.0;
    private BlockPos lastBlockPos;
    private boolean fastBreakBlock;
    private Field blockHitDelayField;

    public static FastBreak getInstance() {
        return instance;
    }

    @Override
    public String getName() {
        return "FastBreak";
    }

    public void setLegitMode(boolean legitMode) {
        this.legitMode = legitMode;
    }

    public void setActivationChance(double activationChance) {
        this.activationChance = activationChance;
    }

    public BlockPos getLastBlockPos() {
        return lastBlockPos;
    }


    protected void onEnable() {
        try {
            if (blockHitDelayField == null) {
                blockHitDelayField = mc.playerController.getClass().getDeclaredField("blockHitDelay");
                blockHitDelayField.setAccessible(true);
            }
            setBlockHitDelay(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        lastBlockPos = null;
    }

    protected void onDisable() {
        lastBlockPos = null;
    }

    public void onUpdate() {
        if (!legitMode) {
            setBlockHitDelay(0);
        }
    }

    public void onBlockBreakingProgress(BlockPos blockPos, EnumFacing direction) {
        if (legitMode) {
            return;
        }

        if (!blockPos.equals(lastBlockPos)) {
            lastBlockPos = blockPos;
            fastBreakBlock = random.nextDouble() <= activationChance;
        }

        if (fastBreakBlock && !isUnbreakable(blockPos)) {
            sendBreakPackets(blockPos, direction);
        }
    }

    private void sendBreakPackets(BlockPos blockPos, EnumFacing direction) {
        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(
                Action.START_DESTROY_BLOCK, blockPos, direction
        ));
        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(
                Action.STOP_DESTROY_BLOCK, blockPos, direction
        ));
    }

    private boolean isUnbreakable(BlockPos blockPos) {
        return mc.theWorld.getBlockState(blockPos).getBlock().getBlockHardness(mc.theWorld, blockPos) == -1.0F;
    }

    private void setBlockHitDelay(int delay) {
        try {
            if (blockHitDelayField != null) {
                blockHitDelayField.setInt(mc.playerController, delay);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
