package com.jelly.mightyminerv2.Feature.impl;

import static com.jelly.mightyminerv2.Macro.AbstractMacro.mc;

import com.jelly.mightyminerv2.Feature.IFeature;
import com.jelly.mightyminerv2.Feature.impl.PathExecutor.State;
import com.jelly.mightyminerv2.Handler.RotationHandler;
import com.jelly.mightyminerv2.MightyMiner;
import com.jelly.mightyminerv2.Util.LogUtil;
import com.jelly.mightyminerv2.pathfinder.calculate.Path;
import com.jelly.mightyminerv2.pathfinder.calculate.path.AStarPathFinder;
import com.jelly.mightyminerv2.pathfinder.goal.Goal;
import com.jelly.mightyminerv2.pathfinder.movement.CalculationContext;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import kotlin.Pair;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class Pathfinder implements IFeature {

  private static Pathfinder instance = new Pathfinder();

  public static Pathfinder getInstance() {
    return instance;
  }

  private boolean enabled = false;
  private Deque<Pair<BlockPos, BlockPos>> pathQueue = new ConcurrentLinkedDeque<>();
  private AStarPathFinder finder;

  private Object pathfindLock = new Object();

  private PathExecutor pathExecutor = PathExecutor.getInstance();

  boolean pathfinding = false;

  private boolean failed = false;
  private boolean succeeded = false;

  @Override
  public String getName() {
    return "Pathfinder";
  }

  @Override
  public boolean isEnabled() {
    return this.enabled;
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
    log("Started");
  }

  @Override
  public void stop() {
    this.enabled = false;
    this.pathfinding = false;
    this.pathQueue.clear();
    this.resetStatesAfterStop();
  }

  @Override
  public void resetStatesAfterStop() {
    pathExecutor.stop();
    RotationHandler.getInstance().reset();
  }

  @Override
  public boolean shouldCheckForFailsafe() {
    return false;
  }

  public void queue(BlockPos start, BlockPos end) {
    if (!this.pathQueue.isEmpty() && !this.pathQueue.peekLast().getSecond().equals(start)) {
      error("This does not start at the end of the previous path. Ignoring!");
      return;
    }

    this.pathQueue.offer(new Pair(start, end));
    log("Queued Path");
  }


  @SubscribeEvent
  public void onTick(ClientTickEvent event) {
    if (!this.enabled) {
      return;
    }

    boolean okToPath = pathExecutor.onTick();

    if (okToPath) {
      log("okToPath");
      if (this.pathQueue.isEmpty() && pathExecutor.getState() == State.WAITING) {
        this.stop();
        this.succeeded = true;
        log("pathqueue empty stopping");
        return;
      }

      if (this.pathfinding) {
        return;
      }
      MightyMiner.executor().execute(() -> {
        log("creating thread");
        this.pathfinding = true;
        Pair<BlockPos, BlockPos> startEnd = this.pathQueue.poll();
        BlockPos start = startEnd.getFirst();
        BlockPos end = startEnd.getSecond();
        double walkSpeed = mc.thePlayer.getAIMoveSpeed();
        CalculationContext ctx = new CalculationContext(MightyMiner.instance, walkSpeed * 1.3, walkSpeed, walkSpeed * 0.3);
        finder = new AStarPathFinder(
            start.getX(), start.getY(), start.getZ(),
            new Goal(end.getX(), end.getY(), end.getZ(), ctx),
            ctx
        );
        Path path = finder.calculatePath();
        log("done pathfinding");
        if (path != null) {
          path.getSmoothedPath();
          PathExecutor.getInstance().queuePath(path);
          log("starting pathexec");
        } else {
          LogUtil.send("No Path Found");
          failed = true;
          stop();
        }
        this.pathfinding = false;
      });
    }
  }

  public boolean failed(){
    return !this.enabled && this.failed;
  }

  public boolean succeeded(){
    return !this.enabled && this.succeeded;
  }
}