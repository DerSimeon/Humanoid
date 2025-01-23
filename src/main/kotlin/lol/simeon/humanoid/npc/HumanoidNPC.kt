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

import com.mojang.authlib.GameProfile
import com.mojang.datafixers.util.Pair
import io.papermc.paper.adventure.PaperAdventure
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lol.simeon.humanoid.Humanoid
import lol.simeon.humanoid.api.NPCInteractAction
import lol.simeon.humanoid.api.NPCParameters
import lol.simeon.humanoid.api.events.NPCInteractEvent
import lol.simeon.humanoid.api.events.NPCPreSpawnEvent
import lol.simeon.humanoid.console.HumanoidLogger
import lol.simeon.humanoid.npc.network.EmptyConnection
import lol.simeon.humanoid.npc.network.EmptyPacketListener
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.Optionull
import net.minecraft.network.chat.RemoteChatSession
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.game.*
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.PositionMoveRotation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Scoreboard
import net.minecraft.world.scores.Team
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import java.util.*
import kotlin.time.TestTimeSource

class HumanoidNPC(val npcParameters: NPCParameters) : ServerPlayer(
    (Bukkit.getServer() as CraftServer).server,
    (npcParameters.location.world as CraftWorld).handle,
    GameProfile(npcParameters.npcAccount.uuid, ""),
    HumanoidClientInformation.DEFAULT_CLIENT_INFORMATION
) {
    private val miniMessage by lazy { MiniMessage.miniMessage() }
    private val visibleFor = mutableSetOf<UUID>()

    private var sittingVehicle: Display.TextDisplay? = null
    private var isTeamCreated: Boolean = false
    private var lastPlayerInteraction: Long = 0

    private var location: Location = npcParameters.location

    init {
        initNetwork()
    }

    fun spawnFor(player: Player) {
        val serverPlayer = (player as CraftPlayer).handle

        if (serverPlayer.level().world.name != npcParameters.location.world.name) {
            return
        }

        val npcPreSpawnEvent = NPCPreSpawnEvent(this, player).apply { callEvent() }

        if (npcPreSpawnEvent.isCancelled) {
            return
        }

        val connection = serverPlayer.connection ?: return

        sendPlayerInfoUpdate(serverPlayer)
        connection.send(constructClientBoundAddEntityPacket())

        setPos(npcParameters.location.x, npcParameters.location.y, npcParameters.location.z)
        level().addFreshEntity(this)

        visibleFor.add(player.uniqueId)

        updateNPC(player)
    }

    fun despawn(player: Player) {
        val serverPlayer = (player as CraftPlayer).handle

        if (serverPlayer.level().world.name != npcParameters.location.world.name) {
            return
        }

        val connection = serverPlayer.connection ?: return

        connection.send(ClientboundPlayerInfoRemovePacket(listOf(this.uuid)))
        connection.send(ClientboundRemoveEntitiesPacket(this.id))

        if (sittingVehicle != null) {
            connection.send(ClientboundRemoveEntitiesPacket(sittingVehicle!!.id))
        }

        visibleFor.remove(player.uniqueId)
    }

    private fun updateNPC(player: Player) {
        if (visibleFor.isEmpty() || !visibleFor.contains(player.uniqueId)) {
            return
        }

        val serverPlayer = (player as CraftPlayer).handle

        val team = PlayerTeam(Scoreboard(), "hnpc-" + this.uuid.toString())
        team.players.clear()
        team.players.add(this.gameProfile.name)
        team.color = PaperAdventure.asVanilla(npcParameters.glowColor)!!
        if (!npcParameters.canCollide) {
            team.collisionRule = Team.CollisionRule.NEVER
        }
        this.customName = null
        this.isCustomNameVisible = false

        if (npcParameters.displayName.contentEquals("<empty>", true)) {
            team.nameTagVisibility = Team.Visibility.NEVER
        } else {
            team.nameTagVisibility = Team.Visibility.ALWAYS
            val vanillaComponent = PaperAdventure.asVanilla(miniMessage.deserialize(npcParameters.displayName))
            team.playerPrefix = vanillaComponent
            this.listName = vanillaComponent

            sendPlayerInfoUpdate(serverPlayer, false)
        }

        if (!isTeamCreated) {
            serverPlayer.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true))
            isTeamCreated = true
        }

        this.setGlowingTag(npcParameters.shouldGlow)

        if (npcParameters.attributes.equipment.isNotEmpty()) {
            val list = mutableListOf<Pair<EquipmentSlot, ItemStack>>()
            npcParameters.attributes.equipment.forEach { (slot, item) ->
                list.add(Pair(EquipmentSlot.byName(slot.name.lowercase()), CraftItemStack.asNMSCopy(item)))
            }

            serverPlayer.connection.send(ClientboundSetEquipmentPacket(this.id, list))
        }

        this.getEntityData()
            .set(DATA_PLAYER_MODE_CUSTOMISATION, ((0x01 or 0x02 or 0x04 or 0x08 or 0x10 or 0x20 or 0x40).toByte()))

        refreshEntityData(this)
        move(player, true)
    }

    fun move(player: Player, swingArm: Boolean) {
        val serverPlayer = (player as CraftPlayer).handle

        setPosRaw(this.location.x, this.location.y, this.location.z)
        setRot(this.location.yaw, this.location.pitch)
        setYHeadRot(this.location.yaw)
        xRot = this.location.pitch
        yRot = this.location.yaw

        val teleportEntityPacket = ClientboundTeleportEntityPacket(
            this.id,
            PositionMoveRotation(
                Vec3(this.location.x, this.location.y, this.location.z),
                Vec3.ZERO,
                this.location.yaw,
                this.location.pitch
            ),
            emptySet(),
            false
        )
        serverPlayer.connection.send(teleportEntityPacket)

        val rotMultiplier = 256.0 / 360.0
        serverPlayer.connection.send(
            ClientboundRotateHeadPacket(
                this,
                (this.location.yaw * rotMultiplier).toInt().toByte()
            )
        )

        if (swingArm) {
            serverPlayer.connection.send(ClientboundAnimatePacket(this, 0))
        }
    }


    private fun constructClientBoundAddEntityPacket(): ClientboundAddEntityPacket {
        return ClientboundAddEntityPacket(
            this.id,
            this.getUUID(),
            this.location.x,
            this.location.y,
            this.location.z,
            this.location.pitch,
            this.location.yaw,
            this.type,
            0,
            Vec3.ZERO,
            this.location.yaw.toDouble()
        )
    }

    private fun getEntry(player: ServerPlayer): ClientboundPlayerInfoUpdatePacket.Entry {
        val profile = player.gameProfile

        return ClientboundPlayerInfoUpdatePacket.Entry(
            player.uuid,
            profile,
            true,
            69,
            player.gameMode.gameModeForPlayer,
            player.tabListDisplayName,
            true,
            -1,
            Optionull.map(player.chatSession, RemoteChatSession::asData)
        )
    }

    private fun initNetwork() {
        val emptyConnection = EmptyConnection(PacketFlow.CLIENTBOUND)
        connection = EmptyPacketListener(
            server,
            emptyConnection,
            this,
            CommonListenerCookie(this.gameProfile, 0, clientInformation(), false)
        )

        setClientLoaded(true)
        getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, 0xFF.toByte())
    }


    private fun sendPlayerInfoUpdate(player: ServerPlayer, shouldAdd: Boolean = true) {
        val actions = EnumSet.noneOf(ClientboundPlayerInfoUpdatePacket.Action::class.java)
        if (shouldAdd) {
            actions.add(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER)
        }
        actions.add(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME)
        if (npcParameters.showInTab) {
            actions.add(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED)
        }

        player.connection.send(ClientboundPlayerInfoUpdatePacket(actions, getEntry(this)))
    }

    fun handleInteract(player: Player) = handleInteract(player, NPCInteractAction.PLUGIN)

    fun handleInteract(player: Player, npcInteractAction: NPCInteractAction) {
        if (npcParameters.interactionCooldown > 0) {
            val cooldownMillis = npcParameters.interactionCooldown * 1000
            val lastInteractionMillis = lastPlayerInteraction
            val cooldownLeft = cooldownMillis - (System.currentTimeMillis() - lastInteractionMillis)

            if (cooldownLeft > 0) {
                HumanoidLogger.info("Cooldown left: $cooldownLeft")
                //TODO: Send message to player
                return
            }

            lastPlayerInteraction = System.currentTimeMillis()
        }

        val interactEvent = NPCInteractEvent(this, player, npcInteractAction).apply { callEvent() }

        if (interactEvent.isCancelled) {
            HumanoidLogger.info("Event cancelled")
            return
        }
        HumanoidLogger.info("Interact Event")
    }

    fun getLocation(): Location = location

    fun setLocation(x: Double, y: Double, z: Double) {
        setPos(x, y, z)
        location = location.clone().set(x, y, z)
        sendLocationUpdate()
    }

    fun setRotation(yaw: Float, pitch: Float) {
        setRot(yaw, pitch)
        location = location.clone().setRotation(yaw, pitch)
        sendLocationUpdate()
    }

    private fun sendLocationUpdate() {
        val packet = ClientboundMoveEntityPacket.PosRot(
            id,
            ((location.x * 32).toInt() - (getLocation().x * 32).toInt()).toShort(),
            ((location.y * 32).toInt() - (getLocation().y * 32).toInt()).toShort(),
            ((location.z * 32).toInt() - (getLocation().z * 32).toInt()).toShort(),
            ((location.yaw * 256.0f / 360.0f).toInt().toByte()),
            ((location.pitch * 256.0f / 360.0f).toInt().toByte()),
            true
        )
        val onlinePlayer = Bukkit.getOnlinePlayers().firstOrNull() ?: return
        val craftPlayer = onlinePlayer as CraftPlayer
        craftPlayer.handle.connection.send(packet)
    }


}