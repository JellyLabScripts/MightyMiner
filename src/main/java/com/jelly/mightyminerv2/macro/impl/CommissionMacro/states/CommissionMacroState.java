package com.jelly.mightyminerv2.macro.impl.CommissionMacro.states;

import com.jelly.mightyminerv2.macro.impl.CommissionMacro.CommissionMacro;

public interface CommissionMacroState {

    void onStart(CommissionMacro macro);

    CommissionMacroState onTick(CommissionMacro macro);

    void onEnd(CommissionMacro macro);

    default void log(String message) {
        System.out.println("[" + this.getClass().getSimpleName() + "] " + message);
    }

    default void logError(String message) {
        System.out.println("[" + this.getClass().getSimpleName() + "] ERROR: " + message);
    }

}
