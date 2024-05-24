package com.jelly.MightyMinerV2.Feature.impl;

import com.jelly.MightyMinerV2.Config.MightyMinerConfig;
import com.jelly.MightyMinerV2.Event.BlockChangeEvent;
import com.jelly.MightyMinerV2.Feature.IFeature;
import com.jelly.MightyMinerV2.Handler.RotationHandler;
import com.jelly.MightyMinerV2.Util.*;
import com.jelly.MightyMinerV2.Util.LogUtil.ELogType;
import com.jelly.MightyMinerV2.Util.helper.Angle;
import com.jelly.MightyMinerV2.Util.helper.Clock;
import com.jelly.MightyMinerV2.Util.helper.RotationConfiguration;
import com.jelly.MightyMinerV2.Util.helper.Target;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class MithrilMiner implements IFeature {

  private static MithrilMiner instance;

  public static MithrilMiner getInstance() {
    if (instance == null) {
      instance = new MithrilMiner();
    }
    return instance;
  }

  private final Minecraft mc = Minecraft.getMinecraft();
  private final RotationConfiguration config = new RotationConfiguration(
      new Angle(0, 0), 500, RotationConfiguration.RotationType.CLIENT, null);

  @Getter
  private boolean enabled = false;
  private State state = State.STARTING;
  private BoostState boostState = BoostState.INACTIVE;
  private final Clock timer = new Clock();
  private int miningSpeed = 100;          // Mainly for TickGliding
  private int miningTime = 0;             // For TickGliding
  private int speedBoost = 0;             // Boost Percentage
  private int[] priority = {1, 1, 1, 1};  // Priority - [GrayWool, Prismarine, BlueWool, Titanium]
  private BlockPos targetBlock = null;
  private Vec3 targetPoint = null;
  @Getter
  private float walkDirection = 0f;
  private final Random random = new Random();

  @Override
  public String getName() {
    return "MithrilMiner";
  }

  @Override
  public boolean isRunning() {
    return this.enabled;
  }

  @Override
  public boolean shouldPauseMacroExecution() {
    return false;
  }

  @Override
  public boolean shouldStartAtLaunch() {
    return false;
  }

  public boolean shouldWalk() {
    return this.isRunning() && this.state == State.WALKING;
  }

  @Override
  public void start() {

  }

  @Override
  public void stop() {
    this.enabled = false;
    this.miningSpeed = 100;
    this.miningTime = 0;
    this.speedBoost = 0;
    this.walkDirection = 0f;
    this.targetPoint = null;
    KeyBindUtil.releaseAllExcept();
    this.resetStatesAfterStop();
  }

  @Override
  public void resetStatesAfterStop() {
    this.timer.reset();
    RotationHandler.getInstance().reset();
    this.state = State.STARTING;
    this.boostState = BoostState.INACTIVE;
  }

  @Override
  public boolean isToggle() {
    return false;
  }

  @Override
  public boolean shouldCheckForFailSafe() {
    return false;
  }

  // No TickGlide
  public void enable(final int[] priority) {
    this.enable(100, 100, priority);
  }

  public void enable(final int miningSpeed, final int speedBoost, final int[] priority) {
    this.miningSpeed = miningSpeed;
    this.speedBoost = speedBoost;
    this.priority = priority;

    this.enabled = true;
  }

  @SubscribeEvent
  public void onTick(final TickEvent.ClientTickEvent event) {
    if (mc.thePlayer == null || mc.theWorld == null || !this.isRunning()) {
      return;
    }

    switch (this.state) {
      case STARTING:
        // Add Tool Hold Mechanism Here; e.g: hold gauntlet or pickonimbus or something
        KeyBindUtil.releaseAllExcept(mc.gameSettings.keyBindAttack);
        this.swapState(State.CHOOSING_BLOCK, 0);

        if (this.boostState == BoostState.AVAILABLE) {
          this.swapState(State.SPEED_BOOST, 250);
          KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, false);
        }
        log("State: Starting");
        break;
      case SPEED_BOOST:
        if (!this.hasTimerEnded()) {
          return;
        }

        KeyBindUtil.rightClick();
        this.swapState(State.STARTING, 0);
        this.boostState = BoostState.ACTIVE;
        break;
      case CHOOSING_BLOCK:
        if (mc.theWorld.isAirBlock(PlayerUtil.getBlockStandingOn())) {
          return;
        }

        final List<BlockPos> blocks = BlockUtil.getBestMithrilBlocks(priority);

        if (blocks.size() < 2) {
          error("Cannot find enough mithril blocks to mine.");
          this.stop();
          return;
        }

        this.targetBlock = blocks.get(blocks.get(0).equals(this.targetBlock) ? 1 : 0);
        this.swapState(State.ROTATING, 0);

        log("State: Choosing Block. Chosen Block Was: " + this.targetBlock);
        break;
      case ROTATING:
        final List<Vec3> points = BlockUtil.bestPointsOnBestSide(this.targetBlock);
        // Should never happen idk
        if (points.isEmpty()) {
          this.swapState(State.STARTING, 0);
          log("Cannot find points to look at");
          return;
        }

        this.targetPoint = points.get(points.size() - 1);

        RotationHandler.getInstance().reset();
        RotationHandler.getInstance().queueRotation(
            new RotationConfiguration(
                new Target(points.get(0)),
                this.getRandomRotationTime(),
                RotationConfiguration.RotationType.CLIENT,
                null));
        if (this.random.nextBoolean()) {
          RotationHandler.getInstance().queueRotation(
              new RotationConfiguration(new Target(this.targetPoint),
                  1000,
                  RotationConfiguration.RotationType.CLIENT,
                  null));
        }
        RotationHandler.getInstance().start();
        this.swapState(State.VERIFYING_ROTATION, 0);
        break;
      case VERIFYING_ROTATION:
        log("State: Verifying Rotation");

        if (!Objects.equals(BlockUtil.getBlockLookingAt(), this.targetBlock)
            && RotationHandler.getInstance().isEnabled()) {
          return;
        }
        // fall through
      case BREAKING_BLOCK:
        // Ping issue might cause the block to respawn late
        if (!Objects.equals(BlockUtil.getBlockLookingAt(), this.targetBlock)) {
          this.swapState(State.STARTING, 0);
          return;
        }

        final int boostMult = this.boostState == BoostState.ACTIVE ? this.speedBoost / 100 : 1;
        this.miningTime =
            BlockUtil.getMiningTime(this.targetBlock, this.miningSpeed * boostMult) * 2;

        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, true);

        if (mc.thePlayer.getPositionEyes(1).distanceTo(this.targetPoint) < 4) {
          this.swapState(State.VERIFYING_BREAK, 0);
          break;
        }
        this.swapState(State.WALKING, 0);

        log("State: Breaking. MiningTime: " + this.miningTime + " ticks");
        // fall through
      case WALKING:
        if (mc.thePlayer.getPositionEyes(1).distanceTo(this.targetPoint) > 3) {
          this.walkDirection = AngleUtil.getRotation(this.targetBlock).getYaw();

          if (this.random.nextBoolean()) {
            KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, true);
          }
          KeyBindUtil.getNeededKeyPresses(this.walkDirection).forEach(
              it -> KeyBindUtil.setKeyBindState(it, true)
          );
        } else {
          this.swapState(State.VERIFYING_BREAK, 0);
          KeyBindUtil.releaseAllExcept(mc.gameSettings.keyBindAttack);
        }
        // fall through
      case VERIFYING_BREAK:
        if (--this.miningTime <= 0
            || !Objects.equals(BlockUtil.getBlockLookingAt(), this.targetBlock)) {
          this.swapState(State.STARTING, 0);
        }

        log("State: Verifying Break, MiningTime: " + this.miningTime);
        break;
    }
  }

  @SubscribeEvent
  public void blockChangeEvent(final BlockChangeEvent event) {
    if (!this.isRunning() || this.state.ordinal() < State.ROTATING.ordinal()) {
      return;
    }
    if (event.pos.equals(this.targetBlock)) {
      RotationHandler.getInstance().reset();
      this.swapState(State.STARTING, 0);
    }
  }

  @SubscribeEvent
  public void onRender(final RenderWorldLastEvent event) {
    if (!this.isRunning()) {
      return;
    }
    if (this.targetBlock != null) {
//      RenderUtil.drawBlockBox(this.targetBlock, new Color(0, 255, 255, 50));
    }
  }

  private void swapState(final State toState, final int delay) {
    this.state = toState;
    this.timer.schedule(delay);
  }

  private boolean hasTimerEnded() {
    return this.timer.isScheduled() && !this.timer.passed();
  }

  private int getRandomRotationTime() {
    return MightyMinerConfig.mithrilMinerRotationTime + (int) (this.random.nextFloat()
        * MightyMinerConfig.mithrilMinerRotationTimeRandomizer);
  }

  // DO NOT REARRANGE THIS
  enum State {
    STARTING, SPEED_BOOST, CHOOSING_BLOCK, ROTATING, VERIFYING_ROTATION, BREAKING_BLOCK, WALKING, VERIFYING_BREAK
  }

  enum BoostState {
    AVAILABLE, ACTIVE, INACTIVE,
  }
}
