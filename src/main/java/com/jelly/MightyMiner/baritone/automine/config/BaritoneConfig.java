package com.jelly.MightyMiner.baritone.automine.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.Block;

import java.util.List;

@AllArgsConstructor
public class BaritoneConfig {
    public MiningType getMineType() {
        return mineType;
    }

    MiningType mineType;

    public boolean isShiftWhenMine() {
        return shiftWhenMine;
    }

    boolean shiftWhenMine;
    @Getter
    boolean mineFloor;
    @Getter
    boolean mineWithPreference;
    @Getter
    int mineRotationTime;

    public int getRestartTimeThreshold() {
        return restartTimeThreshold;
    }

    int restartTimeThreshold;
    @Getter
    List<Block> forbiddenPathfindingBlocks;
    @Getter
    List<Block> allowedPathfindingBlocks;
    @Getter
    int maxY;
    @Getter
    int minY;

}
