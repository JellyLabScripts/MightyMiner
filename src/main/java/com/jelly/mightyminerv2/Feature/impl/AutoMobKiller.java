package com.jelly.mightyminerv2.Feature.impl;

import com.jelly.mightyminerv2.Feature.IFeature;
import com.jelly.mightyminerv2.Handler.RotationHandler;
import com.jelly.mightyminerv2.Util.AngleUtil;
import com.jelly.mightyminerv2.Util.KeyBindUtil;
import com.jelly.mightyminerv2.Util.helper.Clock;
import com.jelly.mightyminerv2.Util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.Util.helper.Target;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class AutoMobKiller implements IFeature {
  @Getter
  private static final AutoMobKiller instance = new AutoMobKiller();

  private Minecraft mc = Minecraft.getMinecraft();
  private boolean enabled = false;
  private State state = State.STALLING;
  private Clock timer = new Clock();
  private Optional<Entity> targetMob = Optional.empty();

  @Override
  public String getName() {
    return "MobKiller";
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean isRunning() {
    return this.enabled;
  }

  @Override
  public boolean shouldPauseMacroExecution() {
    return true;
  }

  @Override
  public boolean shouldStartAtLaunch() {
    return false;
  }

  @Override
  public void start() {
    this.enabled = true;

    log("Started");
  }

  @Override
  public void stop() {
    this.enabled = false;
    this.targetMob = Optional.empty();
    this.timer.reset();
    this.resetStatesAfterStop();

    log("stopped");
  }

  @Override
  public void resetStatesAfterStop() {
    this.state = State.STALLING;
  }

  @Override
  public boolean shouldCheckForFailsafe() {
    return true;
  }

  @SubscribeEvent
  public void onTick(ClientTickEvent event) {
    if (mc.thePlayer == null || mc.theWorld == null || !this.enabled) {
      return;
    }

    switch (this.state) {
      case STARTING:
        this.changeState(State.FINDING_MOB, 0);
        log("Starting");
        break;
      case FINDING_MOB:
        List<Entity> walkers = mc.theWorld.playerEntities.stream()
            .filter(entity -> entity.getName().contains("Ice Walker") && entity.isEntityAlive())
            .collect(Collectors.toList());
        if (walkers.isEmpty()) {
          stop();
          log("cannot find walkers");
          return;
        }

        this.targetMob = walkers.stream().min(Comparator.comparingDouble(
            entity -> AngleUtil.getNeededChange(AngleUtil.getPlayerAngle(), AngleUtil.getRotation(entity)).yaw + +mc.thePlayer.getDistanceSqToEntity(
                entity)));
        this.changeState(State.WALKING_TO_MOB, 0);
        break;
      case WALKING_TO_MOB:
        if (!this.targetMob.isPresent()) {
          stop();
          log("no target mob");
          return;
        }
        this.changeState(State.LOOKING_AT_MOB, 0);
        break;
      case LOOKING_AT_MOB:
        RotationHandler.getInstance().easeTo(new RotationConfiguration(new Target(this.targetMob.get()), 300, null));
        this.changeState(State.KILLING_MOB, 0);

        log("Rotating");
        break;
      case KILLING_MOB:
        if (!Objects.equals(mc.objectMouseOver.entityHit, this.targetMob.get())) {
          if (!RotationHandler.getInstance().isEnabled()) {
            stop();
            log("did not rotate in time");
          }
          return;
        }

        KeyBindUtil.leftClick();
        this.changeState(State.STARTING, 0);
        break;
      case STALLING:
        break;
      case LOOKING:
        break;
    }
  }

  private void changeState(State state, int time) {
    this.state = state;
    this.timer.schedule(time);
  }

  private boolean isTimerRunning() {
    return this.timer.isScheduled() && !this.timer.passed();
  }

  enum State {
    STARTING, FINDING_MOB, WALKING_TO_MOB, LOOKING_AT_MOB, KILLING_MOB, STALLING, LOOKING
  }
}
