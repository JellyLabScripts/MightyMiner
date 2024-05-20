package com.jelly.MightyMinerV2.Util.helper.heap;

public class HeapNode<T> {
    public final T nodeVal;
    public final double nodeCost;

    public HeapNode(final T nodeVal, final double nodeCost) {
        this.nodeVal = nodeVal;
        this.nodeCost = nodeCost;
    }
}
