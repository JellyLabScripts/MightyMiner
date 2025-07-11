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
 * <p>
 * State responsible for breaking the selected target block.
 * Handles player rotation, movement, and mining mechanics.
 * Will attempt to move towards the block if too far away.
 */
public class BreakingState implements BlockMinerState{

    /** Reference to the Minecraft client instance. */
    private final Minecraft mc = Minecraft.getMinecraft();

    /** Random number generator for introducing slight variability (e.g., in targeting or movement). */
    private final Random random = new Random();

    /** Minimum distance required between the player and block to trigger walking behavior. */
    private static final double MIN_WALK_DISTANCE = 0.2;

    /** The maximum allowed distance for the player to attempt to mine a block. */
    private static final double MAX_MINE_DISTANCE = 4;

    /** Number of ticks after which a failsafe is triggered if mining takes too long. */
    private static final int FAILSAFE_TICKS = 40;

    /** Time in milliseconds the player can look away from the block before switching targets. */
    private static final int LOOK_AWAY_THRESHOLD_MS = 500;

    /** Timer used to track how long the player has been looking away from the target block. */
    private Clock lookAwayTimer;

    /** Flag indicating whether the player was looking away from the block in the previous tick. */
    private boolean wasLookingAway = false;

    /** Number of ticks the player has been attempting to break the current block. */
    private int breakAttemptTime;

    /** Estimated number of ticks required to break the current block. */
    private int miningTime;

    /** The exact point on the block being targeted for mining. */
    private Vec3 targetPoint;

    /** The block position that the player is walking toward if not in range to mine. */
    private Vec3 walkingDestinationBlock;

    /** Indicates whether the player is currently walking toward the target block. */
    private boolean isWalking;


    @Override
    public void onStart(BlockMiner miner) {
        log("Entering Breaking State");
        breakAttemptTime = 0;
        isWalking = false;

        lookAwayTimer = new Clock();
        wasLookingAway = false;

        miningTime = BlockUtil.getMiningTime(
                Block.getStateId(Minecraft.getMinecraft().theWorld.getBlockState(miner.getTargetBlockPos())),
                miner.getMiningSpeed()
        );

        // Setup rotation to look at the block
        RotationHandler.getInstance().stop();
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

        // Handle precision mining
        if (miner.getTargetParticlePos() != null) {
            RotationHandler.getInstance().easeTo(new RotationConfiguration(new Target(miner.getTargetParticlePos()), 800, null).followTarget(true));
            miner.setTargetParticlePos(null);
        }

        // Safety mechanism: if we've been trying to break for too long, reset
        if (++this.breakAttemptTime > this.miningTime + FAILSAFE_TICKS) {
            logError("Stuck while mining, return to starting state");
            return new StartingState();
        }

        // Safety mechanism: if we're looking away from target block, reset
        BlockPos currentLookingAt = BlockUtil.getBlockLookingAt();
        boolean isLookingAtTarget = miner.getTargetBlockPos().equals(currentLookingAt);
        if (!isLookingAtTarget) {
            if (!wasLookingAway) {
                lookAwayTimer.schedule(LOOK_AWAY_THRESHOLD_MS);
                wasLookingAway = true;
            } else if (lookAwayTimer.passed()) {
                log("Player looked away from target block for too long, choosing new block");
                return new StartingState();
            }
        } else {
            wasLookingAway = false;
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
     * Handles walking mechanics when the player needs to move toward target block.
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
        if (random.nextBoolean() && MightyMinerConfig.randomizedRotations) {
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

        // Determine if the player needs to walk toward block (too far away)
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
