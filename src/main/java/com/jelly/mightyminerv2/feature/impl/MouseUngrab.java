package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;

public class MouseUngrab extends AbstractFeature {

    private static volatile MouseUngrab instance;
    private MouseHelper oldMouseHelper;
    private boolean oldPauseOnLostFocus;
    private boolean mouseUngrabbed = false;

    public static MouseUngrab getInstance() {
        if (instance == null) {
            synchronized (MouseUngrab.class) {
                if (instance == null) {
                    instance = new MouseUngrab();
                }
            }
        }
        return instance;
    }

    public void ungrabMouse() {
        if (mouseUngrabbed || !Mouse.isGrabbed()) {
            return;
        }

        oldMouseHelper = mc.mouseHelper;
        oldPauseOnLostFocus = mc.gameSettings.pauseOnLostFocus;
        mc.mouseHelper.ungrabMouseCursor();

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

        mc.gameSettings.pauseOnLostFocus = false;
        mouseUngrabbed = true;
        log("Mouse ungrabbed successfully.");
    }

    public void regrabMouse() {
        if (!mouseUngrabbed || Mouse.isGrabbed()) {
            return;
        }

        if (oldMouseHelper != null) {
            mc.mouseHelper = oldMouseHelper;
            oldMouseHelper = null;
        }

        if (mc.currentScreen == null) {
            mc.mouseHelper.grabMouseCursor();
        }

        mc.gameSettings.pauseOnLostFocus = oldPauseOnLostFocus;
        mouseUngrabbed = false;
        log("Mouse regrabbed successfully.");
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
    public boolean shouldStartAtLaunch() {
        return this.isEnabled();
    }

    @Override
    public void start() {
        log("MouseUngrab::start");
        try {
            ungrabMouse();
        } catch (Exception e) {
            log("Failed to ungrab mouse: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        log("MouseUngrab::stop");
        try {
            regrabMouse();
        } catch (Exception e) {
            log("Failed to regrab mouse: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
