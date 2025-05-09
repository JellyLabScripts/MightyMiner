package com.jelly.mightyminerv2.feature.impl.BlockMiner.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.util.*;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.Target;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * BreakingState
 * 
 * State responsible for breaking the selected target block.
 * Handles player rotation, movement, and mining mechanics.
 * Will attempt to move towards the block if too far away.
 */
public class BreakingState implements BlockMinerState{

    private static final double MIN_WALK_DISTANCE = 0.2;  // Minimum distance to trigger walking
    private static final double MAX_MINE_DISTANCE = 4;    // Maximum mining distance for player
    private static final int FAILSAFE_TICKS = 40;         // Safety mechanism if we've been trying to break for too long

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Random random = new Random();

    private int breakAttemptTime;  // Tracks how long we've been trying to break the block (in ticks)
    private int miningTime;        // Expected time to break the block (in ticks)
    private Vec3 targetPoint;      // The specific point on the block to target for mining
    private Vec3 walkingDestinationBlock;  // Target position for walking if needed
    private boolean isWalking;     // Whether the player is currently walking toward the block

    @Override
    public void onStart(BlockMiner miner) {
        log("Entering Breaking State");
        breakAttemptTime = 0;
        isWalking = false;

        miningTime = BlockUtil.getMiningTime(
            Block.getStateId(Minecraft.getMinecraft().theWorld.getBlockState(miner.getTargetBlockPos())),
            miner.getMiningSpeed()
        );

        // Setup rotation to look at the block
        initializeRotation(miner);
    }

    @Override
    public BlockMinerState onTick(BlockMiner miner) {
        // Handle key presses for mining
        handleKeybinds();

        // Handle walking toward block if needed
        if (isWalking) {
            handleWalking();
        }

        // Safety mechanism: if we've been trying to break for too long, reset
        if (++this.breakAttemptTime > this.miningTime + FAILSAFE_TICKS) {
            logError("Stuck while mining, return to starting state");
            return new StartingState();
        }

        // After breaking a block, restart the whole cycle again
        Block detectedBlockType = mc.theWorld.getBlockState(miner.getTargetBlockPos()).getBlock();
        if (!detectedBlockType.equals(miner.getTargetBlockType())) {
            return new StartingState();
        }

        return this;
    }

    @Override
    public void onEnd(BlockMiner miner) {
        RotationHandler.getInstance().stop();
        log("Exiting Breaking State");
    }

    /**
     * Handles key bindings for mining.
     * Sets attack key to continuously mine and manages sneak state.
     */
    private void handleKeybinds() {
        // Hold left-click to break blocks
        KeyBindUtil.setKeyBindState(Minecraft.getMinecraft().gameSettings.keyBindAttack, true);

        if (!isWalking) {
            KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, MightyMinerConfig.sneakWhileMining);
            KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, false);
        }
    }

    /**
     * Handles walking mechanics when player needs to move toward target block.
     * Uses strafing utility to navigate toward the block.
     */
    private void handleWalking() {
        // Calculate distance to walking destination and mining point
        double walkingDistance = 999;
        if (walkingDestinationBlock != null)
            walkingDistance = Math.hypot(this.walkingDestinationBlock.xCoord - mc.thePlayer.posX,
                    this.walkingDestinationBlock.zCoord - mc.thePlayer.posZ);

        double miningDistance = PlayerUtil.getPlayerEyePos().distanceTo(this.targetPoint);

        // Move toward target if too far away
        if (walkingDistance > MIN_WALK_DISTANCE && miningDistance > MAX_MINE_DISTANCE) {
            KeyBindUtil.holdThese(mc.gameSettings.keyBindForward, mc.gameSettings.keyBindSneak);
        } else {
            // Close enough, stop walking
            isWalking = false;
            this.walkingDestinationBlock = null;
        }
    }

    /**
     * Sets up rotation to look at the target block.
     * Also determines if the player needs to walk toward the block.
     * 
     * @param miner The BlockMiner instance
     */
    private void initializeRotation(BlockMiner miner) {
        // Get best points to look at on the block
        List<Vec3> points = BlockUtil.bestPointsOnBestSide(miner.getTargetBlockPos());

        // Handle case where no valid points are found
        if (points.isEmpty()) {
            logError("Cannot find points to look at. Returning to STARTING state.");
            miner.setError(BlockMiner.BlockMinerError.NO_POINTS_FOUND);
            miner.stop();
            return;
        }

        // Select first point as target
        this.targetPoint = points.get(0);

        // Configure rotation to look at target
        RotationHandler.getInstance().stop();
        RotationHandler.getInstance().queueRotation(
                new RotationConfiguration(
                        new Target(targetPoint),
                        MightyMinerConfig.getRandomRotationTime(),
                        null
                )
        );

        // Sometimes randomly choose a different point on the block (for variety)
        if (random.nextBoolean()) {
            int halfwayMark = points.size() / 2;
            this.targetPoint = points.get(random.nextInt(halfwayMark) + halfwayMark - 1);

            RotationHandler.getInstance().queueRotation(
                    new RotationConfiguration(
                            new Target(targetPoint),
                            MightyMinerConfig.getRandomRotationTime() * 2L,
                            null
                    )
            );
        }

        RotationHandler.getInstance().start();

        // Determine if player needs to walk toward block (too far away)
        if (this.targetPoint != null && PlayerUtil.getPlayerEyePos().distanceTo(this.targetPoint) > MAX_MINE_DISTANCE) {
            isWalking = true;
            Vec3 vec = AngleUtil.getVectorForRotation(AngleUtil.getRotationYaw(this.targetPoint));
            
            // Find walkable block closest to target
            if (mc.theWorld.isAirBlock(new BlockPos(mc.thePlayer.getPositionVector().add(vec)))) {
                this.walkingDestinationBlock = BlockUtil.getWalkableBlocksAround(PlayerUtil.getBlockStandingOn())
                        .stream()
                        .min(Comparator.comparingDouble(miner.getTargetBlockPos()::distanceSq))
                        .map(b -> new Vec3(b.getX() + 0.5, b.getY(), b.getZ() + 0.5))
                        .orElse(null);
            }
        }
    }
}
