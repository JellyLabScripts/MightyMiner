package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.util.PlayerUtil;
import com.jelly.mightyminerv2.util.RenderUtil;
import cc.polyfrost.oneconfig.utils.Multithreading;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Nuker extends AbstractFeature {

    private static final Nuker instance = new Nuker();
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static Nuker getInstance() {
        return instance;
    }

    private boolean enabled = false;
    private final int radius = 3;
    private boolean wasKeyPressed = false;
    private List<BlockPos> currentBlocksToBreak = new ArrayList<>();
    private final AtomicBoolean isBreakingBlocks = new AtomicBoolean(false);

    @Override
    public String getName() {
        return "Nuker";
    }

    public void toggle() {
        this.enabled = !this.enabled;
        note("Nuker " + (enabled ? "enabled" : "disabled"));
    }

    @SubscribeEvent
    protected void onTick(ClientTickEvent event) {
        if (MightyMinerConfig.nuker_toggle) {
            if (MightyMinerConfig.nuker_keyBind.isActive()) {
                if (!wasKeyPressed) {
                    toggle();
                    wasKeyPressed = true;
                }
            } else {
                wasKeyPressed = false;
            }

            if (!this.enabled || isBreakingBlocks.get()) {
                return;
            }

            Vec3 playerPosition = PlayerUtil.getPlayerEyePos();
            int playerX = (int) playerPosition.xCoord;
            int playerY = (int) playerPosition.yCoord;
            int playerZ = (int) playerPosition.zCoord;

            currentBlocksToBreak.clear();
            for (int x = playerX - radius; x <= playerX + radius; x++) {
                for (int y = playerY - radius; y <= playerY + radius; y++) {
                    for (int z = playerZ - radius; z <= playerZ + radius; z++) {
                        BlockPos blockPos = new BlockPos(x, y, z);
                        Block block = mc.theWorld.getBlockState(blockPos).getBlock();
                        if (block.getMaterial().isSolid() && block.getBlockHardness(mc.theWorld, blockPos) >= 0) {
                            currentBlocksToBreak.add(blockPos);
                        }
                    }
                }
            }

            breakBlocksAsync();
        }
    }

    private void breakBlocksAsync() {
        isBreakingBlocks.set(true);
        Multithreading.runAsync(() -> {
            for (BlockPos pos : currentBlocksToBreak) {
                breakBlock(pos);
            }
            isBreakingBlocks.set(false);
        });
    }

    private void breakBlock(BlockPos pos) {
        if (mc.theWorld.getBlockState(pos).getBlock().getMaterial().isSolid()) {
            mc.thePlayer.swingItem();
            mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(
                    C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, pos, EnumFacing.UP));
            mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(
                    C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, EnumFacing.UP));
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!this.enabled) {
            return;
        }

        for (BlockPos pos : currentBlocksToBreak) {
            RenderUtil.outlineBlock(pos, new Color(255, 0, 0, 128)); // Highlight blocks being mined
        }

        if (!currentBlocksToBreak.isEmpty()) {
            BlockPos firstBlock = currentBlocksToBreak.get(0);
            RenderUtil.outlineBlock(firstBlock, new Color(0, 255, 0, 128)); // Highlight first block
            RenderUtil.drawText("Mining: " + mc.theWorld.getBlockState(firstBlock).getBlock().getLocalizedName(),
                    firstBlock.getX() + 0.5,
                    firstBlock.getY() + 1.2,
                    firstBlock.getZ() + 0.5,
                    0.02f); // Draw block name
        }
    }

    public void onPlayerBreakBlock(BlockPos blockPos) {
        if (!this.enabled) {
            return;
        }

        List<BlockPos> nearbyBlocks = new ArrayList<>();
        for (int x = blockPos.getX() - radius; x <= blockPos.getX() + radius; x++) {
            for (int y = blockPos.getY() - radius; y <= blockPos.getY() + radius; y++) {
                for (int z = blockPos.getZ() - radius; z <= blockPos.getZ() + radius; z++) {
                    BlockPos nearbyBlockPos = new BlockPos(x, y, z);
                    Block block = mc.theWorld.getBlockState(nearbyBlockPos).getBlock();
                    if (block.getMaterial().isSolid() && block.getBlockHardness(mc.theWorld, nearbyBlockPos) >= 0) {
                        nearbyBlocks.add(nearbyBlockPos);
                    }
                }
            }
        }
        breakBlocksAsync(nearbyBlocks);
    }

    private void breakBlocksAsync(List<BlockPos> blocksToBreak) {
        Multithreading.runAsync(() -> {
            for (BlockPos pos : blocksToBreak) {
                breakBlock(pos);
            }
        });
    }
}
