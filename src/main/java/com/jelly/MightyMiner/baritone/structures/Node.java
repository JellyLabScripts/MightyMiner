package com.jelly.MightyMiner.baritone.structures;

import net.minecraft.util.BlockPos;

public class Node {
    public Node(BlockPos blockPos){
        this.blockPos = blockPos;
    }
    public double hValue;
    public double gValue;
    public double fValue;
    public Node lastNode;
    public BlockPos blockPos;
}
