package com.jelly.mightyminerv2.feature;

import com.jelly.mightyminerv2.failsafe.AbstractFailsafe.Failsafe;
import com.jelly.mightyminerv2.feature.impl.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class FeatureManager {

  private static FeatureManager instance;

  public static FeatureManager getInstance() {
    if (instance == null) {
      instance = new FeatureManager();
    }
    return instance;
  }

  public final Set<AbstractFeature> allFeatures = new LinkedHashSet<>();

  public FeatureManager() {
    this.allFeatures.addAll(Arrays.asList(
        AutoCommissionClaim.getInstance(),
        AutoInventory.getInstance(),
        AutoMobKiller.getInstance(),
        AutoWarp.getInstance(),
        BlockMiner.getInstance(),
        MouseUngrab.getInstance(),
        Pathfinder.getInstance(),
        RouteBuilder.getInstance(),
        RouteNavigator.getInstance(),
        MobTracker.getInstance(),
        AutoDrillRefuel.getInstance(),
        AutoChestUnlocker.instance,
        Nuker.getInstance(),
        FastBreak.getInstance()
    ));
  }

  public void enableAll() {
    this.allFeatures.forEach(it -> {
      if (it.shouldStartAtLaunch()) {
        it.start();
      }
    });
  }

  public void disableAll() {
    this.allFeatures.forEach(it -> {
      if (it.isRunning()) {
        it.stop();
      }
    });
  }

  public void pauseAll() {
    this.allFeatures.forEach(it -> {
      if (it.isRunning()) {
        it.pause();
      }
    });
  }

  public void resumeAll() {
    this.allFeatures.forEach(it -> {
      if (it.isRunning()) {
        it.resume();
      }
    });
  }

  public boolean shouldNotCheckForFailsafe() {
    return this.allFeatures.stream().filter(AbstractFeature::isRunning).anyMatch(AbstractFeature::shouldNotCheckForFailsafe);
  }

  public Set<Failsafe> getFailsafesToIgnore(){
    Set<Failsafe> failsafes = new HashSet<>();
    this.allFeatures.forEach(it -> {
      if(it.isRunning()){
        failsafes.addAll(it.getFailsafesToIgnore());
      }
    });
    return failsafes;
  }
}
