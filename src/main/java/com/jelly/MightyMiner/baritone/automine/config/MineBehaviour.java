package com.jelly.MightyMiner.baritone.automine.config;

import net.minecraft.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MineBehaviour {


    AutoMineType mineType;
    boolean shiftWhenMine;
    int rotationTime;
    int restartTimeThreshold;
    List<Block> forbiddenMiningBlocks;
    List<Block> allowedMiningBlocks;
    int maxY;
    int minY;


    public MineBehaviour(AutoMineType mineType, boolean shiftWhenMine, int rotationTime, int restartTimeThreshold, List<Block> forbiddenMiningBlocks, List<Block> allowedMiningBlocks, int maxY, int minY) {
        this.mineType = mineType;
        this.shiftWhenMine = shiftWhenMine;
        this.rotationTime = rotationTime;
        this.restartTimeThreshold = restartTimeThreshold;
        this.forbiddenMiningBlocks = forbiddenMiningBlocks;
        this.allowedMiningBlocks = allowedMiningBlocks;
        this.maxY = maxY;
        this.minY = minY;
    }

    public AutoMineType getMineType() {
        return mineType;
    }

    public boolean isShiftWhenMine() {
        return shiftWhenMine;
    }

    public int getRotationTime() {
        return rotationTime;
    }

    public int getRestartTimeThreshold() {
        return restartTimeThreshold;
    }

    @Nullable
    public List<Block> getForbiddenMiningBlocks() {
        return forbiddenMiningBlocks;
    }

    @Nullable
    public List<Block> getAllowedMiningBlocks() {
        return allowedMiningBlocks;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getMinY() {
        return minY;
    }
}
