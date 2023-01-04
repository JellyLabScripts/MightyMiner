package com.jelly.MightyMiner.baritone.automine.movementgrapth.graph.node

open class GraphNode{
    val children: MutableList<GraphNode> = ArrayList()

    var parent: GraphNode? = null
        private set

    /**
     * add the [child] node to the chain
     * @return the child node (for chaining purposes)
     */
    fun createChild(child: GraphNode): GraphNode {
        children.add(child)
        child.parent = this
        return child
    }

    /**
     * in a chain of nodes
     * `... [parent] -> this -> [children] ...`
     * add [replacement] above, so it becomes:
     * `... [parent] -> [replacement] -> this -> [children] ...`
     *
     * @return return [replacement] since it takes this node's place in the chain
     */
    fun pushDown(replacement: GraphNode): GraphNode{
        replacement.parent = parent
        parent?.children?.remove(this)
        parent?.children?.add(replacement)

        replacement.children.add(this)

        return replacement
    }

    /**
     * in a chain of nodes
     * `... [parent] -> this -> [children] ...`
     * add [replacement] below, so it becomes:
     * `... [parent] -> this -> [replacement] -> [children] ...`
     *
     * @return return this since it stays in place
     */
    fun pushUp(replacement: GraphNode): GraphNode{
        replacement.children.addAll(children)
        children.clear()
        children.add(replacement)
        replacement.parent = this
        return this
    }


    fun getRoot(): GraphNode{
        var node = this
        while(node.parent != null){
            node = node.parent!!
        }
        return node
    }

    fun getAllNodes(): List<GraphNode>{
        val nodes = ArrayList<GraphNode>()
        nodes.add(this)
        for(child in children){
            nodes.addAll(child.getAllNodes())
        }
        return nodes
    }



}