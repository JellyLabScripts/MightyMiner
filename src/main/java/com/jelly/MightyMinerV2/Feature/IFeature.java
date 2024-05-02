package com.jelly.MightyMinerV2.Feature;

public interface IFeature {
    String getName();
    boolean isEnabled();
    boolean isRunning();
    boolean shouldPauseMacroExecution();
    boolean shouldStartAtLaunch();
    void start();
    void stop();
    void resetFailsafeAfterStop();
    boolean isToggle();
    boolean shouldCheckForFailSafe();
}
