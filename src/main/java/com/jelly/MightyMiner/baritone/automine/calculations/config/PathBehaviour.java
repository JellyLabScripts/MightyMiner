package com.jelly.MightyMiner.baritone.automine.calculations.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.Block;

import java.util.List;

@AllArgsConstructor
public class PathBehaviour{
    @Getter
    List<Block> forbiddenMiningBlocks;
    @Getter
    List<Block> allowedMiningBlocks;
    @Getter
    int maxY;
    @Getter
    int minY;
    @Getter
    int searchRadius;
    @Getter
    boolean staticMode;
}
