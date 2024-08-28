package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.util.LogUtil;
import java.util.LinkedHashSet;
import java.util.Set;

// Idk how good this is - using featuretracker and shiz
// Todo: fuck this shit and restructure everything :angrie:
public class FeatureTracker {

  private static FeatureTracker instance;

  public static FeatureTracker getInstance() {
    if (instance == null) {
      instance = new FeatureTracker();
    }
    return instance;
  }

  public final Set<AbstractFeature> allFeatures = new LinkedHashSet<>();
  public final Set<AbstractFeature> activeFeatures = new LinkedHashSet<>();
  public boolean updated = false;

  // Just need to call the features with shouldStartAtLaunch = true because they aren't called from other classes
  // This is cancer
  public FeatureTracker() {
    MouseUngrab.getInstance();
  }

  public void addFeature(AbstractFeature feature) {
    LogUtil.log("Added " + feature.getName());
    this.allFeatures.add(feature);
  }

  public void enableFeature(AbstractFeature feature) {
    this.activeFeatures.add(feature);
    this.updated = true;
  }

  public void disableFeature(AbstractFeature feature) {
    this.activeFeatures.remove(feature);
    this.updated = true;
  }

  public void stopAllFeatures() {
    this.activeFeatures.forEach(AbstractFeature::stop);
    this.activeFeatures.clear();
  }

  public void startFeatures() {
    this.allFeatures.forEach(it -> {
      if (it.shouldStartAtLaunch()) {
        it.start();
      }
    });
  }
}
