package com.jelly.mightyminerv2.feature.impl.BlockMiner.states;

import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;
import com.jelly.mightyminerv2.util.KeyBindUtil;

/**
 * StartingState
 * 
 * Initial state in the BlockMiner state machine.
 * Acts as an entry point and determines the next appropriate state.
 */
public class StartingState implements BlockMinerState {

    @Override
    public void onStart(BlockMiner miner) {
        log("Entering Starting State");
    }

    @Override
    public BlockMinerState onTick(BlockMiner miner) {
        log("Starting state delay complete. Transitioning...");
        
        // Check if speed boost is available to decide next state
        return miner.getBoostState() == BlockMiner.BoostState.AVAILABLE 
            ? new SpeedBoostState()    // Use available speed boost
            : new ChoosingBlockState(); // Find blocks to mine
    }

    @Override
    public void onEnd(BlockMiner miner) {
        log("Exiting Starting State");
    }
}
