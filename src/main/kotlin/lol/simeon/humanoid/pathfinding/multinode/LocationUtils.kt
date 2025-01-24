package lol.simeon.humanoid.pathfinding.multinode

import net.minecraft.core.BlockPos
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.abs
import kotlin.math.sqrt


public object LocationUtils {

    public fun canStandAt(pos: BlockPos, world: World?): Boolean {
        return canStandAt(pos.toLocation(world!!))
    }

    public fun canStandAt(location: Location): Boolean {
        return !isSolid(location) && !isSolid(location.clone().add(0.0, 1.0, 0.0)) &&
                isSolid(location.clone().add(0.0, -1.0, 0.0))
    }

    public fun isSolid(pos: BlockPos, world: World?): Boolean {
        return isSolid(pos.toLocation(world!!))
    }

    private fun isSolid(location: Location): Boolean {
        val type = location.block.type
        return if (type.name.contains("LEAVES")) true else type.isSolid && !type.name.contains("TRAPDOOR")
    }

    public fun isClimbable(pos: BlockPos, world: World?): Boolean {
        return isClimbable(pos.toLocation(world!!).block.type)
    }

    private fun isClimbable(material: Material): Boolean {
        val name = material.name.lowercase(Locale.getDefault())
        return material == Material.LADDER || name.contains("vine")
    }

    private fun deltaDistance(loc1: BlockPos, loc2: BlockPos): Vector {
        val deltaX = abs((loc1.x - loc2.x).toDouble())
        val deltaY = abs((loc1.y - loc2.y).toDouble())
        val deltaZ = abs((loc1.z - loc2.z).toDouble())
        return Vector(deltaX, deltaY, deltaZ)
    }

    public fun euclideanDistance(loc1: BlockPos, loc2: BlockPos): Double {
        val delta: Vector = deltaDistance(loc1, loc2)
        val distance2d = sqrt(delta.x * delta.x + delta.z * delta.z)
        return sqrt(distance2d * distance2d + delta.y * delta.y)
    }
}