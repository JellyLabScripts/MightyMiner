package com.jelly.MightyMinerV2.Feature;

import com.jelly.MightyMinerV2.Util.LogUtil;

public interface IFeature {
    String getName();
    boolean isEnabled();
    boolean isRunning();
    boolean shouldPauseMacroExecution();
    boolean shouldStartAtLaunch();
    void start();
    void stop();
    void resetStatesAfterStop();
    boolean isToggle();
    boolean shouldCheckForFailSafe();

    default void log(String message){
        LogUtil.send(getMessage(message), LogUtil.ELogType.DEBUG);
    }

    default void success(String message){
        LogUtil.send(getMessage(message), LogUtil.ELogType.SUCCESS);
    }

    default void error(String message){
        LogUtil.send(getMessage(message), LogUtil.ELogType.ERROR);
    }

    default void warning(String message){
        LogUtil.send(getMessage(message), LogUtil.ELogType.WARNING);
    }

    default String getMessage(String message){
        return "[" + this.getName() + "] " + message;
    }
}
