package com.jelly.mightyminerv2.feature.impl.BlockMiner.states;

import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.helper.Clock;
import net.minecraft.client.Minecraft;

/**
 * SpeedBoostState
 * 
 * State responsible for activating the mining speed boost ability.
 * Simulates a right-click to use the pickaxe ability and then waits
 * for a cooldown period before transitioning to the next state.
 */
public class SpeedBoostState implements BlockMinerState {

    private final Clock timer = new Clock();
    private final long COOLDOWN = 1000; // 1-second cooldown after activating boost

    @Override
    public void onStart(BlockMiner blockMiner) {
        log("Entering Speed Boost State");
        timer.reset();

        // Activate the speed boost by right-clicking
        if(Minecraft.getMinecraft().currentScreen == null) {
            KeyBindUtil.releaseAllExcept();
            KeyBindUtil.rightClick();
        }
    }

    @Override
    public BlockMinerState onTick(BlockMiner blockMiner) {
        // Start the cooldown timer if not already scheduled
        if (!timer.isScheduled()) {
            log("Scheduled a 1-second timer for activating speed boost");
            timer.schedule(COOLDOWN);
        }

        // If the timer has ended, transition back to the starting state
        if (timer.isScheduled() && timer.passed()) {
            return new StartingState();
        }

        // Wait for the timer to expire
        return this;
    }

    @Override
    public void onEnd(BlockMiner blockMiner) {
        log("Exiting Speed Boost State");
    }
}
