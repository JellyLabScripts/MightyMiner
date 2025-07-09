package com.jelly.mightyminerv2.feature.impl.AutoMobKiller.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.AutoMobKiller.AutoMobKiller;
import com.jelly.mightyminerv2.feature.impl.Pathfinder;
import com.jelly.mightyminerv2.util.EntityUtil;
import com.jelly.mightyminerv2.util.PlayerUtil;
import com.jelly.mightyminerv2.util.helper.Clock;
import net.minecraft.client.Minecraft;

public class PathfindingState implements AutoMobKillerState{

    private int pathAttempts = 0;
    private final Clock timeout = new Clock();

    @Override
    public void onStart(AutoMobKiller mobKiller) {
        log("Entering pathfinding state");
        pathAttempts = 0;
        timeout.reset();
        timeout.schedule(10_000);
        Pathfinder.getInstance().stopAndRequeue(EntityUtil.nearbyBlock(mobKiller.getTargetMob()));
        Pathfinder.getInstance().setSprintState(MightyMinerConfig.mobKillerSprint);
        Pathfinder.getInstance().setInterpolationState(MightyMinerConfig.mobKillerInterpolate);
        Pathfinder.getInstance().start();
    }

    @Override
    public AutoMobKillerState onTick(AutoMobKiller mobKiller) {
        if (PlayerUtil.getNextTickPosition().squareDistanceTo(mobKiller.getTargetMob().getPositionVector()) < 8
                && Minecraft.getMinecraft().thePlayer.canEntityBeSeen(mobKiller.getTargetMob())) {
            return new KillState();
        }

        if (!mobKiller.getTargetMob().isEntityAlive()) {
            Pathfinder.getInstance().stop();
            log("Target mob is no longer alive. Re-choosing a mob.");
            return new StartingState();
        }

        if (mobKiller.getTargetMob().getPositionVector().squareDistanceTo(mobKiller.getTargetMobOriginalPos()) > 9) {
            if (++pathAttempts > 3) {
                log("Target mob moved away from original location too many times. Re-choosing a mob.");
                mobKiller.getBlacklistedMobs().add(mobKiller.getTargetMob());
                return new StartingState();
            }
            mobKiller.setTargetMobOriginalPos(mobKiller.getTargetMob().getPositionVector());
            Pathfinder.getInstance().stopAndRequeue(EntityUtil.nearbyBlock(mobKiller.getTargetMob()));
            return this;
        }

        if (timeout.passed()) {
            log("Pathfinding timeout. Re-choosing a mob.");
            mobKiller.getBlacklistedMobs().add(mobKiller.getTargetMob());
            return new StartingState();
        }

        return this;
    }

    @Override
    public void onEnd(AutoMobKiller mobKiller) {
        Pathfinder.getInstance().stop();
    }
}
