package com.jelly.MightyMinerV2.Util.helper.heap;

import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Starts from 1
public class MinHeap {
    private HeapNode[] items;
    private int size;
    private int capacity;

    public MinHeap(int capacity) {
        this.capacity = capacity;
        this.items = new HeapNode[this.capacity];
        this.size = 0;
    }

    public void add(BlockPos pos, double cost) {
        add(new HeapNode(pos, cost));
    }

    public void add(HeapNode elem) {
        if (this.size >= capacity) {
            this.capacity *= 2;
            this.items = Arrays.copyOf(this.items, this.capacity);
        }
        this.items[++this.size] = elem;
        this.heapUp(this.size - 1);
    }

    public void heapUp(int index) {
        int parentIndex = index >>> 1;
        while (parentIndex > 0 && this.items[index].nodeCost < this.items[parentIndex].nodeCost) {
            swap(parentIndex, index);
            index = parentIndex;
            parentIndex = index >>> 1;
        }
    }

    public void swap(int i1, int i2) {
        HeapNode temp = this.items[i1];
        this.items[i1] = this.items[i2];
        this.items[i2] = temp;
    }

    public List<BlockPos> getBlocks() {
        final List<BlockPos> blocks = new ArrayList<>();
        for (int i = 1; i < this.items.length; i++) {
            final HeapNode node = this.items[i];
            if (node == null) break;
            blocks.add(node.nodePos);
        }
        return blocks;
    }
}
