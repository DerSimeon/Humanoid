package lol.simeon.humanoid.pathfinding.multinode

import lol.simeon.humanoid.console.HumanoidLogger
import lol.simeon.humanoid.pathfinding.HumanoidPathFinder
import lol.simeon.humanoid.pathfinding.HumanoidPathFindingResult
import lol.simeon.humanoid.pathfinding.HumanoidPathFindingState
import net.minecraft.core.BlockPos
import org.bukkit.Location
import org.bukkit.util.Vector

public class PathFinder : HumanoidPathFinder {

    private val pathFindingSettings = PathFindingSettings()

    override suspend fun findPath(startLocation: Location, endLocation: Location): HumanoidPathFindingResult {
        val start = startLocation.toBlockPos()
        val end = endLocation.toBlockPos()
        val world = startLocation.world ?: return HumanoidPathFindingResult(HumanoidPathFindingState.FAILED)
        val context = PathFindingContext(start, end, world, pathFindingSettings)

        var pathFound = pathFindingSettings.useChunking
        val nsStart = System.nanoTime()
        var best: PathNode? = null

        while (context.closedSet.size < pathFindingSettings.maxNodesToTest && !context.openSet.isEmpty) {
            best = context.openSet.poll() ?: break

            if (best.expenseLeft < 1) {
                pathFound = true
                context.endNode = best
                HumanoidLogger.info("Path found: Checked ${context.closedSet.size} nodes, Final Expense: ${best.getFinalExpense()}")
                break
            }

            best.getReachableLocations()
            context.closedSet += best
        }

        if (pathFindingSettings.useChunking && best != null) {
            context.endNode = best
        }

        if (!pathFound) {
            logDuration(nsStart, "Pathfinding failed.")
            return HumanoidPathFindingResult(HumanoidPathFindingState.IDLE)
        }


        val nodes = reconstructPath(context.endNode)
        val smoothedNodes = smoothPath(nodes)

        if (smoothedNodes.isEmpty() || smoothedNodes.last().loc.distance(context.endLocation) > 1) {
            HumanoidLogger.warn("Pathfinding failed: Last node is not the end node.")
            return HumanoidPathFindingResult(HumanoidPathFindingState.FAILED)
        }

        logDuration(nsStart, "Pathfinding succeeded: Path length ${smoothedNodes.size}.")

        return HumanoidPathFindingResult(
            HumanoidPathFindingState.COMPLETED,
            smoothedNodes.map { adjustToGround(it.loc.toLocation(world)) }
        )
    }

    private fun logDuration(startTime: Long, message: String) {
        val duration = (System.nanoTime() - startTime) / 1_000_000.0
        HumanoidLogger.info("$message Took $duration ms.")
    }

    private fun reconstructPath(endNode: PathNode?): List<PathNode> {
        val path = mutableListOf<PathNode>()
        var currentNode = endNode
        while (currentNode != null) {
            path.add(0, currentNode)
            currentNode = currentNode.origin
            if(currentNode != currentNode?.origin) {
                println("currentNode: $currentNode")
                println("currentNode?.origin: ${currentNode?.origin}")
            }
        }
        return path
    }

    private fun smoothPath(nodes: List<PathNode>): List<PathNode> {
        val smoothed = mutableListOf<PathNode>()
        for (i in 0 until nodes.size - 1) {
            val current = nodes[i]
            val next = nodes[i + 1]
            smoothed.add(current)

            val interpolated = interpolateNodes(current, next, 4) // Adding 4 intermediate nodes
            smoothed.addAll(interpolated)
        }
        smoothed.add(nodes.last())
        return smoothed
    }

    private fun interpolateNodes(start: PathNode, end: PathNode, count: Int): List<PathNode> {
        val interpolated = mutableListOf<PathNode>()
        val startVec = Vector(start.loc.x.toDouble(), start.loc.y.toDouble(), start.loc.z.toDouble())
        val endVec = Vector(end.loc.x.toDouble(), end.loc.y.toDouble(), end.loc.z.toDouble())
        val step = endVec.clone().subtract(startVec).multiply(1.0 / (count + 1))

        for (i in 1..count) {
            val interpolatedVec = startVec.clone().add(step.clone().multiply(i))
            val blockPos = BlockPos(
                interpolatedVec.blockX,
                interpolatedVec.blockY,
                interpolatedVec.blockZ
            )
            interpolated.add(PathNode(blockPos, 0.0, null, start.context))
        }
        return interpolated
    }

    private fun adjustToGround(location: Location): Location {
        val blockBelow = location.clone().add(0.0, -1.0, 0.0)
        if (blockBelow.block.type.isSolid) {
            location.y = blockBelow.y + 1.0 // Adjust to the top of the block
        }
        return location
    }

}