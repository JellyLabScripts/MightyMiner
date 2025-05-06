package com.jelly.mightyminerv2.macro.impl.CommissionMacro.states;

import com.jelly.mightyminerv2.feature.impl.AutoWarp;
import com.jelly.mightyminerv2.macro.impl.CommissionMacro.CommissionMacro;
import com.jelly.mightyminerv2.util.helper.location.SubLocation;

public class WarpingState implements CommissionMacroState{

    AutoWarp autoWarp = AutoWarp.getInstance();

    @Override
    public void onStart(CommissionMacro macro) {
        log("Starting warping state");
        autoWarp.start(null, SubLocation.THE_FORGE);
    }

    @Override
    public CommissionMacroState onTick(CommissionMacro macro) {

        if (AutoWarp.getInstance().isRunning()) {
            return this;
        }

        if (AutoWarp.getInstance().hasSucceeded()) {
            log("Auto Warp Completed");
            return new StartingState();
        }

        switch (AutoWarp.getInstance().getFailReason()) {
            case NONE:
                macro.disable("Auto Warp failed, but no error is detected. Please contact the developer.");
                break;
            case FAILED_TO_WARP:
                log("Retrying Auto Warp");
                autoWarp.start(null, SubLocation.THE_FORGE);
                break;
            case NO_SCROLL:
                macro.disable("You don't have the /warp forge scroll!");
                break;
        }
        return null;
    }

    @Override
    public void onEnd(CommissionMacro macro) {
        autoWarp.stop();
        log("Ending warping state");
    }
}
