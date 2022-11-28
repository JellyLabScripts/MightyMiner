package com.jelly.MightyMiner.baritone.automine.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.Block;

import java.util.List;

@AllArgsConstructor
public class BaritoneConfig {
    @Getter
    MiningType mineType;
    @Getter
    boolean shiftWhenMine;
    @Getter
    boolean mineFloor;
    @Getter
    boolean mineWithPreference;
    @Getter
    int mineRotationTime;
    @Getter
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
