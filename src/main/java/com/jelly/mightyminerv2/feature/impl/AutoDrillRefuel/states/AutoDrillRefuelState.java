package com.jelly.mightyminerv2.feature.impl.AutoDrillRefuel.states;

import com.jelly.mightyminerv2.feature.impl.AutoDrillRefuel.AutoDrillRefuel;


public interface AutoDrillRefuelState {

    void onStart(AutoDrillRefuel refueler);
    AutoDrillRefuelState onTick(AutoDrillRefuel refueler);
    void onEnd(AutoDrillRefuel refueler);

    default void log(String message) {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + message);
    }

    default void logError(String message) {
        System.out.println("[" + this.getClass().getSimpleName() + "] ERROR: " + message);
    }
}
