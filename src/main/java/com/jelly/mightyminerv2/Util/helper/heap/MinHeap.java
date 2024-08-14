package com.jelly.mightyminerv2.Util.helper.heap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// Starts from 1
public class MinHeap<T> {

  private HeapNode<T>[] items;
  private int size;
  private int capacity;

  public MinHeap(int capacity) {
    this.capacity = capacity;
    this.items = new HeapNode[this.capacity];
    this.size = 0;
  }

  public T poll() {
    HeapNode<T> root = items[1];
    items[1] = items[size];
    items[size] = null;
    size--;

    int index = 1;
    int small;
    while ((small = index << 1) <= size) {
      if (small < size && items[small].nodeCost > items[small + 1].nodeCost) {
        small++;
      }

      if (items[index].nodeCost > items[small].nodeCost) {
        HeapNode<T> temp = items[index];
        items[index] = items[small];
        items[small] = temp;
        index = small;
      } else {
        break;
      }
    }

    // should throw exception but oh well
    return root == null ? null : root.nodeVal;
  }

  private void heapDown(int index) {
  }

  public void add(T pos, double cost) {
    add(new HeapNode<>(pos, cost));
  }

  public void add(HeapNode<T> elem) {
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
    HeapNode<T> temp = this.items[i1];
    this.items[i1] = this.items[i2];
    this.items[i2] = temp;
  }

  public List<T> getBlocks() {
    final List<T> blocks = new ArrayList<>();
    for (int i = 1; i < this.items.length; i++) {
      final HeapNode<T> node = this.items[i];
      if (node == null) {
        break;
      }
      blocks.add(node.nodeVal);
    }
    return blocks;
  }
}
