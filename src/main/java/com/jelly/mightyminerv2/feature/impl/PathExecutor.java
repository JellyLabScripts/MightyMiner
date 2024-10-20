package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.config.MightyMinerConfig;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.util.AngleUtil;
import com.jelly.mightyminerv2.util.BlockUtil;
import com.jelly.mightyminerv2.util.KeyBindUtil;
import com.jelly.mightyminerv2.util.Logger;
import com.jelly.mightyminerv2.util.PlayerUtil;
import com.jelly.mightyminerv2.util.StrafeUtil;
import com.jelly.mightyminerv2.util.helper.Angle;
import com.jelly.mightyminerv2.util.helper.Clock;
import com.jelly.mightyminerv2.util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.pathfinder.calculate.Path;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import kotlin.Pair;
import net.minecraft.client.Minecraft;
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

  private final Deque<Path> pathQueue = new LinkedList<>();
  private Path prev;
  private Path curr;

  private final Map<Long, List<Long>> map = new HashMap<>();
  private final List<BlockPos> blockPath = new ArrayList<>();

  private boolean failed = false;
  private boolean succeeded = false;

  private boolean pastTarget = false;

  private int target = 0;
  private int previous = -1;
  private long nodeChangeTime = 0;

  private boolean interpolated = true;
  private float interpolYawDiff = 0f;

  private State state = State.STARTING_PATH;

  private boolean allowSprint = true;
  private boolean allowInterpolation = false;

  private final Clock stuckTimer = new Clock();

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

  public void clearQueue() {
    this.pathQueue.clear();
    this.curr = null;
    this.succeeded = true;
    this.failed = false;
    this.interpolated = false;
    this.target = 0;
    this.previous = -1;
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

    BlockPos playerPos = PlayerUtil.getBlockStandingOn();
    if (this.curr != null) {
      // this is utterly useless but im useless as well
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
        log("changed target from " + this.previous + " to " + this.target);
        RotationHandler.getInstance().stop();
      }

      if (Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ) < 0.05) {
        if (!this.stuckTimer.isScheduled()) {
          this.stuckTimer.schedule(1000);
        }
      } else {
        this.stuckTimer.reset();
      }
    } else {
      if (this.stuckTimer.isScheduled()) {
        this.stuckTimer.reset();
      }
      if (this.pathQueue.isEmpty()) {
        return true;
      }
    }

    if (this.curr == null || this.target == this.blockPath.size()) {
      log("Path traversed");
      if (this.pathQueue.isEmpty()) {
        log("Pathqueue is empty");
        if (this.curr != null) {
          this.curr = null;
          this.target = 0;
          this.previous = -1;
        }
        this.state = State.WAITING;
        return true;
      }
      this.succeeded = true;
      this.failed = false;
      this.prev = this.curr;
      this.target = 1;
      this.previous = 0;
      loadPath(this.pathQueue.poll());
      if (this.target == this.blockPath.size()) {
        return true;
      }
      log("loaded new path target: " + this.target + ", prev: " + this.previous);
    }

    BlockPos target = this.blockPath.get(this.target);

    if (this.target < this.blockPath.size() - 1) {
      BlockPos nextTarget = this.blockPath.get(this.target + 1);
      double playerDistToNext = playerPos.distanceSq(nextTarget);
      double targetDistToNext = target.distanceSq(nextTarget);

      if ((this.pastTarget || (this.pastTarget = playerDistToNext > targetDistToNext)) && playerDistToNext < targetDistToNext) {
        this.previous = this.target;
        this.target++;
        target = this.blockPath.get(this.target);
        log("walked past target");
      }
    }

    boolean onGround = mc.thePlayer.onGround;

    // effectively removes backtracking and makes detection better

    int targetX = target.getX();
    int targetZ = target.getZ();
    double horizontalDistToTarget = Math.hypot(mc.thePlayer.posX - targetX - 0.5, mc.thePlayer.posZ - targetZ - 0.5);
    float yaw = AngleUtil.getRotationYaw360(mc.thePlayer.getPositionVector(), new Vec3(targetX + 0.5, 0.0, targetZ + 0.5));
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

      float time = MightyMinerConfig.fixrot ? MightyMinerConfig.rottime : Math.max(300, (long) (400 - horizontalDistToTarget * MightyMinerConfig.rotmult));
      RotationHandler.getInstance().easeTo(
          new RotationConfiguration(
              new Angle(rotYaw, 15f),
              (long) time, null
          )
      );
    }

    float ipYaw = yaw;
    if (onGround && horizontalDistToTarget >= 8 && this.allowInterpolation && !this.interpolated) {
      float time = 200f;
      long timePassed = Math.min((long) time, System.currentTimeMillis() - this.nodeChangeTime);
      ipYaw -= this.interpolYawDiff * (1 - (timePassed / time));
      if (timePassed == time) {
        this.interpolated = true;
      }
    }
//    else {
//      String because = "";
//      if (!onGround) {
//        because = "onGround";
//      }
//      if (horizontalDistToTarget < 8) {
//        because = "hhriz < 8";
//      }
//      if (!this.allowInterpolation) {
//        because = "interp not allowed";
//      }
//      if (this.interpolated) {
//        because = "interp done";
//      }
//      error("not interpolating cuz " + because);
//    }

    StrafeUtil.enabled = yawDiff > 3;
    StrafeUtil.yaw = ipYaw;

    boolean shouldJump = BlockUtil.canWalkBetween(this.curr.getCtx(), PlayerUtil.getBlockStandingOn(),
        new BlockPos(mc.thePlayer.getPositionVector().add(AngleUtil.getVectorForRotation(yaw))));
    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, true);
    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSprint, this.allowSprint && yawDiff < 40 && !shouldJump);
    if (shouldJump && onGround) {
      mc.thePlayer.jump();
      this.state = State.JUMPING;
    }
    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindJump, shouldJump);

    return playerPos.distanceSqToCenter(target.getX(), target.getY(), target.getZ()) < 100;
  }

  public void loadPath(Path path) {
    this.blockPath.clear();
    this.map.clear();

    this.curr = path;
    this.blockPath.addAll(this.curr.getSmoothedPath());
    for (int i = 0; i < this.blockPath.size(); i++) {
      BlockPos pos = this.blockPath.get(i);
      this.map.computeIfAbsent(this.pack(pos.getX(), pos.getZ()), k -> new ArrayList<>()).add(this.pack(pos.getY(), i));
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

  public Deque<Path> getPathQueue() {
    return this.pathQueue;
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
