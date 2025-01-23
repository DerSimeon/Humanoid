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

import net.minecraft.world.entity.Entity
import org.bukkit.Bukkit

class NPCManager {

    private val npcs = mutableListOf<HumanoidNPC>()

    fun storeNPC(npc: HumanoidNPC) {
        npcs.add(npc)
    }

    fun shutdown() {
        npcs.forEach {
            for (onlinePlayer in Bukkit.getOnlinePlayers()) {
                it.despawn(onlinePlayer)
            }
            it.remove(Entity.RemovalReason.DISCARDED)
        }
    }
    
    fun getNPCByEntityID(id: Int): HumanoidNPC? {
        return npcs.firstOrNull { it.id == id}
    }
    
    fun getNPCByName(name: String): HumanoidNPC? {
        return npcs.firstOrNull { it.npcParameters.npcAccount.name == name}
    }
}