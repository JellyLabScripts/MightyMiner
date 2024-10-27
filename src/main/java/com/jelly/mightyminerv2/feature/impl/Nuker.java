package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.util.PlayerUtil;
import com.jelly.mightyminerv2.util.RenderUtil;
import cc.polyfrost.oneconfig.utils.Multithreading;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Nuker extends AbstractFeature {

    private static final Nuker instance = new Nuker();

    public static Nuker getInstance() {
        return instance;
    }

    private boolean enabled = false;
    private final int radius = 3;
    private final FastBreak fastBreak = FastBreak.getInstance();
    private boolean wasKeyPressed = false;
    private List<BlockPos> currentBlocksToBreak = new ArrayList<>();

    @Override
    public String getName() {
        return "Nuker";
    }

    public void toggle() {
        this.enabled = !this.enabled;
        note("Nuker " + (enabled ? "enabled" : "disabled"));
    }

//    @SubscribeEvent
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

            if (!this.enabled) {
                return;
            }
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

        // Run block breaking asynchronously
        Multithreading.runAsync(() -> {
            for (BlockPos pos : currentBlocksToBreak) {
                fastBreak.onBlockBreakingProgress(pos, EnumFacing.UP);
            }
        });
    }

    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (!this.enabled) {
            return;
        }

        // Highlighting blocks in the area
        for (BlockPos pos : currentBlocksToBreak) {
            RenderUtil.outlineBlock(pos, new Color(255, 0, 0, 128)); // Highlight the blocks being mined
        }

        // Highlight the last block being mined by FastBreak
        BlockPos lastMinedBlock = fastBreak.getLastBlockPos(); // Access last block from FastBreak
        if (lastMinedBlock != null) {
            RenderUtil.outlineBlock(lastMinedBlock, new Color(0, 255, 0, 128)); // Highlight it in green
            RenderUtil.drawText("Mining: " + mc.theWorld.getBlockState(lastMinedBlock).getBlock().getLocalizedName(),
                    lastMinedBlock.getX() + 0.5,
                    lastMinedBlock.getY() + 1.2,
                    lastMinedBlock.getZ() + 0.5,
                    0.02f); // Draw the name of the block being mined
        }
    }


    public void onPlayerBreakBlock(BlockPos blockPos) {
        if (!this.enabled) {
            return;
        }

        for (int x = blockPos.getX() - radius; x <= blockPos.getX() + radius; x++) {
            for (int y = blockPos.getY() - radius; y <= blockPos.getY() + radius; y++) {
                for (int z = blockPos.getZ() - radius; z <= blockPos.getZ() + radius; z++) {
                    BlockPos nearbyBlockPos = new BlockPos(x, y, z);
                    Block block = mc.theWorld.getBlockState(nearbyBlockPos).getBlock();

                    if (block.getMaterial().isSolid() && block.getBlockHardness(mc.theWorld, nearbyBlockPos) >= 0) {
                        fastBreak.onBlockBreakingProgress(nearbyBlockPos, EnumFacing.UP);
                    }
                }
            }
        }
    }
}
