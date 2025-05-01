package com.jelly.mightyminerv2.feature.impl.BlockMiner.states;

import com.jelly.mightyminerv2.feature.impl.BlockMiner.BlockMiner;

/**
 * BlockMinerState
 * 
 * Interface for all states in the BlockMiner state machine.
 * Implements the State design pattern to encapsulate different behaviors
 * for each phase of the mining process.
 */
public interface BlockMinerState {
    /**
     * Called when entering this state.
     * Use for initialization and setup logic.
     * 
     * @param miner Reference to the BlockMiner instance
     */
    void onStart(BlockMiner miner);
    
    /**
     * Called on each game tick while this state is active.
     * Contains the main processing logic for the state.
     * 
     * @param miner Reference to the BlockMiner instance
     * @return The next state to transition to, or this if staying in current state
     */
    BlockMinerState onTick(BlockMiner miner);
    
    /**
     * Called when exiting this state.
     * Use for cleanup and finalization logic.
     * 
     * @param miner Reference to the BlockMiner instance
     */
    void onEnd(BlockMiner miner);

    default void log(String message) {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + message);
    }

    default void logError(String message) {
        System.out.println("[" + this.getClass().getSimpleName() + "] ERROR: " + message);
    }
}
