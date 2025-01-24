package lol.simeon.humanoid.pathfinding.multinode

public class PathFindingSettings {

    public val useChunking: Boolean = true

    public val useDiagonal: Boolean = true
    public val canParkour: Boolean = true
    public val canClimb: Boolean = true

    public val maxNodesToTest: Int = 1000
    public val chunkSize: Int = 25

    public val maxFall: Int = 3
    public val maxParkour: Int = 2

    // costs
    public val costForward: Double = 1.0
    public val costFall: Double = 0.7
    public val costStairs: Double = 0.8
    public val costDiagonal: Double = 1.0
    public val costClimb: Double = 1.4
    public val costJump: Double = 1.1
    public val costParkour: Double = 0.7
}