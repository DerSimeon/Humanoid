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

package lol.simeon.humanoid.commands

import dev.triumphteam.cmd.core.annotations.Command
import kotlinx.coroutines.*
import kotlinx.coroutines.future.asCompletableFuture
import lol.simeon.humanoid.Humanoid
import lol.simeon.humanoid.api.NPCAccount
import lol.simeon.humanoid.api.NPCParameters
import lol.simeon.humanoid.console.HumanoidLogger
import lol.simeon.humanoid.npc.HumanoidNPC
import lol.simeon.humanoid.npc.HumanoidPathPlayer
import lol.simeon.humanoid.pathfinding.HumanoidPathFinder
import lol.simeon.humanoid.pathfinding.HumanoidPathFindingState
import lol.simeon.humanoid.runInSync
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.entity.Player
import java.lang.Runnable
import java.util.UUID
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext

@Command("humanoid")
class TestCommand(val instance: Humanoid) {

    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Command("spawn")
    fun spawnNPC(player: Player) {
        val npcAccount = NPCAccount("Notch", UUID.randomUUID())
        val npcParameters = NPCParameters.withDefault(npcAccount, player.location).copy(
            shouldGlow = true,
            glowColor = NamedTextColor.GOLD,
            showInTab = false,
            canCollide = false,
            canTakeDamage = false
        )
        val humanoid = HumanoidNPC(npcParameters)
        humanoid.spawnFor(player)
        instance.loader.npcManager.storeNPC(humanoid)
    }

    @Command("pathfind")
    suspend fun pathfindNPC(player: Player, name: String) {
        val npc = instance.loader.npcManager.getNPCByName(name) ?: return
        val startLocation = npc.getLocation()
        val endLocation = player.location

        val pathfinder = HumanoidPathFinder(player.world.name)
        HumanoidLogger.info("Finding path for NPC ${npc.npcParameters.npcAccount.name} to $endLocation")
        val result = pathfinder.findPathAsync(startLocation, endLocation)

        result.path.forEach {
            player.world.spawnParticle(Particle.HAPPY_VILLAGER, it.toLocation(player.world), 1)
        }
        
        val humanoidPathPlayer = HumanoidPathPlayer(instance, npc, result.path)

        runInSync(instance) {
            humanoidPathPlayer.play()
        }
    }
}