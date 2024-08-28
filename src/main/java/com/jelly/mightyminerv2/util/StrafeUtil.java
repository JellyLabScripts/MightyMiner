package com.jelly.mightyminerv2.util;

public final class StrafeUtil {

  public static volatile boolean enabled = false;
  public static volatile boolean forceStop = false;
  public static volatile float yaw = 0.0f;

  public static boolean shouldEnable() {
    return !forceStop && enabled;
  }
}
