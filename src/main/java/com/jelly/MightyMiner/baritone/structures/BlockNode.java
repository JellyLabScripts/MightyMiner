package com.jelly.MightyMiner.baritone.structures;

import net.minecraft.util.BlockPos;

public class BlockNode {
    BlockPos blockPos;
    BlockType blockType;

    public BlockNode(BlockPos blockPos, BlockType blockType) {
        this.blockPos = blockPos;
        this.blockType = blockType;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public BlockType getBlockType() {
        return blockType;
    }

    public void setBlockType(BlockType blockType) {
        this.blockType = blockType;
    }
}
