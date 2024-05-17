package com.jelly.MightyMinerV2.Util.helper.heap;

import net.minecraft.util.BlockPos;

public class HeapNode {
    public final BlockPos nodePos;
    public final double nodeCost;

    public HeapNode(final BlockPos nodePos, final double nodeCost) {
        this.nodePos = nodePos;
        this.nodeCost = nodeCost;
    }
}
