package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.feature.AbstractFeature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import cc.polyfrost.oneconfig.utils.Multithreading;

public class FastBreak extends AbstractFeature {

    // Singleton instance
    private static final FastBreak instance = new FastBreak();

    // Public access to the singleton instance
    public static FastBreak getInstance() {
        return instance;
    }

    @Override
    public String getName() {
        return "FastBreak";
    }

    public void breakBlockInstantly(BlockPos blockPos) {
        EntityPlayer player = mc.thePlayer;
        if (player != null && player.isEntityAlive() && !player.capabilities.isCreativeMode) {
            Multithreading.runAsync(() -> {
                for (int i = 0; i < 5; i++) {
                    sendBreakPackets(blockPos);
                }
            });
        }
    }

    private void sendBreakPackets(BlockPos blockPos) {
        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(
                C07PacketPlayerDigging.Action.START_DESTROY_BLOCK,
                blockPos,
                EnumFacing.UP
        ));
        mc.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(
                C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                blockPos,
                EnumFacing.UP
        ));
    }

    public void breakBlocksInParallel(Iterable<BlockPos> blockPositions) {
        for (BlockPos blockPos : blockPositions) {
            Multithreading.runAsync(() -> {
                for (int i = 0; i < 10; i++) {
                    sendBreakPackets(blockPos);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
