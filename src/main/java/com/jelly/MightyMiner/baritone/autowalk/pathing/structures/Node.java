package com.jelly.MightyMiner.baritone.autowalk.pathing.structures;

import com.jelly.MightyMiner.baritone.autowalk.movement.Moves;
import net.minecraft.util.BlockPos;

public class Node {
    public Node(BlockPos blockPos){
        this.blockPos = blockPos;
    }
    public double hValue;
    public double gValue;
    public double fValue;
    public Node lastNode;
    public Moves move;
    public BlockPos blockPos;
}
