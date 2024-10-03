package com.jelly.mightyminerv2.feature;

import com.jelly.mightyminerv2.feature.impl.AutoCommissionClaim;
import com.jelly.mightyminerv2.feature.impl.AutoDrillRefuel;
import com.jelly.mightyminerv2.feature.impl.AutoInventory;
import com.jelly.mightyminerv2.feature.impl.AutoMobKiller;
import com.jelly.mightyminerv2.feature.impl.AutoWarp;
import com.jelly.mightyminerv2.feature.impl.MithrilMiner;
import com.jelly.mightyminerv2.feature.impl.MobTracker;
import com.jelly.mightyminerv2.feature.impl.MouseUngrab;
import com.jelly.mightyminerv2.feature.impl.Pathfinder;
import com.jelly.mightyminerv2.feature.impl.RouteBuilder;
import com.jelly.mightyminerv2.feature.impl.RouteNavigator;
import java.util.Arrays;
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
        MithrilMiner.getInstance(),
        MouseUngrab.getInstance(),
        Pathfinder.getInstance(),
        RouteBuilder.getInstance(),
        RouteNavigator.getInstance(),
        MobTracker.getInstance(),
        AutoDrillRefuel.getInstance()
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
}
