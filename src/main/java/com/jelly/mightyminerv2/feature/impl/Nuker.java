package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.util.PlayerUtil;
import cc.polyfrost.oneconfig.utils.Multithreading;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;

public class Nuker extends AbstractFeature {

    private static final Nuker instance = new Nuker();

    public static Nuker getInstance() {
        return instance;
    }

    private boolean enabled = false;
    private final int radius = 3;
    private FastBreak fastBreak = FastBreak.getInstance();
    private boolean wasKeyPressed = false;

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
        // Check if the key has been pressed
        if (MightyMinerConfig.nuker_keyBind.isActive()) {
            if (!wasKeyPressed) {
                // Toggle Nuker when the key is first pressed down
                toggle();
                wasKeyPressed = true;
            }
        } else {
            // Reset the key press state when the key is released
            wasKeyPressed = false;
        }

        if (!this.enabled) {
            return; // Early return if not enabled
        }

        Vec3 playerPosition = PlayerUtil.getPlayerEyePos();
        int playerX = (int) playerPosition.xCoord;
        int playerY = (int) playerPosition.yCoord;
        int playerZ = (int) playerPosition.zCoord;

        List<BlockPos> blocksToBreak = new ArrayList<>();

        for (int x = playerX - radius; x <= playerX + radius; x++) {
            for (int y = playerY - radius; y <= playerY + radius; y++) {
                for (int z = playerZ - radius; z <= playerZ + radius; z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);
                    Block block = mc.theWorld.getBlockState(blockPos).getBlock();

                    if (block.getMaterial().isSolid() && block.getBlockHardness(mc.theWorld, blockPos) >= 0) {
                        blocksToBreak.add(blockPos);
                    }
                }
            }
        }

        Multithreading.runAsync(() -> {
            for (BlockPos pos : blocksToBreak) {
                fastBreak.breakBlockInstantly(pos);
                /*try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
            }
        });
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
                        fastBreak.breakBlockInstantly(nearbyBlockPos);
                    }
                }
            }
        }
    }
}
