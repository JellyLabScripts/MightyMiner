package com.jelly.mightyminerv2.Feature.impl;

import com.jelly.mightyminerv2.Handler.RotationHandler;
import com.jelly.mightyminerv2.Util.AngleUtil;
import com.jelly.mightyminerv2.Util.KeyBindUtil;
import com.jelly.mightyminerv2.Util.LogUtil;
import com.jelly.mightyminerv2.Util.PlayerUtil;
import com.jelly.mightyminerv2.Util.StrafeUtil;
import com.jelly.mightyminerv2.Util.helper.Angle;
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
  private Path curr;

  private Map<Pair<Integer, Integer>, List<Pair<Integer, Integer>>> map = new HashMap<>();
  private List<BlockPos> blockPath = new ArrayList<>();

  private boolean failed = false;
  private boolean succeeded = false;

  private int target = 0;
  private int previous = -1;

  private State state = State.STARTING_PATH;

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
    StrafeUtil.enabled = false;
    RotationHandler.getInstance().reset();
    KeyBindUtil.releaseAllExcept();
  }

  public boolean onTick() {
//    log("pathexec ontick");
    if (!enabled) {
      return false;
    }

    if (this.blockPath.isEmpty()) {
//      log("blockpath is empty");
      if (this.pathQueue.isEmpty()) {
//        log("pathqueue is empty");
        this.state = State.WAITING;
        return true;
      } else {
//        log("pathqueue isnt empty");
        this.curr = this.pathQueue.poll();
        this.blockPath.addAll(this.curr.getSmoothedPath());
        for (int i = 0; i < this.blockPath.size(); i++) {
          BlockPos pos = this.blockPath.get(i);
          this.map.computeIfAbsent(new Pair<>(pos.getX(), pos.getZ()), k -> new ArrayList<>()).add(new Pair<>(pos.getY(), i));
        }

        this.enabled = true;
        this.state = State.STARTING_PATH;
      }
    }

    BlockPos playerPos = PlayerUtil.getBlockStandingOn();
    int current = Optional.ofNullable(this.map.get(new Pair<>(playerPos.getX(), playerPos.getZ())))
        .map(list -> list.stream()
            .filter(it -> it.getFirst() <= mc.thePlayer.posY)
            .max(Comparator.comparing(Pair::getFirst))
            .map(Pair::getSecond)
            .orElse(-1))
        .orElse(-1);

    if (current != -1 && current != previous) {
      log("Standing on node " + current);
      this.previous = current;
      this.target = current + 1;
      this.state = State.TRAVERSING;
      RotationHandler.getInstance().reset();
      if (this.target == this.blockPath.size()) {
        send("Path traversed");
        this.succeeded = true;
        this.failed = false;
        this.blockPath.clear();
        this.map.clear();
        return true;
      }
    }

    BlockPos target = this.blockPath.get(this.target);
    float yaw = AngleUtil.get360RotationYaw(
        AngleUtil.getRotation(mc.thePlayer.getPositionVector().addVector(mc.thePlayer.motionX, 0.0, mc.thePlayer.motionZ),
            new Vec3(target).addVector(0.5, 0.0, 0.5), false).yaw
    );

    double yawDiff = Math.abs(AngleUtil.get360RotationYaw() - yaw);
    if (yawDiff > 10 && !RotationHandler.getInstance().isEnabled()) {
      log("Started Rotation");
      RotationHandler.getInstance().easeTo(new RotationConfiguration(new Angle(yaw, 20f), 250, null).easeFunction(Ease.EASE_OUT_QUAD));
    }

    StrafeUtil.enabled = true;
    StrafeUtil.yaw = yaw;

    // needs work
    boolean shouldJump = mc.thePlayer.onGround
        && Math.hypot(mc.thePlayer.posX - (target.getX() + 0.5), mc.thePlayer.posZ - (target.getZ() + 0.5)) <= 1.0
        && target.getY() >= mc.thePlayer.posY
        && target.getY() - mc.thePlayer.posY < 0.1;
    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindForward, true);
    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindSprint, yawDiff < 40 && !shouldJump && mc.thePlayer.onGround);
    if (shouldJump) {
      mc.thePlayer.jump();
      this.state = State.JUMPING;
    }

    return mc.thePlayer.getDistanceSqToCenter(this.blockPath.get(this.blockPath.size() - 1)) < 100;
  }

  public State getState() {
    return this.state;
  }

  public boolean isEnabled(){
    return this.enabled;
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
