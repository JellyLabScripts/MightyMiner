package com.jelly.MightyMinerV2.Feature.impl;

import com.jelly.MightyMinerV2.Config.MightyMinerConfig;
import com.jelly.MightyMinerV2.Event.BlockChangeEvent;
import com.jelly.MightyMinerV2.Event.BlockDestroyEvent;
import com.jelly.MightyMinerV2.Feature.IFeature;
import com.jelly.MightyMinerV2.Handler.RotationHandler;
import com.jelly.MightyMinerV2.Util.*;
import com.jelly.MightyMinerV2.Util.helper.Clock;
import com.jelly.MightyMinerV2.Util.helper.RotationConfiguration;
import com.jelly.MightyMinerV2.Util.helper.Target;
import java.awt.Color;
import java.util.Comparator;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;
import java.util.Random;

// Code here is terrible
// Help me fix it or fix it pls
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
  @Getter
  private float walkDirection = 0f;
  private State state = State.STARTING;
  private BoostState boostState = BoostState.INACTIVE;
  private final Clock timer = new Clock();
  private int miningSpeed = 200;          // Mainly for TickGliding
  private int miningTime = 0;             // For TickGliding
  private int speedBoost = 0;             // Boost Percentage
  private int[] priority = {1, 1, 1, 1};  // Priority - [GrayWool, Prismarine, BlueWool, Titanium]
  private BlockPos targetBlock = null;
  private Vec3 targetPoint = null;
  private BlockPos destBlock = null;
  private int breakAttemptTime = 0;

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
    this.miningSpeed = 200;
    this.miningTime = 0;
    this.speedBoost = 0;
    this.walkDirection = 0f;
    this.destBlock = null;
    this.targetPoint = null;
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
    this.enable(200, 100, priority);
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
    if (mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null || !this.isRunning()) {
      return;
    }

    switch (this.state) {
      case STARTING:
        this.swapState(State.CHOOSING_BLOCK, 0);

        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, true);
        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, false);
        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, MightyMinerConfig.mithrilMinerSneakWhileMining);

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
        final List<BlockPos> blocks = BlockUtil.getBestMithrilBlocks(priority);
        if (blocks.size() < 2) {
          error("Cannot find enough mithril blocks to mine.");
          this.stop();
          return;
        }

        this.targetBlock = blocks.get(blocks.get(0).equals(this.targetBlock) ? 1 : 0);
        final int boostMultiplier = this.boostState == BoostState.ACTIVE ? this.speedBoost / 100 : 1;
        this.miningTime = BlockUtil.getMiningTime(this.targetBlock, this.miningSpeed * boostMultiplier) * 2;
        this.swapState(State.ROTATING, 0);

        log("State: Choosing Block. Chosen Block Was: " + this.targetBlock);
        // fall through
      case ROTATING:
        final List<Vec3> points = BlockUtil.bestPointsOnBestSide(this.targetBlock);
        if (points.isEmpty()) {
          this.swapState(State.STARTING, 0);
          log("Cannot find points to look at");
          break;
        }

        this.targetPoint = points.get(0);

        RotationHandler.getInstance().reset();
        RotationHandler.getInstance().queueRotation(
            new RotationConfiguration(
                new Target(targetPoint),
                this.getRandomRotationTime(),
                RotationConfiguration.RotationType.CLIENT,
                null)
        );

        if (this.random.nextBoolean()) {
          this.targetPoint = points.get(points.size() - 1);
          RotationHandler.getInstance().queueRotation(
              new RotationConfiguration(
                  new Target(targetPoint),
                  this.getRandomRotationTime() * 2L,
                  RotationConfiguration.RotationType.CLIENT,
                  null)
          );
        }
        RotationHandler.getInstance().start();

        if (this.targetPoint != null && mc.thePlayer.getPositionEyes(1).distanceTo(this.targetPoint) > 4) {
          this.swapState(State.WALKING, 1000);
          this.destBlock = BlockUtil.getWalkableBlocksAround(PlayerUtil.getBlockStandingOnFloor()).stream()
              .min(Comparator.comparingDouble(this.targetBlock::distanceSq)).orElse(null);
        } else {
          this.swapState(State.BREAKING, 1000);
          break;
        }
        log("Rotation Started");
        // fall through
      case WALKING:
        boolean walk = false;
        boolean sneak = MightyMinerConfig.mithrilMinerSneakWhileMining;

        if (this.destBlock != null && this.destBlock.distanceSqToCenter(mc.thePlayer.posX, this.destBlock.getY() + 0.5, mc.thePlayer.posZ) > 0.04) {
          this.walkDirection = AngleUtil.getRotation(this.destBlock).getYaw();
          walk = sneak = true;
        } else {
          this.destBlock = null;
          this.swapState(State.BREAKING, (int) this.timer.getRemainingTime());
        }

        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, walk);
        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, sneak);

        log("Started Walk. Walk: " + walk + ", Sneak: " + sneak);
        // fall through
      case BREAKING:
        if (!this.targetBlock.equals(BlockUtil.getBlockLookingAt())) {
          if (this.hasTimerEnded()) {
            this.swapState(State.STARTING, 0);
          }
          break;
        }

        if (this.breakAttemptTime > 60) {
          this.swapState(State.STARTING, 0);
          this.breakAttemptTime = 0;
        }

        if (--this.miningTime <= 0) {
          this.swapState(State.STARTING, 0);
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
  public void blockDestroyEvent(final BlockDestroyEvent event) {
    if (!this.isRunning() || !event.getBlock().equals(this.targetBlock)) {
      return;
    }

    this.breakAttemptTime = 0;
  }

  @SubscribeEvent
  public void onRender(final RenderWorldLastEvent event) {
    if (!this.isRunning()) {
      return;
    }
    if (this.targetBlock != null) {
      RenderUtil.drawBlockBox(this.targetBlock, new Color(0, 255, 255, 50));
    }
    if (this.destBlock != null) {
      RenderUtil.drawBlockBox(this.destBlock, new Color(0, 255, 0, 50));
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
    return MightyMinerConfig.mithrilMinerRotationTime + (int) (this.random.nextFloat() * MightyMinerConfig.mithrilMinerRotationTimeRandomizer);
  }

  // DO NOT REARRANGE THIS
  enum State {
    STARTING, SPEED_BOOST, CHOOSING_BLOCK, ROTATING, WALKING, BREAKING
  }

  enum BoostState {
    AVAILABLE, ACTIVE, INACTIVE,
  }
}
