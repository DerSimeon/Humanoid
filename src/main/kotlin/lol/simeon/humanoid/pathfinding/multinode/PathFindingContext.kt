package lol.simeon.humanoid.pathfinding.multinode

import net.minecraft.core.BlockPos
import org.bukkit.World


public class PathFindingContext(public val startLocation: BlockPos, public val endLocation: BlockPos, public val world: World, public val settings: PathFindingSettings) {

    public var startNode: PathNode? = PathNode(startLocation, 0.0, null, this)
    public var endNode: PathNode? = PathNode(endLocation, 0.0, null, this)
    public var closedSet: Set<PathNode> = HashSet()
    public val openSet: HeapOpenSet = HeapOpenSet(1024)

    init {
        openSet.add(startNode!!)
    }

    public fun getNode(location: BlockPos): PathNode {
        return closedSet.find { it.loc == location } ?: PathNode(location, 0.0, null, this)
    }

}