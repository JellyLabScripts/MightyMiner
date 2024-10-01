package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.pathfinder.movement.CalculationContext;
import com.jelly.mightyminerv2.pathfinder.util.BlockUtil;
import com.jelly.mightyminerv2.util.AngleUtil;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.PlayerUtil;
import com.jelly.mightyminerv2.util.RenderUtil;
import com.jelly.mightyminerv2.util.StrafeUtil;
import com.jelly.mightyminerv2.util.helper.Angle;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.pathfinder.calculate.Path;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

public class PathExecutor {

  private static PathExecutor instance;

  public static PathExecutor getInstance() {
    if (instance == null) {
      instance = new PathExecutor();
    }
    return instance;
  }

  private boolean enabled = false;
  private final Minecraft mc = Minecraft.getMinecraft();

  private Deque<Path> pathQueue = new LinkedList<>();
  private Path prev;
  private Path curr;

  private Map<Long, List<Long>> map = new HashMap<>();
  private List<BlockPos> blockPath = new ArrayList<>();

  private boolean failed = false;
  private boolean succeeded = false;

  private boolean pastTarget = false;

  private int target = 0;
  private int previous = -1;
  private long nodeChangeTime = 0;

  private boolean interpolated = true;
  private float interpolYawDiff = 0f;

  private float tempIpYaw = 0;

  private State state = State.STARTING_PATH;

  private boolean allowSprint = true;
  private boolean allowInterpolation = false;

  private Clock stuckTimer = new Clock();

  public void queuePath(Path path) {
    if (path.getPath().isEmpty()) {
      error("Path is empty");
      failed = true;
      return;
    }

    BlockPos start = path.getStart();
    Path lastPath = (this.curr != null) ? this.curr : this.pathQueue.peekLast();

    if (lastPath != null && !lastPath.getGoal().isAtGoal(start.getX(), start.getY(), start.getZ())) {
      error("This path segment does not start at last path's goal. LastpathGoal: " + lastPath.getGoal() + ", ThisPathStart: " + start);
      failed = true;
      return;
    }

    this.pathQueue.offer(path);
  }

  public void start() {
    this.state = State.STARTING_PATH;
    this.enabled = true;
  }

  public void stop() {
    this.enabled = false;
    this.pathQueue.clear();
    this.blockPath.clear();
    this.map.clear();
    this.curr = null;
    this.prev = null;
    this.target = 0;
    this.previous = -1;
    this.pastTarget = false;
    this.state = State.END;
    this.interpolYawDiff = 0f;
    this.allowSprint = true;
    this.allowInterpolation = false;
    this.nodeChangeTime = 0;
    this.interpolated = true;
    StrafeUtil.enabled = false;
    RotationHandler.getInstance().stop();
    KeyBindUtil.releaseAllExcept();
  }

  public void setAllowSprint(boolean sprint) {
    this.allowSprint = sprint;
  }

  public void setAllowInterpolation(boolean interpolate) {
    this.allowInterpolation = interpolate;
  }

  public boolean onTick() {
    if (!enabled) {
      return false;
    }

    if (this.stuckTimer.isScheduled() && this.stuckTimer.passed()) {
      log("Was Stuck For a Second.");
      this.failed = true;
      this.succeeded = false;
      this.stop();
    }

    if (this.curr == null) {
      if (this.stuckTimer.isScheduled()) {
        this.stuckTimer.reset();
      }
      if (this.pathQueue.isEmpty()) {
        this.state = State.WAITING;
        return true;
      }
      log("loading new path");
      this.blockPath.clear();
      this.map.clear();
      this.previous = -1;
      this.target = 0;

      this.curr = this.pathQueue.poll();
      this.blockPath.addAll(this.curr.getSmoothedPath());
      for (int i = 0; i < this.blockPath.size(); i++) {
        BlockPos pos = this.blockPath.get(i);
        this.map.computeIfAbsent(this.pack(pos.getX(), pos.getZ()), k -> new ArrayList<>()).add(this.pack(pos.getY(), i));
      }

      this.enabled = true;
      this.state = State.STARTING_PATH;
    } else {
      if (Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ) < 0.05) {
        if (!this.stuckTimer.isScheduled()) {
          this.stuckTimer.schedule(1000);
        }
      } else {
        this.stuckTimer.reset();
      }
    }

    // this is utterly useless but im useless as well

    BlockPos playerPos = PlayerUtil.getBlockStandingOn();
//    Vec3 nextTickPos = PlayerUtil.getNextTickPosition();
    List<Long> blockHashes = this.map.get(this.pack(playerPos.getX(), playerPos.getZ()));
    int current = -1;
    if (blockHashes != null && !blockHashes.isEmpty()) {
      int bestY = -1;
      double playerY = mc.thePlayer.posY;
      for (Long blockHash : blockHashes) {
        Pair<Integer, Integer> block = this.unpack(blockHash);
        int blockY = block.getFirst();
        int blockTarget = block.getSecond();
        if (blockTarget > this.previous) {
          if (bestY == -1 || (blockY < playerY && blockY > bestY) || (blockY >= playerY && blockY < bestY)) {
            bestY = block.getFirst();
            current = blockTarget;
          }
        }
      }
    }

    if (current != -1 && current > previous) {
      this.previous = current;
      this.target = current + 1;
      this.state = State.TRAVERSING;
      this.pastTarget = false;
      this.interpolated = false;
      this.interpolYawDiff = 0;
      this.nodeChangeTime = System.currentTimeMillis();
      StrafeUtil.enabled = false;
      log("changed target");
      RotationHandler.getInstance().stop();
      if (this.target == this.blockPath.size()) {
        log("Path traversed");
        this.succeeded = true;
        this.failed = false;
        this.prev = this.curr;
        this.curr = null;
        return true;
      }
    }

    BlockPos target = this.blockPath.get(this.target);
    boolean onGround = mc.thePlayer.onGround;

    // effectively removes backtracking and makes detection better
    if (this.target < this.blockPath.size() - 1) {
      BlockPos nextTarget = this.blockPath.get(this.target + 1);
      double playerDistToNext = playerPos.distanceSq(nextTarget);
      double targetDistToNext = target.distanceSq(nextTarget);

      if ((this.pastTarget || (this.pastTarget = playerDistToNext > targetDistToNext)) && playerDistToNext < targetDistToNext) {
        this.previous = this.target;
        this.target++;
        log("walked past target");
        return false;
      }
    }

    int targetX = target.getX();
    int targetZ = target.getZ();
    double horizontalDistToTarget = Math.hypot(mc.thePlayer.posX - targetX - 0.5, mc.thePlayer.posZ - targetZ - 0.5);
    float yaw = AngleUtil.getRotationYaw360(PlayerUtil.getNextTickPosition(0.6f), new Vec3(targetX + 0.5, 0.0, targetZ + 0.5));
    float yawDiff = Math.abs(AngleUtil.get360RotationYaw() - yaw);

    if (this.interpolYawDiff == 0) {
      this.interpolYawDiff = yaw - AngleUtil.get360RotationYaw();
    }

    if (yawDiff > 3 && !RotationHandler.getInstance().isEnabled()) {
      float rotYaw = yaw;

      // look at a block thats at least 5 blocks away instead of looking at the target which helps reduce buggy rotation
      for (int i = this.target; i < this.blockPath.size(); i++) {
        BlockPos rotationTarget = this.blockPath.get(i);
        if (Math.hypot(mc.thePlayer.posX - rotationTarget.getX(), mc.thePlayer.posZ - rotationTarget.getZ()) > 5) {
          rotYaw = AngleUtil.getRotation(rotationTarget).yaw;
          break;
        }
      }

      RotationHandler.getInstance().easeTo(
          new RotationConfiguration(
              new Angle(rotYaw, 15f),
              Math.max(300, (long) (400 - horizontalDistToTarget * 2f)), null
          )
//        .easeFunction(Ease.EASE_OUT_QUAD)
      );
    }

    float ipYaw = yaw;
    if (onGround && horizontalDistToTarget >= 8 && this.allowInterpolation && !this.interpolated) {
      float time = 200f;
      long timePassed = Math.min((long) time, System.currentTimeMillis() - this.nodeChangeTime);
      ipYaw -= this.interpolYawDiff * (1 - (timePassed / time));
      tempIpYaw = ipYaw;
      if (timePassed == time) {
        this.interpolated = true;
      }
    }

    StrafeUtil.enabled = yawDiff > 3;
    StrafeUtil.yaw = ipYaw;

    // needs work
    // if next tick pos is not target and stuck can jump then jump
//    int nextX = nextPos.getX();
//    int nextY = nextPos.getY();
//    int nextZ = nextPos.getZ();
//    CalculationContext ctx = this.curr.getCtx();
//    if (ctx.get(nextX, nextY, nextZ).getBlock() != Blocks.air && ctx.get(nextX, nextY + 1, nextZ).getBlock() == Blocks.air && ctx.get(nextX, nextY + 2, nextZ).getBlock() == Blocks.air) {
//      shouldJump = !BlockUtil.INSTANCE.bresenham(ctx, PlayerUtil.getBlockStandingOn(), nextPos);
//    }
    boolean shouldJump = com.jelly.mightyminerv2.util.BlockUtil.canWalkBetween(this.curr.getCtx(), PlayerUtil.getBlockStandingOn(), new BlockPos(mc.thePlayer.getPositionVector().add(AngleUtil.getVectorForRotation(yaw))));
    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, true);
    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSprint, this.allowSprint && yawDiff < 40 && !shouldJump);
    if (shouldJump && onGround) {
      mc.thePlayer.jump();
      this.state = State.JUMPING;
    }
    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindJump, shouldJump);

    return playerPos.distanceSqToCenter(target.getX(), target.getY(), target.getZ()) < 100;
  }

  public void onRender() {
//    if (!this.interpolated) {
//      RenderUtil.drawPoint(mc.thePlayer.getPositionVector().add(AngleUtil.getVectorForRotation(tempIpYaw)), Color.CYAN);
//    }
    if (this.target != 0 && this.target < this.blockPath.size()) {
      BlockPos target = this.blockPath.get(this.target);
      BlockPos pos = new BlockPos(mc.thePlayer.getPositionVector().add(AngleUtil.getVectorForRotation(
          AngleUtil.getRotationYaw360(PlayerUtil.getNextTickPosition(0.6f), new Vec3(target.getX() + 0.5, 0, target.getZ() + 0.5)))));
      if (!mc.theWorld.isAirBlock(pos)) {
        RenderUtil.drawBlockBox(pos, Color.GREEN);
      }
    }
  }

  public State getState() {
    return this.state;
  }

  public boolean isEnabled() {
    return this.enabled;
  }

  public Path getPreviousPath() {
    return this.prev;
  }

  public Path getCurrentPath() {
    return this.curr;
  }

  public boolean failed() {
    return !this.enabled && this.failed;
  }

  public boolean ended() {
    return !this.enabled && this.succeeded;
  }

  private long pack(int x, int z) {
    return ((long) x << 32) | (z & 0xFFFFFFFFL);
  }

  public Pair<Integer, Integer> unpack(long packed) {
    return new Pair<>((int) (packed >> 32), (int) packed);
  }

  void log(String message) {
    Logger.sendLog(getMessage(message));
  }

  void send(String message) {
    Logger.sendMessage(getMessage(message));
  }

  void error(String message) {
    Logger.sendError(getMessage(message));
  }

  void note(String message) {
    Logger.sendNote(getMessage(message));
  }

  String getMessage(String message) {
    return "[PathExecutor] " + message;
  }

  // we got no use for this - yet
  enum State {
    STARTING_PATH, TRAVERSING, JUMPING, WAITING, END
  }
}
