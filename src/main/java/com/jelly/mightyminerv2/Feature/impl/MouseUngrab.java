package com.jelly.mightyminerv2.Feature.impl;

import com.jelly.mightyminerv2.Config.MightyMinerConfig;
import com.jelly.mightyminerv2.Feature.IFeature;
import com.jelly.mightyminerv2.MightyMiner;
import net.minecraft.client.Minecraft;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;

public class MouseUngrab implements IFeature {
  private final Minecraft mc = Minecraft.getMinecraft();
  private static MouseUngrab instance;

  public static MouseUngrab getInstance() {
    if (instance == null) {
      instance = new MouseUngrab();
    }
    return instance;
  }

  private boolean enabled = false;
  private MouseHelper oldMouseHelper;

  public void ungrabMouse() {
    if (!Mouse.isGrabbed() || enabled) return;
    mc.gameSettings.pauseOnLostFocus = false;
    oldMouseHelper = mc.mouseHelper;
    oldMouseHelper.ungrabMouseCursor();
    mc.inGameHasFocus = true;
    mc.mouseHelper = new MouseHelper() {
      @Override
      public void mouseXYChange() {
      }

      @Override
      public void grabMouseCursor() {
      }

      @Override
      public void ungrabMouseCursor() {
      }
    };
    enabled = true;
  }

  public void regrabMouse() {
    regrabMouse(false);
  }

  public void regrabMouse(boolean force) {
    if (!enabled && !force) return;
    mc.mouseHelper = oldMouseHelper;
    if (mc.currentScreen == null || force) {
      mc.mouseHelper.grabMouseCursor();
    }
    enabled = false;
  }

  @Override
  public String getName() {
    return "Ungrab Mouse";
  }

  @Override
  public boolean isEnabled() {
    return MightyMinerConfig.ungrabMouse;
  }

  @Override
  public boolean isRunning() {
    return this.enabled;
  }

  @Override
  public boolean shouldStartAtLaunch() {
    return this.isEnabled();
  }

  @Override
  public void start() {
    try {
      ungrabMouse();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void stop() {
    try {
      regrabMouse();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void resetStatesAfterStop() {

  }
}
