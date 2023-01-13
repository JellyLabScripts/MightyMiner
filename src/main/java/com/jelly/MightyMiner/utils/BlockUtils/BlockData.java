package com.jelly.MightyMiner.utils.BlockUtils;

import net.minecraft.block.Block;

public class BlockData<T> {
    public Block block;
    public T requiredBlockStateValue;

    public BlockData(Block block, T requiredBlockStateValue) {
        this.block = block;
        this.requiredBlockStateValue = requiredBlockStateValue;
    }
}
