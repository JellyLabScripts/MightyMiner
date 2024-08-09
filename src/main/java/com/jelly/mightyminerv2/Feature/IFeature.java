package com.jelly.mightyminerv2.Feature;

import com.jelly.mightyminerv2.Util.LogUtil;

public interface IFeature {

  String getName();

  boolean isEnabled();

  boolean isRunning();

  boolean shouldPauseMacroExecution();

  boolean shouldStartAtLaunch();

  void start();

  void stop();

  void resetStatesAfterStop();

  boolean shouldCheckForFailsafe();

  default void log(String message) {
    LogUtil.log(getMessage(message));
  }

  default void send(String message) {
    LogUtil.send(getMessage(message));
  }

  default void error(String message) {
    LogUtil.error(getMessage(message));
  }

  default void warn(String message) {
    LogUtil.warn(getMessage(message));
  }

  default String getMessage(String message) {
    return "[" + this.getName() + "] " + message;
  }
}
