package com.jelly.mightyminerv2.feature.impl.AutoMobKiller;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.feature.impl.AutoMobKiller.states.AutoMobKillerState;
import com.jelly.mightyminerv2.feature.impl.AutoMobKiller.states.StartingState;
import com.jelly.mightyminerv2.feature.impl.Pathfinder;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.helper.Clock;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * MobKiller
 * <p>
 * Main controller class for automatic mob killer feature.
 * Implements a state machine pattern to manage different phases of the killing process.
 * Handles mob selection, pathfinding, and attack management.
 */
public class AutoMobKiller extends AbstractFeature {

    private static AutoMobKiller instance;

    public static AutoMobKiller getInstance() {
        if (instance == null) {
            instance = new AutoMobKiller();
        }

        return instance;
    }

    private AutoMobKillerState currentState;

    public enum MKError {
        NONE,        // No error
        NO_ENTITIES  // No entities found
    }

    @Getter
    @Setter
    private MKError error = MKError.NONE;

    /**
     * Names of mobs to kill (ex: Glacite Walker)
     */
    @Getter
    private final Set<String> mobsToKill = new HashSet<>();

    /**
     * Queue of mobs being killed
     */
    @Getter
    private final Set<EntityLivingBase> mobQueue = new HashSet<>();

    /**
     * Target mob (determines what mob to kill)
     */
    @Getter
    @Setter
    private EntityLivingBase targetMob = null;

    /**
     * Target mob's last recorded position (determines what mob to kill)
     */
    @Getter
    @Setter
    private Vec3 entityLastPosition = null;

    /**
     * Needed to stop AutoMobKiller because of no entities found
     */
    @Getter
    private final Clock shutdownTimer = new Clock();

    /**
     * Clears mobQueue after a set time in config
     */
    @Getter
    private final Clock queueTimer = new Clock();

    /**
     * Target mob (determines what mob to kill)
     */
    @Getter
    private final Clock recheckTimer = new Clock();

    /**
     * List of mobs being hunted by a player (not possible to kill)
     */
    @Getter
    private final Set<EntityLivingBase> blacklistedMobs = new HashSet<>();

    /**
     * Repathing attempts in FindMobState
     */
    public int pathRetry = 0;

    /**
     * Last entity attacked (needed so we can blacklist the correct mob)
     */
    @Getter
    @Setter
    private EntityLivingBase lastTarget = null;

    @Override
    public String getName() {
        return "AutoMobKiller";
    }

    /**
     * Starts the AutoMobKiller with specified parameters. Will continue to kill mobs {@code mobsToKill} until stop() is called or no entities found in 10 seconds.
     *
     * @param mobsToKill List of mob names to kill
     * @param weaponName Name of the melee weapon that will be used to kill mobs
     */
    public void start(Collection<String> mobsToKill, String weaponName) {
        // Should never happen, but just in case
        if (!InventoryUtil.holdItem(weaponName)) {
            sendError("Could not hold weapon!");
            stop();
            return;
        }

        this.mobsToKill.addAll(mobsToKill);
        this.error = MKError.NONE;

        this.currentState = new StartingState();
        this.enabled = true;
        log("MobKiller started");
    }

    @Override
    public void stop() {
        if (!this.enabled) {
            return;
        }

        this.mobsToKill.clear();
        this.targetMob = null;
        this.currentState = null;
        this.timer.reset();
        this.shutdownTimer.reset();
        Pathfinder.getInstance().stop();
        log("MobKiller stopped");
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!this.enabled || mc.currentScreen != null || event.phase == TickEvent.Phase.END) {
            return;
        }

        if (this.shutdownTimer.isScheduled() && this.shutdownTimer.passed()) {
            this.error = MKError.NO_ENTITIES;
            this.stop();
            log("Entities did not spawn");
            return;
        }

        if (this.queueTimer.passed()) {
            log("Cleared mob queue");
            this.mobQueue.clear();
            this.queueTimer.schedule(MightyMinerConfig.devMKillTimer);
        }

        if (currentState == null)
            return;

        AutoMobKillerState nextState = currentState.onTick(this);
        transitionTo(nextState);
    }

    private void transitionTo(AutoMobKillerState nextState) {
        // Skip if no state change
        if (currentState == nextState)
            return;

        currentState.onEnd(this);
        currentState = nextState;

        if (currentState == null) {
            log("null state, returning");
            return;
        }

        currentState.onStart(this);
    }

    public boolean succeeded() {
        return !this.enabled && this.error == MKError.NONE;
    }

}
