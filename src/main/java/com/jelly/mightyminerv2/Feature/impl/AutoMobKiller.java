package com.jelly.mightyminerv2.Feature.impl;

import com.jelly.mightyminerv2.Feature.IFeature;
import com.jelly.mightyminerv2.Handler.RotationHandler;
import com.jelly.mightyminerv2.Util.AngleUtil;
import com.jelly.mightyminerv2.Util.CommissionUtil;
import com.jelly.mightyminerv2.Util.KeyBindUtil;
import com.jelly.mightyminerv2.Util.LogUtil;
import com.jelly.mightyminerv2.Util.PathfindUtil;
import com.jelly.mightyminerv2.Util.RenderUtil;
import com.jelly.mightyminerv2.Util.helper.Clock;
import com.jelly.mightyminerv2.Util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.Util.helper.Target;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

// Todo: Make it a universal mob killer perhaps?
//  idk its not a combat macro
public class AutoMobKiller implements IFeature {

  @Getter
  private static final AutoMobKiller instance = new AutoMobKiller();

  private Minecraft mc = Minecraft.getMinecraft();
  private boolean enabled = false;
  private State state = State.STARTING;
  private Clock timer = new Clock();
  private Clock shutdownTimer = new Clock();
  private Optional<EntityPlayer> targetMob = Optional.empty();
  private Optional<Vec3> entityLastPosition = Optional.empty();
  private String mobToKill = "";

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
//    this.enabled = true;

//    log("Started");
  }

  @Override
  public void stop() {
    this.enabled = false;
    this.mobToKill = "";
    this.targetMob = Optional.empty();
    this.timer.reset();
    this.shutdownTimer.reset();
    this.resetStatesAfterStop();

    log("stopped");
  }

  public void enable(String mobToKill) {
    this.mobToKill = mobToKill;
    this.enabled = true;

    log("Started");
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
      this.stop();
      log("Entities did not spawn");
      return;
    }

    switch (this.state) {
      case STARTING:
        this.changeState(State.FINDING_MOB, 0);
        log("Starting");
        break;
      case FINDING_MOB:
//        List<EntityPlayer> mobs = CommissionUtil.getCommissionMobs(this.mobToKill);
        EntityPlayer bestMob = CommissionUtil.getBestMob(this.mobToKill, this.targetMob.orElse(null));
//        if(bestMob.isDead && bestMob.getHealth() <= 0.0f){
//          LogUtil.send("Mob is dead");
//          return;
//        }
        if (bestMob == null) {
          if (!this.shutdownTimer.isScheduled()) {
            log("Cannot find mobs. Starting the 10 second timer");
            this.shutdownTimer.schedule(10_000);
          }

          return;
        } else if(this.shutdownTimer.isScheduled()) {
          this.shutdownTimer.reset();
        }

        this.targetMob = Optional.of(bestMob);
        this.entityLastPosition = Optional.of(bestMob.getPositionVector());
        this.changeState(State.WALKING_TO_MOB, 0);
        break;
      case WALKING_TO_MOB:
        if (!this.targetMob.isPresent() || !this.entityLastPosition.isPresent()) {
          stop();
          log("no target mob || no last position saved"); // idk why this would happen but better safe than sorry (aka im schizophrenic)
          return;
        }
        Vec3 mobPos = this.targetMob.get().getPositionVector();
        PathfindUtil.goTo(MathHelper.floor_double(mobPos.xCoord), (int) Math.ceil(mobPos.yCoord) - 1, MathHelper.floor_double(mobPos.zCoord));
        this.changeState(State.WAITING_FOR_MOB, 0);
        break;
      case WAITING_FOR_MOB:
        if (mc.thePlayer.getDistanceSqToEntity(this.targetMob.get()) < 8) { // 8 cuz why not
          this.changeState(State.LOOKING_AT_MOB, 0);
          PathfindUtil.stop(); // Should I stop?
          return;
        }

        if(!this.targetMob.get().isEntityAlive()){
          this.changeState(State.FINDING_MOB, 0);
          return;
        }

        if (this.targetMob.get().getPositionVector().squareDistanceTo(this.entityLastPosition.get()) > 4) {
          if (mc.thePlayer.getDistanceSqToEntity(this.targetMob.get()) > 100) {
            this.changeState(State.WALKING_TO_MOB, 0);
          } else {
            this.changeState(State.STARTING, 0);
          }
          PathfindUtil.stop();
          return;
        }

        if (!PathfindUtil.isProcessingPath()) {
          stop();
          log("Not processing path. Stopping");
          return;
        }
        break;
      case LOOKING_AT_MOB:
        RotationHandler.getInstance().easeTo(new RotationConfiguration(new Target(this.targetMob.get()), 300, null));
        this.changeState(State.KILLING_MOB, 0);

        log("Rotating");
      case KILLING_MOB:
        if (!Objects.equals(mc.objectMouseOver.entityHit, this.targetMob.get())) {
          if (!RotationHandler.getInstance().isEnabled()) {
            this.changeState(State.STARTING, 0);
            log("Failed to rotate. restarting");
          }
          return;
        }

        KeyBindUtil.leftClick();
        this.changeState(State.STARTING, 0);
        break;
    }
  }

  @SubscribeEvent
  public void onRender(RenderWorldLastEvent event){
    if(!this.isEnabled() || !this.targetMob.isPresent()) return;
    Vec3 pos = this.targetMob.get().getPositionVector();

    RenderUtil.drawBox(new AxisAlignedBB(pos.xCoord - 0.5, pos.yCoord, pos.zCoord - 0.5, pos.xCoord + 0.5, pos.yCoord + 2, pos.zCoord + 0.5), new Color(255, 0, 241, 150));
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
}
