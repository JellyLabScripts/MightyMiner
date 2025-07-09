package com.jelly.mightyminerv2.feature.impl.AutoMobKiller.states;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.impl.AutoMobKiller.AutoMobKiller;
import com.jelly.mightyminerv2.feature.impl.Pathfinder;
import com.jelly.mightyminerv2.util.BlockUtil;
import com.jelly.mightyminerv2.util.EntityUtil;
import com.jelly.mightyminerv2.util.PlayerUtil;
import com.jelly.mightyminerv2.util.helper.Clock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class FindMobState implements AutoMobKillerState {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final Clock waitForMobsToSpawnTimer = new Clock();

    @Override
    public void onStart(AutoMobKiller mobKiller) {
        log("Entering Find Mob State");
        waitForMobsToSpawnTimer.reset();
    }

    @Override
    public AutoMobKillerState onTick(AutoMobKiller mobKiller) {

        EntityLivingBase mob = findBestMob(mobKiller);

        if (mob == null) {
            if (!waitForMobsToSpawnTimer.isScheduled()) {
                log("Scheduled a 10-second timer to see if mobs spawn or not");
                waitForMobsToSpawnTimer.schedule(10_000);
            }

            if (waitForMobsToSpawnTimer.isScheduled() && waitForMobsToSpawnTimer.passed()) {
                log("Cannot find a mob to kill!");
                mobKiller.setError(AutoMobKiller.MKError.NO_ENTITIES);
                mobKiller.stop();
                return null;
            }

            return this;
        }

        mobKiller.setTargetMob(mob);
        mobKiller.setTargetMobOriginalPos(mob.getPositionVector());
        return new PathfindingState();
    }

    private EntityLivingBase findBestMob(AutoMobKiller mobKiller) {
        Minecraft mc = Minecraft.getMinecraft();
        List<EntityPlayer> playerEntities = new ArrayList<>(Minecraft.getMinecraft().theWorld.playerEntities);
        playerEntities.remove(mc.thePlayer);

        List<EntityPlayer> actualPlayers = playerEntities.stream()
                .filter(player -> !EntityUtil.isNpc(player))
                .collect(Collectors.toList());

        List<EntityLivingBase> mobs = EntityUtil.getEntities(mobKiller.getMobsToKill(), mobKiller.getBlacklistedMobs());

        if(mobs.isEmpty())
            return null;

        List<EntityLivingBase> filteredMobs = mobs.stream()
                .filter(mob -> mc.thePlayer.getDistanceSqToEntity(mob) < 1000)
                .filter(mob -> actualPlayers.stream()
                        .noneMatch(player -> player.getDistanceSqToEntity(mob) < 9.0f))
                .filter(mob -> BlockUtil.canStandOn(EntityUtil.getBlockStandingOn(mob)))
                .collect(Collectors.toList());

        return filteredMobs.stream()
                .min(Comparator.comparingDouble(mob -> mc.thePlayer.getDistanceSqToEntity(mob)))
                .orElse(null);
    }


    @Override
    public void onEnd(AutoMobKiller mobKiller) {
        log("Exiting Find Mob State");
    }

}
