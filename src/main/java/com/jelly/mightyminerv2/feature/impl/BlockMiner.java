package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.event.BlockChangeEvent;
import com.jelly.mightyminerv2.event.BlockDestroyEvent;
import com.jelly.mightyminerv2.event.SpawnParticleEvent;
import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.util.*;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.MineableBlock;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.util.helper.Target;
import java.awt.Color;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.List;
import java.util.Random;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

// Code here is terrible
// Help me fix it or fix it pls
public class BlockMiner extends AbstractFeature {

  private static BlockMiner instance;

  public static BlockMiner getInstance() {
    if (instance == null) {
      instance = new BlockMiner();
    }
    return instance;
  }

  private static BoostState boostState = BoostState.UNKNOWN;

  private final Random random = new Random();
  // Map of stateId and index in priority array
  private Map<Integer, Integer> blocks = new HashMap<>();
  private int[] priority = {1, 1, 1, 1};  // Priority of blocks array in order
  private State state = State.STARTING;
  private BlockMinerError mithrilError = BlockMinerError.NONE;
  private int miningSpeed = 200;          // Mainly for TickGliding
  private int miningTime = 0;             // For TickGliding
  private int speedBoost = 0;             // Boost Percentage
  private BlockPos targetBlock = null;
  private Vec3 targetPoint = null;
  private Vec3 destBlock = null;
  private int breakAttemptTime = 0;
  private final Clock shiftTimer = new Clock();
  public int wait_threshold = 2000;

  @Override
  public String getName() {
    return "BlockMiner";
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
  public void start(MineableBlock[] blockToMine, final int[] priority) {
    this.start(blockToMine, 200, 100, priority, "");
  }

  public void start(MineableBlock[] blocksToMine, final int miningSpeed, final int speedBoost, final int[] priority, String miningTool) {
    // this should never happen
    if (!miningTool.isEmpty() && !InventoryUtil.holdItem(miningTool)) {
      error("Cannot hold " + miningTool);
      this.stop(BlockMinerError.NOT_ENOUGH_BLOCKS);
      return;
    }
    if (blocksToMine == null) {
      error("No blocks to mine.");
      this.stop(BlockMinerError.NOT_ENOUGH_BLOCKS);
      return;
    }
    for (int i = 0; i < blocksToMine.length; i++) {
      for (int j : blocksToMine[i].stateIds) {
        this.blocks.put(j, i);
      }
    }
    this.miningSpeed = miningSpeed;
    this.speedBoost = speedBoost;
    this.priority = priority;
    this.enabled = true;
    this.mithrilError = BlockMinerError.NONE;

    this.start();
    send("Enabled");
  }

  public void stop(BlockMinerError error) {
    this.mithrilError = error;
    this.stop();
  }

  public boolean hasSucceeded() {
    return !this.enabled && this.mithrilError == BlockMinerError.NONE;
  }

  public BlockMinerError getMithrilError() {
    return this.mithrilError;
  }

  public static BoostState getBoostState() {
    return boostState;
  }

  @SubscribeEvent
  protected void onTick(TickEvent.ClientTickEvent event) {
    if (!this.enabled || mc.currentScreen != null || event.phase == Phase.END) {
      return;
    }

    if (this.shiftTimer.isScheduled() && this.shiftTimer.passed()) {
      KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSneak, MightyMinerConfig.mithrilMinerSneakWhileMining);
      this.shiftTimer.reset();
    }

    switch (this.state) {
      case STARTING:
        this.breakAttemptTime = 0;
        this.swapState(State.CHOOSING_BLOCK, 0);

        // Unknown or Available
        if (boostState.ordinal() <= 1) {
          this.swapState(State.SPEED_BOOST, 250);
          KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, false);
        }

        log("Starting. stopped walking");
        break;
      case SPEED_BOOST:
        if (this.isTimerRunning()) {
          return;
        }

        KeyBindUtil.rightClick();
        this.swapState(State.STARTING, 0);
        boostState = BoostState.INACTIVE;
        break;
      case CHOOSING_BLOCK:
        List<BlockPos> blocks;
        if (!MightyMinerConfig.mithrilStrafe || (blocks = BlockUtil.getBestMineableBlocksAround(this.blocks, this.priority, this.targetBlock,
            this.miningSpeed)).isEmpty()) {
          blocks = BlockUtil.getBestMineableBlocks(this.blocks, this.priority, this.targetBlock, this.miningSpeed);
        }

        if (blocks.size() < 2) {
          if (!this.timer.isScheduled()) {
            log("Scheduled a 2 second timer to see if mithril spawns back or not");
            this.timer.schedule(wait_threshold);
          }

          if (this.hasTimerEnded()) {
            error("Cannot find enough mithril blocks to mine.");
            this.stop(BlockMinerError.NOT_ENOUGH_BLOCKS);
          }
          return;
        } else if (this.timer.isScheduled()) {
          this.timer.reset();
        }

        this.targetBlock = blocks.get(blocks.get(0).equals(this.targetBlock) ? 1 : 0);
        final int boostMultiplier = boostState == BoostState.ACTIVE ? this.speedBoost / 100 : 1;
        this.miningTime = BlockUtil.getMiningTime(Block.getStateId(mc.theWorld.getBlockState(this.targetBlock)), this.miningSpeed * boostMultiplier);
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
        log("Rotating");
        // fall through
      case WALKING:
        if ((this.destBlock == null || Math.hypot(this.destBlock.xCoord - mc.thePlayer.posX, this.destBlock.zCoord - mc.thePlayer.posZ) > 0.2)
            && PlayerUtil.getPlayerEyePos().distanceTo(this.targetPoint) > 3.5) {
          this.shiftTimer.reset();
          StrafeUtil.enabled = true;
          StrafeUtil.yaw = AngleUtil.getRotationYaw360(this.destBlock == null ? this.targetPoint : this.destBlock);
          KeyBindUtil.holdThese(mc.gameSettings.keyBindForward, mc.gameSettings.keyBindSneak);
        } else {
          this.destBlock = null;
          StrafeUtil.enabled = false;
          this.swapState(State.BREAKING, 0);
          this.shiftTimer.schedule(this.getRandomSneakTime());
          KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, false);
        }
        // fall through
      case BREAKING:
        KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindAttack, true);

        // sometimes it gets stuck while moving and doesnt look at targetBlock so it keeps trying infinitely
        if (++this.breakAttemptTime > this.miningTime + 60) {
          log("BreakAttempTime > MiningTime + 60");
          this.swapState(State.STARTING, 0);
        }

        if (this.targetBlock.equals(BlockUtil.getBlockLookingAt())) {
          if (--this.miningTime <= 0) {
            log("MiningTime <= 0");
            this.swapState(State.STARTING, 0);
          }
        } else if (this.state != State.WALKING && !RotationHandler.getInstance().isEnabled()) {
          log("!Rotating");
          this.swapState(State.STARTING, 0);
        }

        if (StrafeUtil.enabled && this.state == State.BREAKING) {
          this.destBlock = null;
          StrafeUtil.enabled = false;
          this.shiftTimer.schedule(this.getRandomSneakTime());
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
        log("stopped rot");
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
    if (!this.enabled) {
      return;
    }
    if (this.targetBlock != null) {
      RenderUtil.drawBlock(this.targetBlock, new Color(0, 255, 255, 100));
    }
    if (this.targetPoint != null) {
      RenderUtil.drawPoint(this.targetPoint, new Color(255, 0, 0, 150));
    }
  }

  private void swapState(final State toState, final int delay) {
    this.state = toState;
    if (delay == 0) {
      this.timer.reset();
    } else {
      this.timer.schedule(delay);
    }
  }

  private int getRandomRotationTime() {
    return MightyMinerConfig.mithrilMinerRotationTime + (int) (this.random.nextFloat() * MightyMinerConfig.mithrilMinerRotationTimeRandomizer);
  }

  private int getRandomSneakTime() {
    return MightyMinerConfig.mithrilMinerSneakTime + (int) (this.random.nextFloat() * MightyMinerConfig.mithrilMinerSneakTimeRandomizer);
  }

  // DO NOT REARRANGE THIS
  enum State {
    STARTING, SPEED_BOOST, CHOOSING_BLOCK, ROTATING, WALKING, BREAKING
  }

  enum BoostState {
    UNKNOWN, AVAILABLE, ACTIVE, INACTIVE,
  }

  public enum BlockMinerError {
    NONE, NOT_ENOUGH_BLOCKS
  }
}
