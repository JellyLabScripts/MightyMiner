package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.event.BlockChangeEvent;
import com.jelly.mightyminerv2.event.BlockDestroyEvent;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.util.*;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.Target;
import java.awt.Color;
import java.util.Comparator;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;
import java.util.Random;

// Code here is terrible
// Help me fix it or fix it pls
public class MithrilMiner extends AbstractFeature {

  private static MithrilMiner instance;

  public static MithrilMiner getInstance() {
    if (instance == null) {
      instance = new MithrilMiner();
    }
    return instance;
  }

  private static BoostState boostState = BoostState.UNKNOWN;

  private final Random random = new Random();
  private State state = State.STARTING;
  private MithrilError mithrilError = MithrilError.NONE;
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
  public void onDisable() {
    if (!this.enabled) {
      return;
    }

    this.enabled = false;
    this.miningSpeed = 200;
    this.miningTime = 0;
    this.speedBoost = 0;
    this.destBlock = null;
    this.targetPoint = null;
    KeyBindUtil.releaseAllExcept();
    StrafeUtil.enabled = false;
    this.resetStatesAfterStop();

    send("Disabled");
  }

  @Override
  public void resetStatesAfterStop() {
    this.timer.reset();
    this.state = State.STARTING;
    RotationHandler.getInstance().reset();
  }

  // No TickGlide
  public void start(final int[] priority) {
    this.start(200, 100, priority);
  }

  public void start(final int miningSpeed, final int speedBoost, final int[] priority) {
    this.miningSpeed = miningSpeed;
    this.speedBoost = speedBoost;
    this.priority = priority;
    this.enabled = true;
    this.mithrilError = MithrilError.NONE;

    this.start();
    send("Enabled");
  }

  public void stop(MithrilError error) {
    this.mithrilError = error;
    this.stop();
  }

  public boolean hasSucceeded() {
    return !this.enabled && this.mithrilError == MithrilError.NONE;
  }

  public MithrilError getMithrilError() {
    return this.mithrilError;
  }

  public static BoostState getBoostState() {
    return boostState;
  }

  @Override
  protected void onTick(TickEvent.ClientTickEvent event) {
//    if (mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null || !this.isRunning()) {
//      return;
//    }

    if (mc.currentScreen != null) {
      return;
    }

    switch (this.state) {
      case STARTING:
        this.swapState(State.CHOOSING_BLOCK, 0);

        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, true);
        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, false);
        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, MightyMinerConfig.mithrilMinerSneakWhileMining);

        // Unknown or Available
        if (boostState.ordinal() <= 1) {
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
        boostState = BoostState.INACTIVE;
        break;
      case CHOOSING_BLOCK:
        List<BlockPos> blocks;
        if (!MightyMinerConfig.mithrilStrafe || (blocks = BlockUtil.getBestMithrilBlocksAround(priority, this.targetBlock)).isEmpty()) {
          blocks = BlockUtil.getBestMithrilBlocks(priority, this.targetBlock);
        }

        if (blocks.size() < 2) {
          error("Cannot find enough mithril blocks to mine.");
          this.stop(MithrilError.NOT_ENOUGH_BLOCKS);
          return;
        }

        this.targetBlock = blocks.get(blocks.get(0).equals(this.targetBlock) ? 1 : 0);
        final int boostMultiplier = boostState == BoostState.ACTIVE ? this.speedBoost / 100 : 1;
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
                null)
        );

        if (this.random.nextBoolean()) {
          final int halfwayMark = points.size() / 2;
          this.targetPoint = points.get(random.nextInt(halfwayMark) + halfwayMark - 1);
          RotationHandler.getInstance().queueRotation(
              new RotationConfiguration(
                  new Target(targetPoint),
                  this.getRandomRotationTime() * 2L,
                  null)
          );
        }
        RotationHandler.getInstance().start();

        if (this.targetPoint != null && PlayerUtil.getPlayerEyePos().distanceTo(this.targetPoint) > 4) {
          this.swapState(State.WALKING, 0);
          this.destBlock = BlockUtil.getWalkableBlocksAround(PlayerUtil.getBlockStandingOn())
              .stream()
              .min(Comparator.comparingDouble(this.targetBlock::distanceSq))
              .orElse(null);
        } else {
          this.swapState(State.BREAKING, 0);
          break;
        }
        log("Rotation Started");
        // fall through
      case WALKING:
        boolean walk = false;
        boolean sneak = MightyMinerConfig.mithrilMinerSneakWhileMining;

        if (this.destBlock != null
            && this.destBlock.distanceSqToCenter(mc.thePlayer.posX, this.destBlock.getY() + 0.5, mc.thePlayer.posZ) > 0.04
            && this.targetPoint.distanceTo(PlayerUtil.getPlayerEyePos()) > 3.5) {
          StrafeUtil.enabled = true;
          StrafeUtil.yaw = AngleUtil.get360RotationYaw(AngleUtil.getRotation(this.destBlock).getYaw());
          walk = sneak = true;
        } else {
          this.destBlock = null;
          StrafeUtil.enabled = false;
          this.swapState(State.BREAKING, 0);
        }

        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, walk);
        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, sneak);

        log("Started Walk. Walk: " + walk + ", Sneak: " + sneak);
        // fall through
      case BREAKING:
        if (!this.targetBlock.equals(BlockUtil.getBlockLookingAt())) {
          if (this.state == State.BREAKING && !RotationHandler.getInstance().isEnabled()) {
            this.swapState(State.STARTING, 0);
          }
          break;
        }

        // i forgot what this is for :sob:
        if (++this.breakAttemptTime > 60) {
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

  @Override
  protected void onBlockChange(BlockChangeEvent event) {
    if (this.state.ordinal() < State.ROTATING.ordinal()) {
      return;
    }
    if (event.pos.equals(this.targetBlock)) {
      RotationHandler.getInstance().reset();
      this.swapState(State.STARTING, 0);
    }
  }

  @SubscribeEvent
  public void onBlockDestroy(BlockDestroyEvent event) {
    if (!event.getBlock().equals(this.targetBlock)) {
      return;
    }

    this.breakAttemptTime = 0;
  }

  @Override
  protected void onChat(String message) {
    if (message.equals("Mining Speed Boost is now available!")) {
      boostState = BoostState.AVAILABLE;
    }
    if (message.contains("You used your Mining Speed Boost Pickaxe Ability!")) {
      boostState = BoostState.ACTIVE;
    }
    if (message.equals("Your Mining Speed Boost has expired!")
        || (boostState != BoostState.ACTIVE && message.startsWith("This ability is on cooldown for"))) {
      boostState = BoostState.INACTIVE;
    }
  }

  @Override
  protected void onRender(RenderWorldLastEvent event) {
    if (this.targetBlock != null) {
      RenderUtil.drawBlockBox(this.targetBlock, new Color(0, 255, 255, 100));
    }
    if (this.destBlock != null) {
      RenderUtil.drawBlockBox(this.destBlock, new Color(0, 255, 0, 100));
    }
  }

  private void swapState(final State toState, final int delay) {
    this.state = toState;
    this.timer.schedule(delay);
  }

  private int getRandomRotationTime() {
    return MightyMinerConfig.mithrilMinerRotationTime + (int) (this.random.nextFloat() * MightyMinerConfig.mithrilMinerRotationTimeRandomizer);
  }

  // DO NOT REARRANGE THIS
  enum State {
    STARTING, SPEED_BOOST, CHOOSING_BLOCK, ROTATING, WALKING, BREAKING
  }

  enum BoostState {
    UNKNOWN, AVAILABLE, ACTIVE, INACTIVE,
  }

  public enum MithrilError {
    NONE, NOT_ENOUGH_BLOCKS
  }
}
