package com.jelly.mightyminerv2.failsafe.impl;

import com.jelly.mightyminerv2.failsafe.AbstractFailsafe;
import com.jelly.mightyminerv2.macro.MacroManager;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class BedrockCheckFailsafe extends AbstractFailsafe {

    @Getter
    private static final BedrockCheckFailsafe instance = new BedrockCheckFailsafe();
    private static final int CHECK_RADIUS = 5;
    private static final int BEDROCK_THRESHOLD = 10;

    @Override
    public String getName() {
        return "BedrockCheckFailsafe";
    }

    @Override
    public Failsafe getFailsafeType() {
        return Failsafe.BEDROCK_CHECK;
    }

    @Override
    public int getPriority() {
        return 6;
    }


    public boolean checkForBedrock(Vec3 playerPos) {
        int bedrockCount = 0;

        for (int x = -CHECK_RADIUS; x <= CHECK_RADIUS; x++) {
            for (int y = -CHECK_RADIUS; y <= CHECK_RADIUS; y++) {
                for (int z = -CHECK_RADIUS; z <= CHECK_RADIUS; z++) {
                    BlockPos blockPos = new BlockPos(
                            playerPos.xCoord + x,
                            playerPos.yCoord + y,
                            playerPos.zCoord + z
                    );
                    Block block = mc.theWorld.getBlockState(blockPos).getBlock();

                    if (block == Blocks.bedrock) {
                        bedrockCount++;
                    }

                    if (bedrockCount >= BEDROCK_THRESHOLD) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public boolean react() {
        MacroManager.getInstance().disable();
        warn("Disabling macro due to bedrock surroundings.");
        return true;
    }

}

