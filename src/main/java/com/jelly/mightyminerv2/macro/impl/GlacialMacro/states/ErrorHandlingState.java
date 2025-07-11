package com.jelly.mightyminerv2.macro.impl.GlacialMacro.states;

import com.jelly.mightyminerv2.macro.impl.GlacialMacro.GlacialMacro;

public class ErrorHandlingState implements GlacialMacroState {
    private final String reason;

    public ErrorHandlingState(String reason) {
        this.reason = reason;
    }

    @Override
    public void onStart(GlacialMacro macro) {
        logError("Entering Error Handling State");
        macro.disable(reason);
    }

    @Override
    public GlacialMacroState onTick(GlacialMacro macro) {
        return this;
    }

    @Override
    public void onEnd(GlacialMacro macro) {
    }
}