package com.jelly.MightyMinerV2.Feature.impl;

import com.jelly.MightyMinerV2.Config.MightyMinerConfig;
import com.jelly.MightyMinerV2.Event.BlockChangeEvent;
import com.jelly.MightyMinerV2.Feature.IFeature;
import com.jelly.MightyMinerV2.Handler.RotationHandler;
import com.jelly.MightyMinerV2.MightyMiner;
import com.jelly.MightyMinerV2.Util.*;
import com.jelly.MightyMinerV2.Util.helper.Angle;
import com.jelly.MightyMinerV2.Util.helper.Clock;
import com.jelly.MightyMinerV2.Util.helper.RotationConfiguration;
import com.jelly.MightyMinerV2.Util.helper.Target;
import java.awt.Color;
import java.util.Comparator;
import java.util.stream.Collectors;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import scala.reflect.internal.ReificationSupport.ReificationSupportImpl.MaybeTypeTreeOriginal$;

public class MithrilMiner implements IFeature {

  private static MithrilMiner instance;

  public static MithrilMiner getInstance() {
    if (instance == null) {
      instance = new MithrilMiner();
    }
    return instance;
  }

  private final Minecraft mc = Minecraft.getMinecraft();
  private final Random random = new Random();
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
  private BlockPos blockToWalkTo = null;
  @Getter
  private float walkDirection = 0f;

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
    this.blockToWalkTo = null;
    KeyBindUtil.releaseAllExcept();
    this.resetStatesAfterStop();

    success("Disabled");
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

    success("Enabled");
  }

  @SubscribeEvent
  public void onTick(final TickEvent.ClientTickEvent event) {
    if (mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null
        || !this.isRunning()) {
      return;
    }

    switch (this.state) {
      case STARTING:
        this.swapState(State.CHOOSING_BLOCK, 0);

        if (this.boostState == BoostState.AVAILABLE) {
          this.swapState(State.SPEED_BOOST, 250);
          KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, false);
        }

        log("Starting");
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
        // player is probably falling
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
        final int boostMult = this.boostState == BoostState.ACTIVE ? this.speedBoost / 100 : 1;
        this.miningTime =
            BlockUtil.getMiningTime(this.targetBlock, this.miningSpeed * boostMult) * 2;
        success(
            "Hardness: " + BlockUtil.getBlockStrength(this.targetBlock) + ", Time: " + miningTime);

        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, true);
        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak,
            MightyMinerConfig.mithrilMinerSneakWhileMining
        );
        this.swapState(State.ROTATING, 0);

        log("State: Choosing Block. Chosen Block Was: " + this.targetBlock);
        // fall through
      case ROTATING:
        final List<Vec3> points = BlockUtil.bestPointsOnBestSide(this.targetBlock);
        // Should never happen idk
        if (points.isEmpty()) {
          this.swapState(State.STARTING, 0);
          log("Cannot find points to look at");
          break;
        }

        RotationHandler.getInstance().reset();
        RotationHandler.getInstance().queueRotation(
            new RotationConfiguration(
                new Target(points.get(0)),
                this.getRandomRotationTime(),
                RotationConfiguration.RotationType.CLIENT,
                null));

        if (this.random.nextBoolean()) {
          RotationHandler.getInstance().queueRotation(
              new RotationConfiguration(new Target(points.get(points.size() - 1)),
                  this.getRandomRotationTime() * 2L,
                  RotationConfiguration.RotationType.CLIENT,
                  null));
        }
        RotationHandler.getInstance().start();

        if (mc.thePlayer.getPositionEyes(1)
            .distanceTo(new Vec3(this.targetBlock).addVector(0.5, 0.5, 0.5)) > 4) {
          this.swapState(State.WALKING, 1000);
          this.blockToWalkTo = BlockUtil.getWalkableBlocksAround(
                  PlayerUtil.getBlockStandingOnFloor()
              ).stream()
              .min(Comparator.comparingDouble(this.targetBlock::distanceSq))
              .orElse(null);
        } else {
          this.swapState(State.BREAKING, 1000);
        }

        log("Rotation Started");
        // fall through
      case WALKING:
        if (this.blockToWalkTo != null && this.blockToWalkTo.distanceSqToCenter(
            mc.thePlayer.posX,
            mc.thePlayer.posY - 0.5,
            mc.thePlayer.posZ
        ) > 0.01) {
          this.walkDirection = AngleUtil.getRotation(this.blockToWalkTo).getYaw();

          KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, true);
          KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, true);
        } else {
          this.blockToWalkTo = null;
          this.swapState(State.BREAKING, (int) this.timer.getRemainingTime());
          KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, false);
          KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak,
              MightyMinerConfig.mithrilMinerSneakWhileMining
          );
        }

        log("Started Walk. Dist to travel: " +
            (this.blockToWalkTo != null ? this.blockToWalkTo.distanceSqToCenter(
                mc.thePlayer.posX,
                mc.thePlayer.posY - 0.5,
                mc.thePlayer.posZ
            ) : "NONE"));
        // fall through
      case BREAKING:
        if (!Objects.equals(BlockUtil.getBlockLookingAt(), this.targetBlock)) {
          if (this.hasTimerEnded()) {
            log("Could not look at block!");
            this.swapState(State.STARTING, 0);
          }
          break;
        }
        if (--this.miningTime <= 0) {
          this.swapState(State.STARTING, 0);

          KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, false);
          KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak,
              MightyMinerConfig.mithrilMinerSneakWhileMining
          );
        }

        log("Breaking Block");
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
      RenderUtil.drawBlockBox(this.targetBlock, new Color(0, 255, 255, 50));
    }
    if (this.blockToWalkTo != null) {
      RenderUtil.drawBlockBox(this.blockToWalkTo, new Color(0, 255, 0, 50));
    }
  }

  private void swapState(final State toState, final int delay) {
    this.state = toState;
    this.timer.schedule(delay);
  }

  private boolean hasTimerEnded() {
    return this.timer.isScheduled() && this.timer.passed();
  }

  private int getRandomRotationTime() {
    return MightyMinerConfig.mithrilMinerRotationTime + (int) (this.random.nextFloat()
        * MightyMinerConfig.mithrilMinerRotationTimeRandomizer);
  }

  // DO NOT REARRANGE THIS
  enum State {
    STARTING, SPEED_BOOST, CHOOSING_BLOCK, ROTATING, WALKING, BREAKING
  }

  enum BoostState {
    AVAILABLE, ACTIVE, INACTIVE,
  }
}
