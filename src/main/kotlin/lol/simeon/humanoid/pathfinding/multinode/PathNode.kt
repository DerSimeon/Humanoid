package lol.simeon.humanoid.pathfinding.multinode

import net.minecraft.core.BlockPos
import org.bukkit.Location
import org.bukkit.World
import kotlin.math.pow
import kotlin.math.sqrt

public class PathNode(public val loc: BlockPos, public var expense: Double, public var origin: PathNode?, public var context: PathFindingContext) :
    Comparable<PathNode> {

    public var expenseLeft: Double = -1.0
    public var heapIndex: Int = 0

    override fun compareTo(other: PathNode): Int {
        var compare = getFinalExpense().compareTo(other.getFinalExpense())
        if (compare == 0) compare = expense.compareTo(other.expense)
        return -compare
    }

    public fun getFinalExpense(): Double {
        if (expenseLeft == -1.0) expenseLeft = LocationUtils.euclideanDistance(loc, context.endLocation)
        return expense + 1.1 * expenseLeft
    }

    private fun addNode(locThere: BlockPos, expenseThere: Double) {
        val node: PathNode = context.getNode(locThere)
        if (node.origin == null && node !== context.startNode) { // new node
            node.expense = expenseThere
            node.origin = this
            context.openSet.add(node)
            return
        }
        if (node.expense > expenseThere) {
            node.expense = expenseThere
            node.origin = this
        }
    }

    public fun getReachableLocations() {
        for (x in -1..1) {
            for (z in -1..1) {
                // check is current node location or diagonal
                if ((x == 0 && z == 0) || (!context.settings.useDiagonal && x * z != 0)) continue
                val loc = loc.add(x, 0, z)

                // check if outside of pathfinder chunk
                val chunkSize = context.settings.chunkSize
                val outsideRange = context.startLocation.distance(loc) > chunkSize
                if (outsideRange && context.settings.useChunking) {
                    continue
                }

                // movement
                if (LocationUtils.canStandAt(loc.toLocation(context.world!!))) {
                    if (x * z != 0) { // if diagonal movement
                        val xDelta = loc.x - this.loc.x
                        val zDelta = loc.z - this.loc.z
                        var potentialWall = BlockPos(xDelta + x, this.loc.y, this.loc.z)
                        if (LocationUtils.isSolid(potentialWall, context.world)) continue
                        potentialWall = BlockPos(this.loc.x, this.loc.y, zDelta + z)
                        if (LocationUtils.isSolid(potentialWall, context.world)) continue
                        addNode(loc, expense + context.settings.costDiagonal)
                    } else {
                        addNode(loc, expense + context.settings.costForward)
                    }
                }

                // jumping
                if (!LocationUtils.isSolid(loc.add(-x, 2, -z), context.world)) {
                    val upLoc = loc.above()
                    if (LocationUtils.canStandAt(upLoc, context.world)) {
                        if (upLoc.toLocation(context.world!!).block.type.name.contains("STAIRS")) {
                            addNode(upLoc, expense + context.settings.costStairs)
                        } else {
                            addNode(upLoc, expense + context.settings.costJump)
                        }
                    }
                }

                // falling
                if (!LocationUtils.isSolid(loc.add(0, 1, 0), context.world)) { // block above possible new tile
                    val nLoc = loc.add(0, -1, 0)
                    if (LocationUtils.canStandAt(nLoc, context.world)) {
                        addNode(nLoc, expense + context.settings.costFall) // one block down
                    } else if (!LocationUtils.isSolid(nLoc, context.world) && !LocationUtils.isSolid(nLoc.clone().add(0, 1, 0), context.world)) { // fall
                        var drop = 1
                        while (drop <= context.settings.maxFall && !LocationUtils.isSolid(loc.clone().add(0, -drop, 0), context.world)) {
                            val dropLoc = loc.clone().add(0, -drop, 0)
                            if (LocationUtils.canStandAt(dropLoc, context.world)) {
                                val fallNode = createNode(loc, expense + 1)
                                fallNode.addNode(dropLoc, expense + (drop * context.settings.costFall))
                            }
                            drop++
                        }
                    }
                }

                // climbing
                if (context.settings.canClimb) {
                    if (LocationUtils.isClimbable(loc.add(-x, 0, -z), context.world)) {
                        val nLoc = loc.add(-x, 0, -z)
                        var up = 1
                        while (LocationUtils.isClimbable(nLoc.clone().add(0, up, 0), context.world)) up++
                        addNode(nLoc.clone().add(0, up, 0), expense + (up * context.settings.costClimb))
                    }
                }

                // parkour
                // the furthest jump possible: 4 blocks long and 1 block higher
                if (context.settings.canParkour) {
                    var nLoc = loc.above()
                    if (LocationUtils.canStandAt(nLoc, context.world)) {
                        var jumpLength = 1
                        while (jumpLength <= context.settings.maxParkour && !LocationUtils.isSolid(nLoc.add(x, 0, z), context.world)) {
                            nLoc = nLoc.clone().add(x, 0, z)
                            jumpLength++
                        }
                        if (jumpLength > 1) {
                            addNode(nLoc, expense + (jumpLength * context.settings.costParkour))
                        }
                    }
                }
            }
        }
    }


    private fun createNode(loc: BlockPos, expense: Double): PathNode {
        return PathNode(loc, expense, this, context)
    }
}

public fun BlockPos.add(x: Int, y: Int, z: Int): BlockPos {
    return BlockPos(this.x + x, this.y + y, this.z + z)
}

public fun BlockPos.toLocation(world: World): Location {
    return Location(world, this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
}

public fun Location.toBlockPos(): BlockPos {
    return BlockPos(this.x.toInt(), this.y.toInt(), this.z.toInt())
}

public fun BlockPos.clone(): BlockPos {
    return BlockPos(this.x, this.y, this.z)
}

public fun BlockPos.distance(other: BlockPos): Double {
    return sqrt(
        (x - other.x).toDouble().pow(2.0) + (y - other.y).toDouble().pow(2.0) + (z - other.z).toDouble().pow(2.0)
    )
}