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

package lol.simeon.humanoid.npc

import lol.simeon.humanoid.Humanoid
import lol.simeon.humanoid.console.HumanoidLogger
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.CompletableFuture
import kotlin.math.atan2

class HumanoidPathPlayer(private val instance: Humanoid, val npc: HumanoidNPC, val path: List<Location>) {
    private var currentFrame: Location? = null
    private var currentFrameIndex = 0
    private var playing: Boolean = false

    private var bukkitTask: BukkitTask? = null

    private val future: CompletableFuture<Unit> = CompletableFuture()

    fun play(): CompletableFuture<Unit> {
        currentFrame = path[0]
        playing = true
        bukkitTask = Bukkit.getScheduler().runTaskTimer(instance, Runnable {
            if (playing) {
                sendPacket()
                if (!next()) {
                    stop()
                    future.complete(null)
                    bukkitTask?.cancel()
                }
            }
        }, 0, 5)
        return future
    }

    private fun stop() {
        playing = false
    }

    fun next(): Boolean {
        if (currentFrameIndex >= path.size) {
            return false
        }
        currentFrame = path[currentFrameIndex]
        currentFrameIndex++
        HumanoidLogger.warn("Frame $currentFrameIndex of ${path.size} with location $currentFrame")
        return true
    }

    private fun sendPacket() {
        val frame = currentFrame ?: return

        val yaw = (atan2(frame.x - npc.getLocation().x, frame.z - npc.getLocation().z) * 180.0 / Math.PI).toFloat()
            .toInt()
            .toByte()
        val pitch = (frame.pitch * 256.0f / 360.0f).toInt().toByte()

        npc.setRotation(yaw.toFloat(), pitch.toFloat())
        npc.setLocation(
            frame.x,
            frame.y,
            frame.z,
        )
    }

}