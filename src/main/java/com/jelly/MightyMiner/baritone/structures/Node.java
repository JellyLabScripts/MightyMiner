package com.jelly.MightyMiner.baritone.structures;

import net.minecraft.util.BlockPos;

public class Node {

    public Node(){
    }
    public Node(BlockPos blockPos){
        this.blockPos = blockPos;
    }
    public Node(BlockPos blockPos, boolean checked, boolean opened){
        this.blockPos = blockPos;
        this.checked = checked;
        this.opened = opened;
    }

    public double hValue;
    public double gValue;
    public double fValue;
    public Node lastNode;
    public BlockPos blockPos;
    public boolean checked;
    public boolean opened;




}
