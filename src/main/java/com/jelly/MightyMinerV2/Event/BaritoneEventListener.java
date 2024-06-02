package com.jelly.MightyMinerV2.Event;

import baritone.api.behavior.IBehavior;
import baritone.api.event.events.PathEvent;

public class BaritoneEventListener implements IBehavior {
    public static PathEvent pathEvent;
    @Override
    public void onPathEvent(PathEvent event) {
        pathEvent = event;
    }
}