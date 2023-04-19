package com.jelly.MightyMiner.baritone.automine.structures;

import net.minecraft.util.BlockPos;

public class Node {
    public Node(BlockPos pos){
        this.pos = pos;
    }

    public BlockPos pos;

    public double hCost;
    public double gCost = -1;
    public double fCost;
    public Node parentNode;
}
