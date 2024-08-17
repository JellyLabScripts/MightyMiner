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
    this.target = 0;
    this.previous = -1;
    this.state = State.END;
    this.allowSprint = true;
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

  public boolean onTick() {
//    log("pathexec ontick");
    if (!enabled) {
      return false;
    }

    if (this.stuckTimer.isScheduled() && this.stuckTimer.passed()) {
      log("Stuck for a second.");
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
      if (Math.hypot(mc.thePlayer.motionX, mc.thePlayer.motionZ) < 0.15) {
        if (!this.stuckTimer.isScheduled()) {
          this.stuckTimer.schedule(1000);
        }
      } else {
        this.stuckTimer.reset();
      }
    }

    BlockPos playerPos = PlayerUtil.getBlockStandingOn();
    // this is utterly useless but im useless as well
    int current = -1;
    List<Pair<Integer, Integer>> blocks = this.map.get(new Pair<>(playerPos.getX(), playerPos.getZ()));
    if (blocks == null) {
      current = -1;
    } else if (blocks.size() == 1) {
      current = blocks.get(0).getSecond();
    } else {
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
      this.nodeChangeTime = System.currentTimeMillis();
      this.interpolated = false;
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
    double horizontalDistanceToTarget = Math.hypot(mc.thePlayer.posX - target.getX(), mc.thePlayer.posZ - target.getZ());
    float yaw = AngleUtil.get360RotationYaw(
        AngleUtil.getRotation(mc.thePlayer.getPositionVector().addVector(mc.thePlayer.motionX, 0.0, mc.thePlayer.motionZ),
            new Vec3(target).addVector(0.5, 0.0, 0.5), false).yaw);
    float yawDiff = Math.abs(AngleUtil.get360RotationYaw() - yaw);
    if (yawDiff > 10 && !RotationHandler.getInstance().isEnabled()) {
      float rotationYaw = yaw;
      for (int i = this.target; i < this.blockPath.size(); i++) {
        BlockPos rotationTarget = this.blockPath.get(i);
        if (Math.hypot(mc.thePlayer.posX - rotationTarget.getX(), mc.thePlayer.posZ - rotationTarget.getZ()) > 5) {
          rotationYaw = AngleUtil.getRotation(rotationTarget).yaw;
          break;
        }
      }

      RotationHandler.getInstance().easeTo(new RotationConfiguration(new Angle(rotationYaw, 15f),
          Math.max(300, (long) (400 - horizontalDistanceToTarget * MightyMinerConfig.devPathRotMult)), null).easeFunction(Ease.EASE_OUT_QUAD));
    }

    if (strafeTimer.passed()) {
      strafeAmount = (float) (10f * (Math.random() * 2f - 1));
      strafeTimer.schedule(500);
    }

    if (horizontalDistanceToTarget >= 5) {
      // makes it more human but decreases accuracy - removes that weird sliding effect
      if (this.allowInterpolation && !this.interpolated) {
        long timePassed = System.currentTimeMillis() - this.nodeChangeTime;
        if (timePassed < 250) {
          yaw = mc.thePlayer.rotationYaw + yawDiff * (timePassed / 250f);
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
    boolean shouldJump = mc.thePlayer.onGround
        && Math.hypot(mc.thePlayer.posX - (target.getX() + 0.5), mc.thePlayer.posZ - (target.getZ() + 0.5)) <= 1.0
        && target.getY() >= mc.thePlayer.posY
        && target.getY() - mc.thePlayer.posY < 0.1;
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

  enum State {
    STARTING_PATH, TRAVERSING, JUMPING, WAITING, END
  }
}
