package com.jelly.MightyMinerV2.Handler;

import baritone.api.pathing.goals.Goal;
import net.minecraft.util.BlockPos;

import java.util.List;

public interface IBaritoneHandler {

    void pathTo(BlockPos pos);

    void pathThrough(List<BlockPos> waypoints);
}