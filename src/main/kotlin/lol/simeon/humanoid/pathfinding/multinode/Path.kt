package lol.simeon.humanoid.pathfinding.multinode

import lol.simeon.humanoid.npc.HumanoidNPC
import org.bukkit.util.Vector


public class Path(nodes: Array<PathNode?>) {
    private var nodes: Array<PathNode?>
    private var nextNodeIndex = 0

    init {
        this.nodes = nodes
    }

    private fun getNode(index: Int): PathNode? {
        return nodes[index]
    }

    public fun getNodes(): Array<PathNode?> {
        return nodes
    }

    public fun size(): Int {
        return nodes.size
    }

    private fun getNPCPosAtNode(npc: HumanoidNPC, index: Int): Vector {
        return try {
            val node: PathNode = getNode(index) ?: return npc.getLocation().toVector()
            val x: Double = node.loc.x + 0.5
            val y: Double = node.loc.y.toDouble()
            val z: Double = node.loc.z + 0.5
            Vector(x, y, z)
        } catch (e: IndexOutOfBoundsException) {
            npc.getLocation().toVector()
        }
    }

    public fun getNextNPCPos(npc: HumanoidNPC): Vector {
        return getNPCPosAtNode(npc, nextNodeIndex)
    }

    public val nextNodePos: Vector
        get() = getNodePos(nextNodeIndex)

    private fun getNodePos(index: Int): Vector {
        val node: PathNode = getNode(index) ?: return Vector(0, 0, 0)
        return Vector(node.loc.x, node.loc.y, node.loc.z)
    }

    public fun advance() {
        ++nextNodeIndex
    }

    public fun notStarted(): Boolean {
        return nextNodeIndex <= 0
    }

    public val isDone: Boolean
        get() = nextNodeIndex >= nodes.size || nodes.isEmpty()

    public fun replaceNode(index: Int, node: PathNode?) {
        nodes[index] = node
    }

    public fun clean() {
        if (nodes.isEmpty()) return
        val newNodes: Array<PathNode?> = arrayOfNulls(nodes.size - 1)
        System.arraycopy(nodes, 1, newNodes, 0, newNodes.size)
        nodes = newNodes
    }
}