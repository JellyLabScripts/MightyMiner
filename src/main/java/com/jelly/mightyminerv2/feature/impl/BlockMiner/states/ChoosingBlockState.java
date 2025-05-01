package com.jelly.mightyminerv2.feature.impl.BlockMiner.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.util.BlockUtil;
import com.jelly.mightyminerv2.util.helper.Clock;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

import java.util.List;

/**
 * ChoosingBlockState
 * 
 * State responsible for finding the next block to mine.
 * Uses priority settings to determine the best block to target.
 * Includes wait logic if no blocks are immediately available.
 */
public class ChoosingBlockState implements BlockMinerState {
    private final Clock timer = new Clock();

    @Override
    public void onStart(BlockMiner blockMiner) {
        log("Entering Choosing Block State");
        timer.reset();
    }

    @Override
    public BlockMinerState onTick(BlockMiner blockMiner) {
        // Try to find mineable blocks around the player based on priorities
        List<BlockPos> blocks = BlockUtil.findMineableBlocksFromAccessiblePositions(
                blockMiner.getBlockPriority(),
                blockMiner.getTargetBlockPos(),
                blockMiner.getMiningSpeed()
        );

        // If strafe is enabled and no blocks found, try alternative search method
        if (MightyMinerConfig.strafe && blocks.isEmpty()) {
            blocks = BlockUtil.findMineableBlocksAroundHead(
                    blockMiner.getBlockPriority(),
                    blockMiner.getTargetBlockPos(),
                    blockMiner.getMiningSpeed()
            );
        }

        // Handle case where no blocks are found
        if (blocks.isEmpty()) {
            if (!timer.isScheduled()) {
                log("Scheduled a 2-second timer to see if blocks spawn or not");
                timer.schedule(blockMiner.getWait_threshold());
            }

            // If the timer has ended and still no blocks, stop mining
            if (timer.isScheduled() && timer.passed()) {
                logError("Cannot find enough blocks to mine.");
                blockMiner.stop();
                blockMiner.setError(BlockMiner.BlockMinerError.NOT_ENOUGH_BLOCKS);
                return null;
            }

            // Wait for the timer to expire
            return this;
        }

        // Found blocks - select the best one (first in list) and transition to breaking
        blockMiner.setTargetBlockPos(blocks.get(0));
        blockMiner.setTargetBlockType(Minecraft.getMinecraft().theWorld.getBlockState(blocks.get(0)).getBlock());
        return new BreakingState();
    }

    @Override
    public void onEnd(BlockMiner blockMiner) {
        log("Exiting Choosing Block State");
    }
}
