package com.jelly.mightyminerv2.feature.impl.AutoMobKiller.states;

import com.jelly.mightyminerv2.feature.impl.AutoMobKiller.AutoMobKiller;

/**
 * AutoMobKillerState
 * <p>
 * Interface for all states in the AutoMobKiller state machine.
 * Implements the State design pattern to encapsulate different behaviors
 * for each phase of the mining process.
 */
public interface AutoMobKillerState {
    /**
     * Called when entering this state.
     * Use for initialization and setup logic.
     *
     * @param mobKiller Reference to the AutoMobKiller instance
     */
    void onStart(AutoMobKiller mobKiller);

    /**
     * Called on each game tick while this state is active.
     * Contains the main processing logic for the state.
     *
     * @param mobKiller Reference to the AutoMobKiller instance
     * @return The next state to transition to, or this if staying in current state
     */
    AutoMobKillerState onTick(AutoMobKiller mobKiller);

    /**
     * Called when exiting this state.
     * Use for cleanup and finalization logic.
     *
     * @param mobKiller Reference to the AutoMobKiller instance
     */
    void onEnd(AutoMobKiller mobKiller);

    default void log(String message) {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + message);
    }

    default void logError(String message) {
        System.out.println("[" + this.getClass().getSimpleName() + "] ERROR: " + message);
    }
}
