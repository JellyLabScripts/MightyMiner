package com.jelly.MightyMiner.baritone.automine.structures;

import com.jelly.MightyMiner.baritone.automine.calculations.config.PathMode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedList;

@AllArgsConstructor
public class Path {
    @Getter
    LinkedList<BlockNode> blocksInPath;
    @Getter
    PathMode mode;

}
