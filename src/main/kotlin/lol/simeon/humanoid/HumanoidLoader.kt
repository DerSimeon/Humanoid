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

package lol.simeon.humanoid

import dev.triumphteam.cmd.bukkit.BukkitCommandManager
import dev.triumphteam.cmd.core.extention.ExtensionBuilder
import dev.triumphteam.cmds.useCoroutines
import lol.simeon.humanoid.commands.TestCommand
import lol.simeon.humanoid.listeners.BukkitNPCInteractEvent
import lol.simeon.humanoid.listeners.NPCDamageHandler
import lol.simeon.humanoid.npc.NPCManager
import org.bukkit.command.CommandSender

class HumanoidLoader(private val instance: Humanoid) {

    val npcManager = NPCManager()
    private val bukkitCommandManager: BukkitCommandManager<CommandSender> = BukkitCommandManager.create(instance) {builder ->
        builder.extensions(ExtensionBuilder<CommandSender, CommandSender>::useCoroutines)
    }

    fun initLoader() {
        bukkitCommandManager.registerCommand(TestCommand(instance))
        NPCDamageHandler(instance)
        BukkitNPCInteractEvent(instance)
    }

    fun shutdownLoader() {
        this.npcManager.shutdown()
    }
}