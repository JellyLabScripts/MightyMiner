package com.jelly.mightyminerv2.Util;

import com.jelly.mightyminerv2.MightyMiner;
import com.jelly.mightyminerv2.pathfinder.calculate.Path;
import com.jelly.mightyminerv2.pathfinder.calculate.path.AStarPathFinder;
import com.jelly.mightyminerv2.pathfinder.calculate.path.PathExecutor;
import com.jelly.mightyminerv2.pathfinder.goal.Goal;
import com.jelly.mightyminerv2.pathfinder.movement.CalculationContext;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;

public class PathfindUtil {

  private static final Minecraft mc = Minecraft.getMinecraft();
  private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, Long.MAX_VALUE, TimeUnit.DAYS, new LinkedBlockingQueue<>());
  private static volatile boolean processingPath = false;

  public static void queue(int x0, int y0, int z0, int x1, int y1, int z1, double walkSpeed) {
    executor.submit(() -> {
      LogUtil.log("Started Thread " + Thread.currentThread().getName());
      processingPath = true;
      CalculationContext ctx = new CalculationContext(MightyMiner.instance, walkSpeed * 1.3, walkSpeed, walkSpeed * 0.3);
      AStarPathFinder pathfinder = new AStarPathFinder(
          x0, y0, z0,
          new Goal(x1, y1, z1, ctx),
          ctx
      );
      Path path = pathfinder.calculatePath();
      if (path == null) {
        LogUtil.send("No Path Found");
        processingPath = false;
        return;
      }
//      path.getSmoothedPath();
      PathExecutor.INSTANCE.queuePath(path);
      if (executor.getActiveCount() + executor.getQueue().size() == 1) {
        processingPath = false;
      }
    });
  }

  public static void goTo(int x, int y, int z) {
    executor.submit(() -> {
      processingPath = true;
      BlockPos playerPos = PlayerUtil.getBlockStandingOn();
      double walkSpeed = mc.thePlayer.getAIMoveSpeed();
      CalculationContext ctx = new CalculationContext(MightyMiner.instance, walkSpeed * 1.3, walkSpeed, walkSpeed * 0.3);
      AStarPathFinder pathfinder = new AStarPathFinder(
          playerPos.getX(), playerPos.getY(), playerPos.getZ(),
          new Goal(x, y, z, ctx),
          ctx
      );
      Path path = pathfinder.calculatePath();
      if (path == null) {
        LogUtil.send("No Path Found");
        processingPath = false;
        return;
      }
      PathExecutor.INSTANCE.start(path);
      processingPath = false;
      LogUtil.log("Created Thread");
    });
  }

  public static void start() {
    executor.submit(() -> {
      PathExecutor.INSTANCE.start();
      processingPath = false;
    });
  }

  public static void stop() {
    PathExecutor.INSTANCE.stop();
  }

  public static boolean isPathexecEnabled() {
    return PathExecutor.INSTANCE.getEnabled();
  }

  public static boolean isProcessingPath() {
    return processingPath || PathExecutor.INSTANCE.getEnabled();
  }

  public static boolean pathfindFailed() {
    return !processingPath && PathExecutor.INSTANCE.failed();
  }

  public static boolean pathfindSucceeded() {
    return !processingPath && PathExecutor.INSTANCE.succeeded();
  }
}
