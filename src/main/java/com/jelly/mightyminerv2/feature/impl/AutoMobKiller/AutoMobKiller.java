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

import java.util.*;

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
     * Target mob to kill
     */
    @Getter
    @Setter
    private EntityLivingBase targetMob = null;

    /**
     * Original position of the target mob (to check if it has moved away)
     */
    @Getter
    @Setter
    private Vec3 targetMobOriginalPos = null;

    /**
     * Number of Re-pathing attempts
     */
    @Getter
    @Setter
    private int pathAttempts = 0;

    /**
     * Blacklisted mobs (from failed pathfinding attempts)
     */
    @Getter
    @Setter
    private Set<EntityLivingBase> blacklistedMobs = new HashSet<>();

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
        if (!InventoryUtil.holdItem(weaponName)) {
            sendError("Weapon not found in inventory!");
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
        this.blacklistedMobs.clear();
        this.targetMob = null;
        this.targetMobOriginalPos = null;
        this.currentState = null;

        Pathfinder.getInstance().stop();
        log("MobKiller stopped");
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!this.enabled || mc.currentScreen != null || event.phase == TickEvent.Phase.END) {
            return;
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
}
