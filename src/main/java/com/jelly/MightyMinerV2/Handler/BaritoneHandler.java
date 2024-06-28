package com.jelly.MightyMinerV2.Handler;

import baritone.api.IBaritone;
import baritone.api.behavior.IPathingBehavior;
import baritone.api.event.events.TickEvent;
import baritone.api.event.listener.AbstractGameEventListener;
import baritone.api.pathing.calc.IPathFinder;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import baritone.api.pathing.path.IPathExecutor;
import baritone.api.process.PathingCommand;
import baritone.api.process.PathingCommandType;
import baritone.api.utils.IPlayerContext;
import net.minecraft.util.BlockPos;

import java.util.*;
import java.util.stream.Collectors;

public class BaritoneHandler implements AbstractGameEventListener, IBaritoneHandler {

    private final IBaritone baritone;
    private final IPathingBehavior behavior;
    private final IPlayerContext ctx;

    private final Deque<Goal> pathQueue;

    public BaritoneHandler(IBaritone baritone) {
        this.baritone = baritone;
        this.behavior = baritone.getPathingBehavior();
        this.ctx = baritone.getPlayerContext();
        this.pathQueue = new ArrayDeque<>();
        baritone.getGameEventHandler().registerEventListener(this);
    }

    @Override
    public void onTick(TickEvent event) {
        if (event.getType() == TickEvent.Type.OUT) {
            pathQueue.clear();
            return;
        }

        if (pathQueue.isEmpty()) {
            return;
        }

        Optional<? extends IPathFinder> inProgress = behavior.getInProgress();
        if (inProgress.isPresent()) {
            return;
        }

        IPathExecutor current = behavior.getCurrent();
        if (current == null) {
            System.out.println("Normal");
            behavior.secretInternalSetGoalAndPath(new PathingCommand(pathQueue.getFirst(), PathingCommandType.SET_GOAL_AND_PATH));
            this.pathQueue.removeFirst();
            return;
        }

        if (behavior.getNext() != null) {
            return;
        }

        if (current.getPath().getGoal().heuristic(ctx.playerFeet()) < 20) {
            System.out.println("Lookahead");
            this.behavior.secretInternalSetGoalAndPath(new PathingCommand(pathQueue.getFirst(), PathingCommandType.SET_GOAL_AND_PATH));
            this.pathQueue.removeFirst();
        }
    }


    public void pathTo(BlockPos pos) {
        pathQueue.addLast(new GoalBlock(pos));
    }


    public void pathThrough(List<BlockPos> waypoints) {
        pathQueue.addAll(waypoints.stream().map(GoalBlock::new).collect(Collectors.toList()));
    }
}
