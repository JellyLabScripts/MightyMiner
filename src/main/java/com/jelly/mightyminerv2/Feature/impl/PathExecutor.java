package com.jelly.mightyminerv2.Feature.impl;

import com.jelly.mightyminerv2.Config.MightyMinerConfig;
import com.jelly.mightyminerv2.Handler.RotationHandler;
import com.jelly.mightyminerv2.Util.AngleUtil;
import com.jelly.mightyminerv2.Util.KeyBindUtil;
import com.jelly.mightyminerv2.Util.LogUtil;
import com.jelly.mightyminerv2.Util.PlayerUtil;
import com.jelly.mightyminerv2.Util.StrafeUtil;
import com.jelly.mightyminerv2.Util.helper.Angle;
import com.jelly.mightyminerv2.Util.helper.Clock;
import com.jelly.mightyminerv2.Util.helper.RotationConfiguration;
import com.jelly.mightyminerv2.Util.helper.RotationConfiguration.Ease;
import com.jelly.mightyminerv2.pathfinder.calculate.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

  private Deque<Path> pathQueue = new LinkedList<>();
  private Path prev;
  private Path curr;

  private Map<Pair<Integer, Integer>, List<Pair<Integer, Integer>>> map = new HashMap<>();
  private List<BlockPos> blockPath = new ArrayList<>();

  private boolean failed = false;
  private boolean succeeded = false;

  private int target = 0;
  private int previous = -1;
  private long nodeChangeTime = 0;
  private boolean interpolated = true;

  private State state = State.STARTING_PATH;

  private boolean allowSprint = true;
  private boolean allowInterpolation = false;
  private boolean allowStrafing = false;

  private Clock stuckTimer = new Clock();
  private Clock strafeTimer = new Clock();
  private float strafeAmount = 0f;

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
    this.state = State.END;
    this.allowSprint = true;
    this.allowInterpolation = false;
    this.strafeAmount = 0;
    this.nodeChangeTime = 0;
    this.interpolated = true;
    StrafeUtil.enabled = false;
    RotationHandler.getInstance().reset();
    KeyBindUtil.releaseAllExcept();
  }

  public void setAllowSprint(boolean sprint) {
    this.allowSprint = sprint;
  }

  public void setAllowInterpolation(boolean interpolate) {
    this.allowInterpolation = interpolate;
  }

  public void setAllowStrafing(boolean strafing) {
    this.allowInterpolation = strafing;
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
        this.map.computeIfAbsent(new Pair<>(pos.getX(), pos.getZ()), k -> new ArrayList<>()).add(new Pair<>(pos.getY(), i));
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
    List<Pair<Integer, Integer>> blocks = this.map.get(new Pair<>(playerPos.getX(), playerPos.getZ()));
    int current = -1;
    if (blocks != null && !blocks.isEmpty()) {
      Pair<Integer, Integer> best = blocks.get(0);
      double y = mc.thePlayer.posY;
      for (Pair<Integer, Integer> block : blocks) {
        if ((block.getFirst() < y && block.getFirst() > best.getFirst()) ||
            (block.getFirst() >= y && block.getFirst() < best.getFirst())) {
          best = block;
        }
      }
      current = best.getSecond();
    }

    if (current != -1 && current != previous) {
      this.previous = current;
      this.target = current + 1;
      this.state = State.TRAVERSING;
      this.interpolated = false;
      this.nodeChangeTime = System.currentTimeMillis();
      RotationHandler.getInstance().reset();
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
    double horizontalDistToTarget = Math.hypot(mc.thePlayer.posX - target.getX() - 0.5, mc.thePlayer.posZ - target.getZ() - 0.5);
    float yaw = AngleUtil.get360RotationYaw(
        AngleUtil.getRotation(PlayerUtil.getNextTickPosition(), new Vec3(target).addVector(0.5, 0.0, 0.5), false).yaw);
    float yawDiff = Math.abs(AngleUtil.get360RotationYaw() - yaw);
    if (yawDiff > 10 && !RotationHandler.getInstance().isEnabled()) {
      float rotationYaw = yaw;

      // look at a block thats at least 5 blocks away instead of looking at the target which helps reduce buggy rotation
      for (int i = this.target; i < this.blockPath.size(); i++) {
        BlockPos rotationTarget = this.blockPath.get(i);
        if (Math.hypot(mc.thePlayer.posX - rotationTarget.getX(), mc.thePlayer.posZ - rotationTarget.getZ()) > 5) {
          rotationYaw = AngleUtil.getRotation(rotationTarget).yaw;
          break;
        }
      }

      RotationHandler.getInstance().easeTo(
          new RotationConfiguration(
              new Angle(rotationYaw, 15f),
              Math.max(300, (long) (400 - horizontalDistToTarget * MightyMinerConfig.devPathRotMult)),
              null)
              .easeFunction(Ease.EASE_OUT_QUAD)
      );
    }

    if (strafeTimer.passed()) {
      strafeAmount = (float) (10f * (Math.random() * 2f - 1));
      strafeTimer.schedule(500);
    }

    if (horizontalDistToTarget >= 5) {
      // makes it more human but decreases accuracy - removes that weird sliding effect
      if (this.allowInterpolation && !this.interpolated) {
        long timePassed = System.currentTimeMillis() - this.nodeChangeTime;
        if (timePassed < 200) {
          yaw = mc.thePlayer.rotationYaw + yawDiff * (timePassed / 200f);
        } else {
          this.interpolated = true;
        }
      }
    } else {
      strafeAmount = 0;
    }

    StrafeUtil.enabled = true;
    StrafeUtil.yaw = yaw + strafeAmount;

    // needs work
    boolean shouldJump =
        mc.thePlayer.onGround && horizontalDistToTarget <= 1.0 && target.getY() >= mc.thePlayer.posY && target.getY() - mc.thePlayer.posY < 0.05;
    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, true);
    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSprint, this.allowSprint && yawDiff < 40 && !shouldJump && mc.thePlayer.onGround);
    if (shouldJump) {
      mc.thePlayer.jump();
      this.state = State.JUMPING;
    }

    return mc.thePlayer.getDistanceSqToCenter(this.blockPath.get(this.blockPath.size() - 1)) < 100;
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

  void log(String message) {
    LogUtil.log(getMessage(message));
  }

  void send(String message) {
    LogUtil.send(getMessage(message));
  }

  void error(String message) {
    LogUtil.error(getMessage(message));
  }

  String getMessage(String message) {
    return "[PathExecutor] " + message;
  }

  // we got no use for this - yet
  enum State {
    STARTING_PATH, TRAVERSING, JUMPING, WAITING, END
  }
}
