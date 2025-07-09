package com.jelly.mightyminerv2.macro.impl.GlacialMacro.states;

import com.jelly.mightyminerv2.macro.impl.GlacialMacro.GlacialMacro;
import com.jelly.mightyminerv2.util.Logger;

/**
 * Interface representing the state of the Glacial Macro.
 * Each state defines its own behavior for starting, ticking, and ending.
 */
public interface GlacialMacroState {

    void onStart(GlacialMacro macro);

    GlacialMacroState onTick(GlacialMacro macro);

    void onEnd(GlacialMacro macro);

    default void log(String message) {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + message);
    }

    default void logError(String message) {
        System.out.println("[" + this.getClass().getSimpleName() + "] ERROR: " + message);
    }

    default void send(String message) {
        Logger.addMessage("[" + this.getClass().getSimpleName() + "] " + message);
    }
}
