package lol.simeon.humanoid.pathfinding.multinode


public class HeapOpenSet(maxHeapSize: Int) {
    private var nodes: Array<PathNode?>
    private var size = 0

    init {
        nodes = arrayOfNulls(maxHeapSize)
    }

    public fun add(node: PathNode) {
        if (isFull) {
            val length = nodes.size shl 1
            nodes = nodes.copyOf(length)
        }
        size++
        nodes[size] = node
        node.heapIndex = size
        update(node)
    }

    private fun update(n: PathNode) {
        var index: Int = n.heapIndex
        val cost: Double = n.getFinalExpense()
        var parentIndex = index ushr 1
        var parent: PathNode = nodes[parentIndex] ?: return
        while (index > 1 && parent.getFinalExpense() > cost) {
            nodes[index] = parent
            nodes[parentIndex] = n
            n.heapIndex = parentIndex
            parent.heapIndex = index
            index = parentIndex
            parentIndex = index ushr 1
            parent = nodes[parentIndex] ?: return
        }
    }

    public fun poll(): PathNode? {
        val node: PathNode? = nodes[1]
        val lastNode: PathNode? = nodes[size]
        nodes[1] = lastNode
        nodes[size] = null
        lastNode?.heapIndex = 1
        node?.heapIndex = -1
        size--
        if (size < 2) return node
        var index = 1
        var childIndex = 2
        val cost: Double = lastNode?.getFinalExpense() ?: return node
        while (true) {
            var child: PathNode? = nodes[childIndex]
            var childCost: Double = child?.getFinalExpense() ?: break
            if (childIndex < size) {
                val rightChild: PathNode? = nodes[childIndex + 1]
                val rightChildCost: Double = rightChild?.getFinalExpense() ?: break
                if (childCost > rightChildCost) {
                    childIndex++
                    child = rightChild
                    childCost = rightChildCost
                }
            }
            if (cost <= childCost) break
            nodes[index] = child
            nodes[childIndex] = lastNode
            lastNode.heapIndex = childIndex
            child.heapIndex = index
            index = childIndex
            childIndex = childIndex shl 1
            if (childIndex > size) break
        }
        return node
    }

    private val isFull: Boolean
        get() = size >= nodes.size - 1
    public val isEmpty: Boolean
        get() = size == 0

    public fun size(): Int {
        return size
    }
}