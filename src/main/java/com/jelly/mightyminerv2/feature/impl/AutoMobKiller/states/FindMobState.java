package com.jelly.mightyminerv2.feature.impl.AutoMobKiller.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.AutoMobKiller.AutoMobKiller;
import com.jelly.mightyminerv2.feature.impl.Pathfinder;
import com.jelly.mightyminerv2.util.BlockUtil;
import com.jelly.mightyminerv2.util.EntityUtil;
import com.jelly.mightyminerv2.util.PlayerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;

import java.util.List;

public class FindMobState implements AutoMobKillerState {

    private final Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void onStart(AutoMobKiller mobKiller) {
        log("Entering Find Mob State");
    }

    @Override
    public AutoMobKillerState onTick(AutoMobKiller mobKiller) {
        if (!mobKiller.getRecheckTimer().isScheduled() || mobKiller.getRecheckTimer().passed()) {
            List<EntityLivingBase> mobs = EntityUtil.getEntities(mobKiller.getMobsToKill(), mobKiller.getMobQueue());

            if (mobs.isEmpty()) {
                if (!mobKiller.getShutdownTimer().isScheduled()) {
                    log("Cannot find mobs. Starting a 10 second timer");
                    mobKiller.getShutdownTimer().schedule(10_000);
                }

                return this;
            } else if (mobKiller.getShutdownTimer().isScheduled()) {
                mobKiller.getShutdownTimer().reset();
            }

            EntityLivingBase best = null;

            for (EntityLivingBase mob : mobs) {
                BlockPos mobPos = EntityUtil.getBlockStandingOn(mob);

                if (!mobKiller.getBlacklistedMobs().contains(mob) && BlockUtil.canStandOn(mobPos)) {
                    best = mob;
                    break;
                }
            }

            if (best == null) {
                log("Didnt find a mob that has a valid position. ");
                return new StartingState();
            }

            if (mobKiller.getTargetMob() == null || !mobKiller.getTargetMob().equals(best)) {
                mobKiller.setTargetMob(best);
                mobKiller.setEntityLastPosition(best.getPositionVector());
                Pathfinder.getInstance().stopAndRequeue(EntityUtil.nearbyBlock(mobKiller.getTargetMob()));
            }

            mobKiller.getRecheckTimer().schedule(MightyMinerConfig.devMKillTimer);
        }

        if (mobKiller.getTargetMob() == null || mobKiller.getEntityLastPosition() == null) {
            log("No target mob or last position saved");
            mobKiller.setError(AutoMobKiller.MKError.NO_ENTITIES);
            mobKiller.stop();
            return this;
        }

        if (!Pathfinder.getInstance().isRunning()) {
            log("Pathfinder wasn't enabled. Starting pathfinder");
            Pathfinder.getInstance().setSprintState(MightyMinerConfig.mobKillerSprint);
            Pathfinder.getInstance().setInterpolationState(MightyMinerConfig.mobKillerInterpolate);
            Pathfinder.getInstance().start();
        }

        if (PlayerUtil.getNextTickPosition().squareDistanceTo(mobKiller.getTargetMob().getPositionVector()) < 8 && mc.thePlayer.canEntityBeSeen(mobKiller.getTargetMob())) {
            Pathfinder.getInstance().stop();
            return new KillState();
        }

        if (!mobKiller.getTargetMob().isEntityAlive()) {
            Pathfinder.getInstance().stop();
            log("Target mob is no longer alive.");
            return new StartingState();
        }

        if (mobKiller.getTargetMob().getPositionVector().squareDistanceTo(mobKiller.getEntityLastPosition()) > 9) {
            if (++mobKiller.pathRetry > 3) {
                log("Target mob moved away from original location. Repathing");
                mobKiller.pathRetry = 0;
                return new StartingState();
            }

            log("Repathing");
            mobKiller.setEntityLastPosition(mobKiller.getTargetMob().getPositionVector());
            Pathfinder.getInstance().stopAndRequeue(EntityUtil.nearbyBlock(mobKiller.getTargetMob()));
            return this;
        }

        if (!Pathfinder.getInstance().isRunning()) {
            log("Pathfinder not enabled");
            return new StartingState();
        }

        return this;
    }

    @Override
    public void onEnd(AutoMobKiller mobKiller) {
        log("Exiting Find Mob State");
    }

}
