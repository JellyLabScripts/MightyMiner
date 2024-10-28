package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.pathfinder.helper.BlockStateAccessor;
import com.jelly.mightyminerv2.pathfinder.movement.MovementHelper;
import com.jelly.mightyminerv2.util.BlockUtil;
import com.jelly.mightyminerv2.util.EntityUtil;
import com.jelly.mightyminerv2.util.InventoryUtil;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.PlayerUtil;
import com.jelly.mightyminerv2.util.RenderUtil;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.Target;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import kotlin.Pair;
import kotlin.Triple;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

// Todo: Make it a universal mob killer perhaps?
//  idk its not a combat macro
public class AutoMobKiller extends AbstractFeature {

  private static AutoMobKiller instance;

  public static AutoMobKiller getInstance() {
    if (instance == null) {
      instance = new AutoMobKiller();
    }
    return instance;
  }

  private State state = State.STARTING;
  private MKError mkError = MKError.NONE;
  private final Clock shutdownTimer = new Clock();
  private final Clock queueTimer = new Clock();
  private final Clock recheckTimer = new Clock();
  private final Set<EntityLivingBase> mobQueue = new HashSet<>();
  private Optional<EntityLivingBase> targetMob = Optional.empty();
  private Optional<Vec3> entityLastPosition = Optional.empty();
  private final Set<String> mobToKill = new HashSet<>();
  private Map<EntityLivingBase, Pair<String, BlockPos>> debug = new HashMap<>();
  int pathRetry = 0;
//  private Set<String> mobToKill = ImmutableSet.of("Glacite Walker", "Goblin", "Knifethrower", "Zombie", "Crypt Ghoul", "");
//  private List<EntityLivingBase> mobs = new ArrayList<>();

  @Override
  public String getName() {
    return "MobKiller";
  }

  public void start(Collection<String> mobsToKill, String weaponName) {
    // this should NEVER happen
    if (!InventoryUtil.holdItem(weaponName)) {
      error("Could not hold MobKiller Weapon");
      this.stop(MKError.NO_ENTITIES);
      return;
    }
    this.mobToKill.addAll(mobsToKill);
    this.mkError = MKError.NONE;
    this.enabled = true;

    debug.clear();
    this.start();
    note("Mobkiller started for " + mobsToKill);
    log("Started");
  }

  @Override
  public void stop() {
    if (!this.enabled) {
      return;
    }
    this.enabled = false;
    this.mobToKill.clear();
    this.timer.reset();
    this.shutdownTimer.reset();
    this.targetMob = Optional.empty();
    this.resetStatesAfterStop();
    Pathfinder.getInstance().stop();
//    mobs.clear();

    log("stopped");
  }

  public void stop(MKError error) {
    this.mkError = error;
    this.stop();
  }

  @Override
  public void resetStatesAfterStop() {
    this.state = State.STARTING;
  }

  public boolean succeeded() {
    return !this.enabled && this.mkError == MKError.NONE;
  }

  public MKError getMkError() {
    return this.mkError;
  }

  @SubscribeEvent
  protected void onTick(ClientTickEvent event) {
    if (!this.enabled) {
      return;
    }

    if (this.shutdownTimer.isScheduled() && this.shutdownTimer.passed()) {
      this.stop(MKError.NO_ENTITIES);
      log("Entities did not spawn");
      return;
    }

    if (this.queueTimer.passed()) {
      log("Queue celared");
      this.mobQueue.clear();
      this.queueTimer.schedule(MightyMinerConfig.devMKillTimer);
    }

    switch (this.state) {
      case STARTING:
        this.targetMob.ifPresent(this.mobQueue::add);
        this.changeState(State.FINDING_MOB, 0);
        this.recheckTimer.reset();
        log("Starting");
      case FINDING_MOB:
        if (!this.recheckTimer.isScheduled() || this.recheckTimer.passed()) {
          List<EntityLivingBase> mobs = EntityUtil.getEntities(this.mobToKill, this.mobQueue);
          if (mobs.isEmpty()) {
            if (!this.shutdownTimer.isScheduled()) {
              log("Cannot find mobs. Starting a 10 second timer");
              this.shutdownTimer.schedule(10_000);
            }
            return;
          } else if (this.shutdownTimer.isScheduled()) {
            this.shutdownTimer.reset();
          }

          EntityLivingBase best = null;
          for (EntityLivingBase mob : mobs) {
            BlockPos mobPos = EntityUtil.getBlockStandingOn(mob);
            if (BlockUtil.canStandOn(mobPos)) {
              best = mob;
              break;
            }
          }
          if (best == null) {
            log("Didnt find a mob that has a valid position. ");
            this.changeState(State.STARTING, 0);
            return;
          }
          if (!this.targetMob.isPresent() || !this.targetMob.get().equals(best)) {
            this.targetMob = Optional.of(best);
            this.entityLastPosition = Optional.of(best.getPositionVector());
            Pathfinder.getInstance().stopAndRequeue(EntityUtil.getBlockStandingOn(this.targetMob.get()));
          }
          this.recheckTimer.schedule(MightyMinerConfig.devMKillTimer);
        }

        if (!this.targetMob.isPresent() || !this.entityLastPosition.isPresent()) {
          this.stop(MKError.NO_ENTITIES);
          log("no target mob || no last position saved"); // idk why this would happen but better safe than sorry (im schizophrenic)
          return;
        }

        if (!Pathfinder.getInstance().isRunning()) {
          log("Pathfinder wasnt enabled. starting");
          Pathfinder.getInstance().setSprintState(MightyMinerConfig.commMobKillerSprint);
          Pathfinder.getInstance().setInterpolationState(MightyMinerConfig.commMobKillerInterpolate);
          Pathfinder.getInstance().start();
        }

        if (PlayerUtil.getNextTickPosition().squareDistanceTo(this.targetMob.get().getPositionVector()) < 8 && mc.thePlayer.canEntityBeSeen(this.targetMob.get())) { // 8 cuz why not
          log("Looking at mob");
          this.changeState(State.LOOKING_AT_MOB, 0);
          return;
        }

        if (!this.targetMob.get().isEntityAlive()) {
          Pathfinder.getInstance().stop();
          this.changeState(State.STARTING, 0);
          log("Targetmob isnt alive");
          return;
        }

        if (this.targetMob.get().getPositionVector().squareDistanceTo(this.entityLastPosition.get()) > 9) {
          if (++this.pathRetry > 3) {
            this.changeState(State.STARTING, 0);
            log("target mob moved away. repathing");
//            Pathfinder.getInstance().stop();
            this.pathRetry = 0;
            return;
          }
          log("Repathing");
          this.entityLastPosition = Optional.of(this.targetMob.get().getPositionVector());
          Pathfinder.getInstance().stopAndRequeue(EntityUtil.getBlockStandingOn(this.targetMob.get()));
          return;
        }

        if (!Pathfinder.getInstance().isRunning()) {
          log("Pathfinder not enabled");
          this.changeState(State.STARTING, 0);
          return;
        }
        break;
      case LOOKING_AT_MOB:
        if (!Pathfinder.getInstance().isRunning()) {
          RotationHandler.getInstance().easeTo(new RotationConfiguration(new Target(this.targetMob.get()), 400, null));
          this.changeState(State.KILLING_MOB, 0);

          log("Rotating");
        }
      case KILLING_MOB:
        if (!Objects.equals(mc.objectMouseOver.entityHit, this.targetMob.get())) {
          if (mc.thePlayer.getDistanceSqToEntity(this.targetMob.get()) < 9 && Pathfinder.getInstance().isRunning()) {
            Pathfinder.getInstance().stop();
            return;
          }
          if (!Pathfinder.getInstance().isRunning() && !RotationHandler.getInstance().isEnabled()) {
            this.changeState(State.STARTING, 0);
//            this.debug.put(this.targetMob.get(), new Pair<>("NPNR", walkPos));
          }
          return;
        }

        KeyBindUtil.leftClick();
        RotationHandler.getInstance().stop();
        this.changeState(State.STARTING, 0);
        break;
    }
  }

  @SubscribeEvent
  protected void onRender(RenderWorldLastEvent event) {
//    debug.forEach((a, b) -> {
//      RenderUtil.drawText(b.getFirst(), a.posX, a.posY + a.height, a.posZ, 1);
//      if (b.getSecond() != null) {
//        RenderUtil.drawBlock(b.getSecond(), new Color(255, 0, 241, 150));
//      }
//    });
    if (!this.enabled || !this.targetMob.isPresent()) {
      return;
    }

    RenderUtil.drawBox(this.targetMob.get().getEntityBoundingBox(), new Color(255, 255, 255, 200));

  }

  private void changeState(State state, int time) {
    this.state = state;
    this.timer.schedule(time);
  }

  enum State {
    STARTING, FINDING_MOB, WALKING_TO_MOB, WAITING_FOR_MOB, LOOKING_AT_MOB, KILLING_MOB,
  }

  public enum MKError {
    NONE, NO_ENTITIES
  }
}
