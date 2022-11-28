package com.jelly.MightyMiner.baritone.automine.structures;

import com.jelly.MightyMiner.baritone.automine.calculations.behaviour.PathMode;
import lombok.AllArgsConstructor;

import java.util.LinkedList;

public class SemiPath extends Path{
    public SemiPath(LinkedList<BlockNode> blocksInPath, PathMode mode) {
        super(blocksInPath, mode);
    }
}
