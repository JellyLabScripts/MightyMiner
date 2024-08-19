package com.jelly.mightyminerv2.Feature.impl;

import com.jelly.mightyminerv2.Config.MightyMinerConfig;
import com.jelly.mightyminerv2.Feature.IFeature;
import com.jelly.mightyminerv2.Handler.RotationHandler;
import com.jelly.mightyminerv2.Util.CommissionUtil;
import com.jelly.mightyminerv2.Util.KeyBindUtil;
import com.jelly.mightyminerv2.Util.PlayerUtil;
import com.jelly.mightyminerv2.Util.RenderUtil;
import com.jelly.mightyminerv2.Util.helper.Clock;
import com.jelly.mightyminerv2.Util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.Util.helper.Target;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import javax.xml.xpath.XPathException;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import org.apache.commons.io.comparator.PathFileComparator;

// Todo: Make it a universal mob killer perhaps?
//  idk its not a combat macro
public class AutoMobKiller implements IFeature {

  @Getter
  private static final AutoMobKiller instance = new AutoMobKiller();

  private Minecraft mc = Minecraft.getMinecraft();
  private boolean enabled = false;
  private State state = State.STARTING;
  private MKError mkError = MKError.NONE;
  private Clock timer = new Clock();
  private Clock shutdownTimer = new Clock();
  private Clock queueTimer = new Clock();
  private Set<EntityPlayer> mobQueue = new HashSet<>();
  private Optional<EntityPlayer> targetMob = Optional.empty();
  private Optional<Vec3> entityLastPosition = Optional.empty();
  private String mobToKill = "";
  private byte killAttempts = 0;

  @Override
  public String getName() {
    return "MobKiller";
  }

  @Override
  public boolean isEnabled() {
    return this.enabled;
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
  }

  @Override
  public void stop() {
    if (!this.enabled) {
      return;
    }
    this.enabled = false;
    this.mobToKill = "";
    this.killAttempts = 0;
    this.targetMob = Optional.empty();
    this.timer.reset();
    this.shutdownTimer.reset();
    this.resetStatesAfterStop();
    Pathfinder.getInstance().stop();

    log("stopped");
  }

  public void enable(String mobToKill) {
    this.mobToKill = mobToKill;
    this.mkError = MKError.NONE;
    this.enabled = true;

    log("Started");
  }

  public void stop(MKError error) {
    this.mkError = error;
    this.stop();
  }

  public boolean succeeded() {
    return !this.enabled && this.mkError == MKError.NONE;
  }

  public MKError getMkError() {
    return this.mkError;
  }

  @Override
  public void resetStatesAfterStop() {
    this.state = State.STARTING;
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

    if (this.shutdownTimer.isScheduled() && this.shutdownTimer.passed()) {
      this.stop(MKError.NO_ENTITIES);
      log("Entities did not spawn");
      return;
    }

    if (this.queueTimer.passed()) {
      log("Queue celared");
      this.mobQueue.clear();
      this.queueTimer.schedule(2000);
    }

    switch (this.state) {
      case STARTING:
        this.targetMob.ifPresent(this.mobQueue::add);
        this.changeState(State.FINDING_MOB, 0);
        log("Starting");
      case FINDING_MOB:
        List<EntityPlayer> mobs = CommissionUtil.getMobList(this.mobToKill, this.mobQueue);
        if (mobs.isEmpty()) {
          if (!this.shutdownTimer.isScheduled()) {
            log("Cannot find mobs. Starting a 10 second timer");
            this.shutdownTimer.schedule(10_000);
          }
          return;
        } else if (this.shutdownTimer.isScheduled()) {
          this.shutdownTimer.reset();
        }

        EntityPlayer best = mobs.get(0);
        this.targetMob = Optional.ofNullable(best);
        this.entityLastPosition = Optional.of(best.getPositionVector());
        this.changeState(State.WALKING_TO_MOB, 0);
        break;
      case WALKING_TO_MOB:
        if (!this.targetMob.isPresent() || !this.entityLastPosition.isPresent()) {
          this.stop(MKError.NO_ENTITIES);
          log("no target mob || no last position saved"); // idk why this would happen but better safe than sorry (im schizophrenic)
          return;
        }
        Vec3 mobPos = this.targetMob.get().getPositionVector();

        Pathfinder.getInstance().queue(new BlockPos(mobPos.xCoord, Math.ceil(mobPos.yCoord) - 1, mobPos.zCoord));
        log("Queued new to " + mobPos);
        if (!Pathfinder.getInstance().isEnabled()) {
          log("Pathfinder wasnt enabled. starting");
          Pathfinder.getInstance().setSprintState(MightyMinerConfig.commMobKillerSprint);
          Pathfinder.getInstance().setInterpolationState(MightyMinerConfig.commMobKillerInterpolate);
          Pathfinder.getInstance().start();
        }
        this.changeState(State.WAITING_FOR_MOB, 0);
        break;
      case WAITING_FOR_MOB:
        // next tick pos makes hits more accurate
        if (PlayerUtil.getNextTickPosition().squareDistanceTo(this.targetMob.get().getPositionVector()) < 8) { // 8 cuz why not
          this.changeState(State.LOOKING_AT_MOB, 0);
          return;
        }

        if (!this.targetMob.get().isEntityAlive()) {
          Pathfinder.getInstance().stop();
          this.changeState(State.STARTING, 0);
          return;
        }

        if (this.targetMob.get().getPositionVector().squareDistanceTo(this.entityLastPosition.get()) > 9) {
          this.changeState(State.STARTING, 0);
          log("target mob is far away. repathing");
          Pathfinder.getInstance().stop();
          return;
        }

        if (!Pathfinder.getInstance().isEnabled()) {
          log("Pathfinder not enabled");
          this.changeState(State.STARTING, 0);
          return;
        }
        break;
      case LOOKING_AT_MOB:
        if (!Pathfinder.getInstance().isEnabled()) {
          RotationHandler.getInstance().easeTo(new RotationConfiguration(new Target(this.targetMob.get()), 400, null));
          this.changeState(State.KILLING_MOB, 0);

          log("Rotating");
        }
      case KILLING_MOB:
        if (!Objects.equals(mc.objectMouseOver.entityHit, this.targetMob.get())) {
          if (mc.thePlayer.getDistanceSqToEntity(this.targetMob.get()) < 9 && Pathfinder.getInstance().isEnabled()) {
            Pathfinder.getInstance().stop();
            return;
          }
          if (!Pathfinder.getInstance().isEnabled() && !RotationHandler.getInstance().isEnabled()) {
            this.changeState(State.STARTING, 0);
          }
          return;
        }

        KeyBindUtil.leftClick();
        RotationHandler.getInstance().reset();
        this.changeState(State.STARTING, 0);
        break;
    }
  }

  @SubscribeEvent
  public void onRender(RenderWorldLastEvent event) {
    if (!this.isEnabled() || !this.targetMob.isPresent()) {
      return;
    }
    Vec3 pos = this.targetMob.get().getPositionVector();

    RenderUtil.drawBox(new AxisAlignedBB(pos.xCoord - 0.5, pos.yCoord, pos.zCoord - 0.5, pos.xCoord + 0.5, pos.yCoord + 2, pos.zCoord + 0.5),
        new Color(255, 0, 241, 150));
  }

  private void changeState(State state, int time) {
    this.state = state;
    this.timer.schedule(time);
  }

  private boolean isTimerRunning() {
    return this.timer.isScheduled() && !this.timer.passed();
  }

  enum State {
    STARTING, FINDING_MOB, WALKING_TO_MOB, WAITING_FOR_MOB, LOOKING_AT_MOB, KILLING_MOB,
//    STALLING, LOOKING - Todo: make this
  }

  public enum MKError {
    NONE, NO_ENTITIES
  }
}
