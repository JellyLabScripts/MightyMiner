package com.jelly.mightyminerv2.Feature;

import com.jelly.mightyminerv2.Feature.impl.AutoCommissionClaim;
import com.jelly.mightyminerv2.Feature.impl.AutoInventory;
import com.jelly.mightyminerv2.Feature.impl.AutoMobKiller;
import com.jelly.mightyminerv2.Feature.impl.MithrilMiner;
import com.jelly.mightyminerv2.Feature.impl.Pathfinder;
import com.jelly.mightyminerv2.Feature.impl.RouteNavigator;
import com.jelly.mightyminerv2.Feature.impl.RouteBuilder;
import com.jelly.mightyminerv2.Util.LogUtil;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class FeatureManager {

  private static FeatureManager instance;
  private final List<IFeature> features = new ArrayList<>();

  public static FeatureManager getInstance() {
    if (instance == null) {
      instance = new FeatureManager();
    }
    return instance;
  }

  public FeatureManager() {
    addFeature(RouteBuilder.getInstance());
    addFeature(RouteNavigator.getInstance());
    addFeature(AutoInventory.getInstance());

    addFeature(MithrilMiner.getInstance());
    addFeature(AutoCommissionClaim.getInstance());
    addFeature(AutoMobKiller.getInstance());
    addFeature(Pathfinder.getInstance());
  }

  public void enableAll() {
    for (IFeature feature : features) {
      if (feature.shouldStartAtLaunch() && !feature.isEnabled()) {
        feature.start();
        LogUtil.send("Enabled Feature:" + feature.getName());
      }

    }
  }

  public void disableAll() {
    for (IFeature feature : features) {
      if (feature.isEnabled()) {
        feature.stop();
        LogUtil.send("Disabled Feature:" + feature.getName());
      }
    }
  }

  public boolean shouldPauseMacroExecution() {
    return features.stream().anyMatch(feature -> {
      if (feature.isEnabled() && feature.isRunning()) {
        return feature.shouldPauseMacroExecution();
      }
      return false;
    });
  }

  public void addFeature(IFeature feature) {
    features.add(feature);
  }

  public void removeFeature(IFeature feature) {
    features.remove(feature);
  }

  public void resetAllStates() {
    features.forEach(IFeature::resetStatesAfterStop);
  }

  public void disableCurrentRunning(IFeature sender) {
    features.stream().filter(IFeature::isRunning).forEach(feature -> {
        if (feature == sender) {
            return;
        }
      feature.stop();
      LogUtil.send("Disabled Feature:" + feature.getName());
    });
  }

  public void disableAllExcept(IFeature... sender) {
    features.stream().filter(IFeature::isRunning).forEach(feature -> {
        if (Arrays.asList(sender).contains(feature)) {
            return;
        }
      feature.stop();
      LogUtil.send("Disabled Feature:" + feature.getName());
    });
  }

  public List<IFeature> getCurrentRunning() {
    List<IFeature> running = new ArrayList<>();
    features.stream().filter(IFeature::isRunning).forEach(running::add);
    return running;
  }
}
