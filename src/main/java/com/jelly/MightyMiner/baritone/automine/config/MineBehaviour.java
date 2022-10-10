package com.jelly.MightyMiner.baritone.automine.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.Block;

import java.util.List;

@AllArgsConstructor
public class MineBehaviour {
    @Getter
    AutoMineType mineType;
    @Getter
    boolean shiftWhenMine;
    @Getter
    boolean mineFloor;
    @Getter
    boolean mineWithPreference;
    @Getter
    int rotationTime;
    @Getter
    int restartTimeThreshold;
    @Getter
    List<Block> forbiddenMiningBlocks;
    @Getter
    List<Block> allowedMiningBlocks;
    @Getter
    int maxY;
    @Getter
    int minY;
}
