package com.jelly.MightyMiner.baritone.automine.structures;

import net.minecraft.util.BlockPos;

public class BlockNode {
    BlockPos blockPos;
    BlockType blockType;

    boolean isFullPath; // only use in last blockNode of path

    public BlockNode(BlockPos blockPos, BlockType blockType) {
        this.blockPos = blockPos;
        this.blockType = blockType;
    }

    public BlockNode(boolean isFullPath) {
        this.isFullPath = isFullPath;

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

    public boolean isFullPath() {
        return isFullPath;
    }


}
