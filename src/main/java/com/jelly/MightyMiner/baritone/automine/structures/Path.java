package com.jelly.MightyMiner.baritone.automine.structures;

import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathMode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedList;

@AllArgsConstructor
public class Path {
    public LinkedList<BlockNode> getBlocksInPath() {
        return blocksInPath;
    }

    LinkedList<BlockNode> blocksInPath;

    public PathMode getMode() {
        return mode;
    }

    PathMode mode;

}
