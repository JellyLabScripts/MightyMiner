package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.event.BlockChangeEvent;
import com.jelly.mightyminerv2.event.BlockDestroyEvent;
import com.jelly.mightyminerv2.event.SpawnParticleEvent;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.util.*;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.Target;
import java.awt.Color;
import java.util.Comparator;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
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
  private Vec3 destBlock = null;
  private int breakAttemptTime = 0;
  private final Clock shiftTimer = new Clock();

  @Override
  public String getName() {
    return "MithrilMiner";
  }

  @Override
  public void stop() {
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
    RotationHandler.getInstance().stop();
  }

  // No TickGlide
  public void start(final int[] priority) {
    this.start(200, 100, priority, "");
  }

  public void start(final int miningSpeed, final int speedBoost, final int[] priority, String miningTool) {
    // this should never happen
    if (!miningTool.isEmpty() && !InventoryUtil.holdItem(miningTool)) {
      error("Cannot hold " + miningTool);
      this.stop(MithrilError.NOT_ENOUGH_BLOCKS);
      return;
    }
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

  @SubscribeEvent
  protected void onTick(TickEvent.ClientTickEvent event) {
    if (!this.enabled || mc.currentScreen != null) {
      return;
    }

    if (this.shiftTimer.isScheduled() && this.shiftTimer.passed()) {
      KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, MightyMinerConfig.mithrilMinerSneakWhileMining);
      this.shiftTimer.reset();
    }

    switch (this.state) {
      case STARTING:
        this.swapState(State.CHOOSING_BLOCK, 0);

//        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, true);
//        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, false);
//        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, MightyMinerConfig.mithrilMinerSneakWhileMining);

        // Unknown or Available
        if (boostState.ordinal() <= 1) {
          this.swapState(State.SPEED_BOOST, 250);
          KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, false);
        }

        log("Starting. stopped walking");
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

        // fall through
      case ROTATING:
        final List<Vec3> points = BlockUtil.bestPointsOnBestSide(this.targetBlock);
        if (points.isEmpty()) {
          this.swapState(State.STARTING, 0);
          log("Cannot find points to look at");
          break;
        }

        this.targetPoint = points.get(0);

        RotationHandler.getInstance().stop();
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
//          note("Should walk");
          Vec3 vec = AngleUtil.getVectorForRotation(AngleUtil.getRotationYaw(this.targetPoint));
          if (mc.theWorld.isAirBlock(new BlockPos(mc.thePlayer.getPositionVector().add(vec)))) {
            this.destBlock = BlockUtil.getWalkableBlocksAround(PlayerUtil.getBlockStandingOn())
                .stream()
                .min(Comparator.comparingDouble(this.targetBlock::distanceSq))
                .map(b -> new Vec3(b.getX() + 0.5, b.getY(), b.getZ() + 0.5))
                .orElse(null);
          }
        } else {
          this.swapState(State.BREAKING, 0);
        }
        // fall through
      case WALKING:
        if ((this.destBlock == null || Math.hypot(this.destBlock.xCoord - mc.thePlayer.posX, this.destBlock.zCoord - mc.thePlayer.posZ) > 0.2)
            && PlayerUtil.getPlayerEyePos().distanceTo(this.targetPoint) > 3.9) {
          this.shiftTimer.reset();
          StrafeUtil.enabled = true;
          StrafeUtil.yaw = AngleUtil.getRotationYaw360(this.destBlock == null ? this.targetPoint : this.destBlock);
          KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, true);
          KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, true);
          log("Dist: " + PlayerUtil.getPlayerEyePos().distanceTo(this.targetPoint) + ", EyePos: " + PlayerUtil.getPlayerEyePos() + ", Target: "
              + this.targetPoint);
        } else {
          this.destBlock = null;
          StrafeUtil.enabled = false;
          this.swapState(State.BREAKING, 0);
          this.shiftTimer.schedule(500);
          KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, false);
          System.out.println("Should stop walking");
        }
        // fall through
      case BREAKING:
        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, true);

        // sometimes it gets stuck while moving and doesnt look at targetBlock so it keeps trying infinitely
        if (++this.breakAttemptTime > 60) {
          this.swapState(State.STARTING, 0);
          this.breakAttemptTime = 0;
        }

        if (this.targetBlock.equals(BlockUtil.getBlockLookingAt())) {
          if (--this.miningTime <= 0) {
            this.swapState(State.STARTING, 0);
          }
        } else if (this.state == State.BREAKING && !RotationHandler.getInstance().isEnabled()) {
          this.swapState(State.STARTING, 0);
        }

        if (StrafeUtil.enabled && this.state == State.BREAKING) {
          this.destBlock = null;
          StrafeUtil.enabled = false;
          this.shiftTimer.schedule(500);
          KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, false);
        }

        break;
    }
  }

  @SubscribeEvent
  protected void onBlockChange(BlockChangeEvent event) {
    if (!this.enabled || this.state.ordinal() < State.ROTATING.ordinal()) {
      return;
    }
    if (event.pos.equals(this.targetBlock)) {
      RotationHandler.getInstance().stop();
      this.swapState(State.STARTING, 0);
    }
  }

  @SubscribeEvent
  public void onBlockDestroy(BlockDestroyEvent event) {
    if (!this.enabled || !event.getBlock().equals(this.targetBlock)) {
      return;
    }

    this.breakAttemptTime = 0;
  }

  @SubscribeEvent
  public void onParticleSpawn(SpawnParticleEvent event) {
    if (!this.enabled || !MightyMinerConfig.mithrilMinerPrecisionMiner) {
      return;
    }

    int particleID = event.getParticleTypes().getParticleID();
    if (particleID == 9 || particleID == 10) {
      Vec3 particlePos = new Vec3(event.getXCoord(), event.getYCoord(), event.getZCoord());
      MovingObjectPosition raytrace = RaytracingUtil.raytraceTowards(PlayerUtil.getPlayerEyePos(), particlePos, 4);
      if (raytrace != null && this.targetBlock.equals(raytrace.getBlockPos())) {
        RotationHandler.getInstance().stop();
        RotationHandler.getInstance().easeTo(new RotationConfiguration(
            new Target(particlePos),
            this.getRandomRotationTime(),
            null
        ));
      }
    }
  }

  @SubscribeEvent
  protected void onChat(ClientChatReceivedEvent event) {
    if (event.type != 0) {
      return;
    }
    String message = event.message.getUnformattedText();
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

  @SubscribeEvent
  protected void onRender(RenderWorldLastEvent event) {
    if (this.targetBlock != null) {
      RenderUtil.drawBlockBox(this.targetBlock, new Color(0, 255, 255, 100));
    }

    if (this.state.ordinal() >= State.WALKING.ordinal() && this.targetPoint != null) {
      Vec3 playerEye = PlayerUtil.getPlayerEyePos();
      Vec3 subVal = this.targetPoint.subtract(playerEye);
      Vec3 pos = playerEye.addVector(subVal.xCoord * 0.5, subVal.yCoord * 0.5, subVal.zCoord * 0.5);
      RenderUtil.drawTracer(this.targetPoint, new Color(124, 015, 214, 255));
      double trackerDist = PlayerUtil.getPlayerEyePos().distanceTo(targetPoint);
      if (mc.gameSettings.keyBindForward.isKeyDown()) {
        System.out.println("TrackerDist: " + trackerDist);
      }
      RenderUtil.drawText(String.format("%.2f", trackerDist), pos.xCoord, pos.yCoord, pos.zCoord, 1);
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
