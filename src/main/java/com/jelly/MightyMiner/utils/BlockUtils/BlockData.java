package com.jelly.MightyMiner.utils.BlockUtils;

import net.minecraft.block.Block;

public class BlockData<T> {
    public Block block;

    //Must specify which type
    public T requiredBlockStateValue;


    public BlockData(Block block) {
        this.block = block;
        this.requiredBlockStateValue = null;
    }

    public BlockData(Block block, T requiredBlockStateValue) {
        this.block = block;
        this.requiredBlockStateValue = requiredBlockStateValue;
    }
}
