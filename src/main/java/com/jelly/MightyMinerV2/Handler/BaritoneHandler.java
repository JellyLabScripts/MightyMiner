package com.jelly.MightyMinerV2.Handler;

import baritone.api.BaritoneAPI;
import baritone.api.event.events.PathEvent;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.goals.GoalNear;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import com.jelly.MightyMinerV2.Config.MightyMinerConfig;
import com.jelly.MightyMinerV2.Event.BaritoneEventListener;
import com.jelly.MightyMinerV2.Util.LogUtil;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BaritoneHandler {
    private static final Minecraft mc = Minecraft.getMinecraft();
    @Getter
    public static boolean pathing = false;
    private static List<BlockPos> waypoints;
    private static int currentWaypointIndex = 0;
    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    static {
        BaritoneAPI.getSettings().chatDebug.value = true; // Enable chat debug
        BaritoneAPI.getSettings().allowDiagonalAscend.value = true;
        BaritoneAPI.getSettings().pathingMapDefaultSize.value = 2048;
        BaritoneAPI.getSettings().maxFallHeightNoWater.value = 50;
        List<Block> blacklist = Arrays.asList(Blocks.oak_fence, Blocks.oak_fence_gate);
        BaritoneAPI.getSettings().blocksToAvoid.value = blacklist;
        BaritoneAPI.getSettings().costHeuristic.value = 50.0;
        BaritoneAPI.getSettings().yawSmoothingFactor.value = (float) MightyMinerConfig.yawsmoothingfactor;
        BaritoneAPI.getSettings().pitchSmoothingFactor.value = (float) MightyMinerConfig.pitchsmoothingfactor;
    }

    public static void walkThroughWaypoints(List<BlockPos> waypoints) {
        BaritoneHandler.waypoints = waypoints;
        currentWaypointIndex = 0;

        // Print the list of waypoints in the chat
        StringBuilder waypointsString = new StringBuilder("Waypoints: ");
        for (BlockPos waypoint : waypoints) {
            waypointsString.append(waypoint.toString()).append(" -> ");
        }
        waypointsString.setLength(waypointsString.length() - 4); // Remove the last " -> "
        LogUtil.send(waypointsString.toString(), LogUtil.ELogType.SUCCESS);

        // Start the pathing process
        walkToNextWaypoint();
    }

    private static void walkToNextWaypoint() {
        if (currentWaypointIndex >= waypoints.size()) {
            stopPathing();
            return;
        }

        BlockPos waypoint = waypoints.get(currentWaypointIndex);
        walkToBlockPos(waypoint);

        // Schedule periodic checks
        scheduler.scheduleAtFixedRate(() -> {
            try {
                checkPathingStatus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    private static void checkPathingStatus() {
        if (!pathing) return;

        if (isWalkingToGoalBlock(0.5)) {
            if (hasFailed()) {
                LogUtil.send("Pathing failed to waypoint: " + waypoints.get(currentWaypointIndex).toString(), LogUtil.ELogType.ERROR);
                stopPathing();
            }
        } else {
            LogUtil.send("Reached waypoint: " + waypoints.get(currentWaypointIndex).toString(), LogUtil.ELogType.SUCCESS);
            currentWaypointIndex++;
            if (currentWaypointIndex < waypoints.size()) {
                walkToNextWaypoint();
            } else {
                stopPathing();
            }
        }
    }

    public static boolean isWalkingToGoalBlock() {
        return isWalkingToGoalBlock(0.75);
    }

    public static boolean isWalkingToGoalBlock(double nearGoalDistance) {
        if (pathing) {
            if (!mc.thePlayer.onGround) return true;
            double distance;
            Goal goal = BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().getGoal();
            if (goal instanceof GoalBlock) {
                GoalBlock goal1 = (GoalBlock) BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().getGoal();
                distance = mc.thePlayer.getDistance(goal1.getGoalPos().getX() + 0.5f, mc.thePlayer.posY, goal1.getGoalPos().getZ() + 0.5);
            } else if (goal instanceof GoalNear) {
                GoalNear goal1 = (GoalNear) BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().getGoal();
                distance = mc.thePlayer.getDistance(goal1.getGoalPos().getX() + 0.5f, mc.thePlayer.posY, goal1.getGoalPos().getZ() + 0.5);
            } else {
                distance = goal.isInGoal(mc.thePlayer.getPosition()) ? 0 : goal.heuristic();
            }
            if (distance <= nearGoalDistance || BaritoneEventListener.pathEvent == PathEvent.AT_GOAL) {
                BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
                pathing = false;
                return false;
            }
            return BaritoneEventListener.pathEvent != PathEvent.CANCELED;
        }
        return false;
    }

    public static boolean hasFailed() {
        if (BaritoneEventListener.pathEvent == PathEvent.CALC_FAILED
                || BaritoneEventListener.pathEvent == PathEvent.NEXT_CALC_FAILED) {
            BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
            pathing = false;
            return true;
        }
        return false;
    }

    public static void walkToBlockPos(BlockPos blockPos) {
        PathingCommand pathingCommand = new PathingCommand(new GoalBlock(blockPos), PathingCommandType.REVALIDATE_GOAL_AND_PATH);
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().secretInternalSetGoalAndPath(pathingCommand);
        pathing = true;
    }

    public static void stopPathing() {
        BaritoneAPI.getProvider().getPrimaryBaritone().getPathingBehavior().cancelEverything();
        pathing = false;
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }
}
