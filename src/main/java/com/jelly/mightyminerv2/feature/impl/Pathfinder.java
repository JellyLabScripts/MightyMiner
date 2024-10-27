package com.jelly.mightyminerv2.feature.impl;

import com.jelly.mightyminerv2.feature.AbstractFeature;
import com.jelly.mightyminerv2.feature.impl.PathExecutor.State;
import com.jelly.mightyminerv2.handler.RotationHandler;
import com.jelly.mightyminerv2.MightyMiner;
import com.jelly.mightyminerv2.util.PlayerUtil;
import com.jelly.mightyminerv2.util.RenderUtil;
import com.jelly.mightyminerv2.pathfinder.calculate.Path;
import com.jelly.mightyminerv2.pathfinder.calculate.path.AStarPathFinder;
import com.jelly.mightyminerv2.pathfinder.goal.Goal;
import com.jelly.mightyminerv2.pathfinder.movement.CalculationContext;
import java.awt.Color;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import jdk.nashorn.internal.ir.Block;
import kotlin.Pair;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class Pathfinder extends AbstractFeature {

  private static Pathfinder instance;

  public static Pathfinder getInstance() {
    if (instance == null) {
      instance = new Pathfinder();
    }
    return instance;
  }

  private final Deque<Pair<BlockPos, BlockPos>> pathQueue = new ConcurrentLinkedDeque<>();
  private final PathExecutor pathExecutor = PathExecutor.getInstance();
  private AStarPathFinder finder;

  private volatile boolean skipTick = false;
  private volatile boolean pathfinding = false;
  private boolean failed = false;
  private boolean succeeded = false;

  @Override
  public String getName() {
    return "Pathfinder";
  }

  @Override
  public void start() {
    if (this.pathQueue.isEmpty()) {
      error("Pathqueue is empty. Cannot start");
      return;
    }

    this.enabled = true;
    this.succeeded = false;
    this.failed = false;
    pathExecutor.start();
    send("Started");
  }

  @Override
  public void stop() {
    this.enabled = false;
    this.pathfinding = false;
    this.skipTick = false;
    this.pathQueue.clear();
    this.resetStatesAfterStop();

    send("stopped");
  }

  @Override
  public void resetStatesAfterStop() {
    if (finder != null) {
      finder.requestStop();
    }
    pathExecutor.stop();
    RotationHandler.getInstance().stop();
  }

  public void queue(BlockPos start, BlockPos end) {
    if (!this.pathQueue.isEmpty() && !this.pathQueue.peekLast().getSecond().equals(start)) {
      error("This does not start at the end of the previous path. Ignoring!");
      return;
    }

    this.pathQueue.offer(new Pair(start, end));
    log("Queued Path");
  }

  public void stopAndRequeue(BlockPos pos) {
    if (this.enabled) {
      this.pathQueue.clear();
      this.pathExecutor.clearQueue();
      this.finder.requestStop();
    }
    this.queue(pos);
  }

  public void queue(BlockPos end) {
    BlockPos start;
    if (this.pathQueue.isEmpty()) {
      if (this.pathExecutor.getCurrentPath() == null) {
        start = PlayerUtil.getBlockStandingOn();
      } else {
        start = this.pathExecutor.getCurrentPath().getEnd();
      }
    } else {
      start = this.pathQueue.peekLast().getFirst();
    }
    this.pathQueue.offer(new Pair<>(start, end));
  }

  public void setSprintState(boolean sprint) {
    pathExecutor.setAllowSprint(sprint);
  }

  public void setInterpolationState(boolean interpolate) {
    pathExecutor.setAllowInterpolation(interpolate);
  }

  @SubscribeEvent
  protected void onTick(ClientTickEvent event) {
    if (!this.enabled) {
      return;
    }

    boolean okToPath = false;
    if (event.phase == Phase.START) {
      okToPath = pathExecutor.onTick();
    }

    // just to let pathexecutor update after path has been found
    if (this.skipTick) {
      this.skipTick = false;
      return;
    }

    if (pathExecutor.failed()) {
      log("pathexeutor failed");
      this.failed = true;
      this.stop();
      return;
    }

    if (!okToPath) {
      return;
    }

    if (this.pathQueue.isEmpty()) {
      if (this.pathExecutor.getState() == State.WAITING && !this.pathfinding) {
        this.stop();
        this.succeeded = true;
        log("pathqueue empty stopping");
      }
      return;
    }

    if (this.pathfinding) {
      return;
    }

    MightyMiner.executor().execute(() -> {
      log("creating thread. wasPathfinding: " + this.pathfinding);
      if (this.pathfinding) {
        return;
      }
      this.pathfinding = true;
      try {
        Pair<BlockPos, BlockPos> startEnd = this.pathQueue.poll();
//        if(startEnd == null) return;

        BlockPos start = startEnd.getFirst();
        BlockPos end = startEnd.getSecond();
        double walkSpeed = mc.thePlayer.getAIMoveSpeed();
        CalculationContext ctx = new CalculationContext(walkSpeed * 1.3, walkSpeed, walkSpeed * 0.3);
        finder = new AStarPathFinder(start.getX(), start.getY(), start.getZ(), new Goal(end.getX(), end.getY(), end.getZ(), ctx), ctx);
        Path path = finder.calculatePath();
        log("done pathfinding");
        if (path != null) {
          path.getSmoothedPath();
          PathExecutor.getInstance().queuePath(path);
          log("starting pathexec");
        } else {
          log("No Path Found");
          failed = true;
          stop();
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      this.pathfinding = false;
      this.skipTick = true;
    });
  }

  @SubscribeEvent
  protected void onRender(RenderWorldLastEvent event) {
    if (!this.enabled) {
      return;
    }
//    this.pathExecutor.onRender();
    Deque<Path> paths = new LinkedList<>(this.pathExecutor.getPathQueue());
    if (pathExecutor.getCurrentPath() != null) {
      paths.add(pathExecutor.getCurrentPath());
    }
    if (!paths.isEmpty()) {
      paths.forEach(path -> {
        List<BlockPos> bpath = path.getSmoothedPath();
        for (int i = 0; i < bpath.size(); i++) {
          RenderUtil.drawBlock(bpath.get(i), new Color(0, 255, 0, 150));
          if (i != 0) {
            RenderUtil.drawLine(new Vec3(bpath.get(i)).addVector(0.5, 1, 0.5), new Vec3(bpath.get(i - 1)).addVector(0.5, 1, 0.5),
                new Color(0, 255, 0, 150));
          }
        }
      });
    }
  }

  public boolean completedPathTo(BlockPos pos) {
    Path prev = pathExecutor.getPreviousPath();
    return prev != null && prev.getGoal().isAtGoal(pos.getX(), pos.getY(), pos.getZ());
  }

  public boolean failed() {
    return !this.enabled && this.failed;
  }

  public boolean succeeded() {
    return !this.enabled && this.succeeded;
  }
}