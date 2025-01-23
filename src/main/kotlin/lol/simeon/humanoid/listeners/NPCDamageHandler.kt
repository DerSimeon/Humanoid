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

package lol.simeon.humanoid.listeners

import lol.simeon.humanoid.Humanoid
import lol.simeon.humanoid.api.NPCInteractAction
import lol.simeon.humanoid.api.events.NPCDamageEvent
import lol.simeon.humanoid.npc.HumanoidNPC
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent

public class NPCDamageHandler(private val instance: Humanoid) : HumanoidListener(instance) {

    @EventHandler
    public fun onDamage(entityDamageEvent: EntityDamageEvent) {
        if (entityDamageEvent.entity is Player) {
            val serverPlayer = (entityDamageEvent.entity as CraftPlayer).handle
            if (serverPlayer is HumanoidNPC) {
                val npc = instance.loader.npcManager.getNPCByEntityID(serverPlayer.id) ?: return
                if (!npc.npcParameters.canTakeDamage) {
                    entityDamageEvent.isCancelled = true
                }
                if (entityDamageEvent.damageSource.causingEntity is Player) {
                    npc.handleInteract(entityDamageEvent.damageSource.causingEntity as Player, NPCInteractAction.LEFT)
                }

                if (!entityDamageEvent.isCancelled) {
                    val event = NPCDamageEvent(
                        entityDamageEvent.damageSource,
                        entityDamageEvent.cause,
                        npc
                    ).apply { callEvent() }
                    entityDamageEvent.isCancelled = event.isCancelled
                }

            }
        }
    }
}