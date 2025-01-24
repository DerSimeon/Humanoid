/*
 * MIT License
 *
 * Copyright (c) 2025 Simeon L.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package lol.simeon.humanoid.pathfinding.singlenode

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lol.simeon.humanoid.pathfinding.HumanoidNode
import lol.simeon.humanoid.pathfinding.HumanoidPathFinder
import lol.simeon.humanoid.pathfinding.HumanoidPathFindingResult
import lol.simeon.humanoid.pathfinding.HumanoidPathFindingState
import org.bukkit.Location
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.abs

public class HumanoidSingleNodePathFinder(private val world: String) : HumanoidPathFinder {

    private val directions = listOf(
        Vector(1.0, 0.0, 0.0),  // East
        Vector(-1.0, 0.0, 0.0), // West
        Vector(0.0, 0.0, 1.0),  // South
        Vector(0.0, 0.0, -1.0)  // North
    )

    public override suspend fun findPath(start: Location, end: Location): HumanoidPathFindingResult = withContext(Dispatchers.IO) {
        val openSet = PriorityQueue<HumanoidNode>(compareBy { it.fCost })
        val closedSet = mutableSetOf<Location>()

        val startNode = HumanoidNode(start, 0.0, heuristic(start, end), null)
        openSet.add(startNode)

        while (openSet.isNotEmpty()) {
            val currentNode = openSet.poll()

            if (currentNode.location.isSameBlock(end)) {
                return@withContext HumanoidPathFindingResult(
                    HumanoidPathFindingState.COMPLETED,
                    constructPath(currentNode)
                )
            }

            closedSet.add(currentNode.location)

            for (direction in directions) {
                val neighborLocation = currentNode.location.clone().add(direction)
                val jumpLocation = neighborLocation.clone().add(0.0, 1.0, 0.0)

                if (closedSet.contains(neighborLocation)) continue

                val (isWalkable, isJumpable) = checkWalkableAndJumpable(neighborLocation, jumpLocation)

                if (!isWalkable && !isJumpable) continue

                val gCost = currentNode.gCost + currentNode.location.distance(neighborLocation)
                val hCost = heuristic(neighborLocation, end)
                val neighborNode = HumanoidNode(
                    if (isJumpable) jumpLocation else neighborLocation,
                    gCost,
                    hCost,
                    currentNode
                )

                if (openSet.none { it.location == neighborNode.location && it.gCost <= gCost }) {
                    openSet.add(neighborNode)
                }
            }
        }

        return@withContext HumanoidPathFindingResult(HumanoidPathFindingState.FAILED)
    }

    private fun checkWalkableAndJumpable(neighbor: Location, jump: Location): Pair<Boolean, Boolean> {
        val isNeighborSolid = neighbor.block.type.isSolid
        val isJumpSolid = jump.block.type.isSolid
        val isBelowNeighborSolid = neighbor.clone().add(0.0, -1.0, 0.0).block.type.isSolid
        val isBelowJumpSolid = jump.clone().add(0.0, -1.0, 0.0).block.type.isSolid

        val isWalkable = !isNeighborSolid && isBelowNeighborSolid
        val isJumpable = !isJumpSolid && isBelowJumpSolid

        return Pair(isWalkable, isJumpable)
    }

    private fun Location.isSameBlock(other: Location): Boolean {
        return this.blockX == other.blockX && this.blockY == other.blockY && this.blockZ == other.blockZ
    }

    private fun constructPath(node: HumanoidNode): List<Location> {
        val path = mutableListOf<Location>()
        var currentNode: HumanoidNode? = node
        while (currentNode != null) {
            path.add(0, currentNode.location)
            currentNode = currentNode.parent
        }
        return path
    }

    private fun heuristic(start: Location, end: Location): Double {
        return abs(start.x - end.x) + abs(start.y - end.y) + abs(start.z - end.z)
    }

}