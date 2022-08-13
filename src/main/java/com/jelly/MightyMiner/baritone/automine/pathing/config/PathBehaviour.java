package com.jelly.MightyMiner.baritone.automine.pathing.config;

import com.jelly.MightyMiner.baritone.automine.config.MineBehaviour;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;

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
