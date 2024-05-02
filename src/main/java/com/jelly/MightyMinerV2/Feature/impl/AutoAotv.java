package com.jelly.MightyMinerV2.Feature.impl;

import com.jelly.MightyMinerV2.Feature.FeatureManager;
import com.jelly.MightyMinerV2.Feature.IFeature;
import lombok.Getter;

@Getter
public class AutoAotv implements IFeature {
    private static AutoAotv instance;

    public static AutoAotv getInstance() {
        if (instance == null) {
            instance = new AutoAotv();
            FeatureManager.getInstance().addFeature(instance);
        }
        return instance;
    }

    private boolean enabled = false;

    @Override
    public String getName() {
        return "AutoAotv";
    }

    @Override
    public boolean isRunning() {
        return this.enabled;
    }

    @Override
    public boolean shouldPauseMacroExecution() {
        return true;
    }

    @Override
    public boolean shouldStartAtLaunch() {
        return false;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        this.enabled = false;
    }

    @Override
    public void resetStatesAfterStop() {

    }

    @Override
    public boolean isToggle() {
        return false;
    }

    @Override
    public boolean shouldCheckForFailSafe() {
        return true;
    }
}
