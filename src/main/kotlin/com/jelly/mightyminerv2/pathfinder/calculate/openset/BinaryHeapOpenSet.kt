package com.jelly.mightyminerv2.pathfinder.calculate.openset

import com.jelly.mightyminerv2.pathfinder.calculate.PathNode
import java.util.*

class BinaryHeapOpenSet(initialSize: Int = 1024) {
    var items = arrayOfNulls<PathNode>(initialSize)
    var size = 0

    fun add(node: PathNode) {
        if (size >= items.size - 1) {
            items = Arrays.copyOf(items, items.size shl 1)
        }
        node.heapPosition = ++size
        items[size] = node
        relocate(node)
    }

    fun relocate(node: PathNode) {
        var parent = node.heapPosition ushr 1
        var parentNode = items[parent]
        while (node.heapPosition > 1 && node.totalCost < parentNode!!.totalCost) {
            items[node.heapPosition] = parentNode
            items[parent] = node
            node.heapPosition = parent
            parent = parent ushr 1
            parentNode = items[parent]
        }
    }

    fun poll(): PathNode {
        val itemToPoll = items[1]
        itemToPoll!!.heapPosition = -1
        val itemToSwap = items[size--]
        itemToSwap!!.heapPosition = 1
        items[1] = itemToSwap
        val itemToSwapCost = itemToSwap.totalCost

        if (size <= 1) return itemToPoll

        var parentIndex = 1
        var smallestChildIndex = 2
        while (smallestChildIndex <= size) {
            val rightChildIndex = smallestChildIndex + 1
            if (rightChildIndex < size && items[rightChildIndex]!!.totalCost < items[smallestChildIndex]!!.totalCost) {
                smallestChildIndex = rightChildIndex
            }

            if (items[smallestChildIndex]!!.totalCost >= itemToSwapCost) {
                break
            }

            val swapTemp = items[smallestChildIndex]
            swapTemp!!.heapPosition = parentIndex
            items[parentIndex] = swapTemp
            itemToSwap.heapPosition = smallestChildIndex
            items[smallestChildIndex] = itemToSwap

            parentIndex = smallestChildIndex
            smallestChildIndex = parentIndex shl 1
        }
        return itemToPoll
    }

    fun isEmpty() = size <= 0
}