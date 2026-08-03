package fr.legendsofxania.structure.manager

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.DiggingAction
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange
import com.typewritermc.engine.paper.plugin
import fr.legendsofxania.structure.interaction.instance.StructureInstance
import fr.legendsofxania.structure.util.chunkKey
import io.papermc.paper.event.packet.PlayerChunkLoadEvent
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap


object InstanceManager {
    private val instancesByPlayer = ConcurrentHashMap<UUID, MutableSet<StructureInstance>>()

    @Volatile
    private var registered = false

    private val packetListener = object : PacketListenerAbstract() {
        override fun onPacketReceive(event: PacketReceiveEvent) {
            if (event.packetType != PacketType.Play.Client.PLAYER_DIGGING) return
            val instances = instancesByPlayer[event.user.uuid] ?: return

            val packet = WrapperPlayClientPlayerDigging(event)
            if (packet.action != DiggingAction.START_DIGGING && packet.action != DiggingAction.FINISHED_DIGGING) return

            instances.forEach { it.onPlayerDigging(event.user.uuid, packet) }
        }

        override fun onPacketSend(event: PacketSendEvent) {
            if (event.packetType != PacketType.Play.Server.BLOCK_CHANGE) return
            val instances = instancesByPlayer[event.user.uuid] ?: return

            val packet = WrapperPlayServerBlockChange(event)
            instances.forEach { it.onBlockChangeSend(event.user.uuid, packet) }
        }
    }

    private val bukkitListener = object : Listener {
        @EventHandler(ignoreCancelled = true)
        fun onBlockPlace(event: BlockPlaceEvent) {
            val instances = instancesByPlayer[event.player.uniqueId] ?: return
            val b = event.block
            if (instances.any { it.occupies(event.player.uniqueId, b.x, b.y, b.z) }) {
                event.isCancelled = true
            }
        }

        @EventHandler(ignoreCancelled = true)
        fun onBlockBreak(event: BlockBreakEvent) {
            val instances = instancesByPlayer[event.player.uniqueId] ?: return
            val b = event.block
            if (instances.any { it.occupies(event.player.uniqueId, b.x, b.y, b.z) }) {
                event.isCancelled = true
            }
        }

        @EventHandler(ignoreCancelled = true)
        fun onChunkLoad(event: PlayerChunkLoadEvent) {
            val instances = instancesByPlayer[event.player.uniqueId] ?: return
            val key = chunkKey(event.chunk.x, event.chunk.z)
            instances.forEach { it.onChunkLoad(event.player, key) }
        }

        @EventHandler(ignoreCancelled = true)
        fun onChunkUnload(event: PlayerChunkUnloadEvent) {
            val instances = instancesByPlayer[event.player.uniqueId] ?: return
            val key = chunkKey(event.chunk.x, event.chunk.z)
            instances.forEach { it.onChunkUnload(event.player.uniqueId, key) }
        }

        @EventHandler
        fun onQuit(event: PlayerQuitEvent) {
            instancesByPlayer.remove(event.player.uniqueId)
        }
    }

    @Synchronized
    private fun ensureRegistered() {
        if (registered) return
        PacketEvents.getAPI().eventManager.registerListener(packetListener)
        plugin.server.pluginManager.registerEvents(bukkitListener, plugin)
        registered = true
    }

    fun track(uuid: UUID, instance: StructureInstance) {
        ensureRegistered()
        instancesByPlayer.getOrPut(uuid) { ConcurrentHashMap.newKeySet() }.add(instance)
    }

    fun untrack(uuid: UUID, instance: StructureInstance) {
        val set = instancesByPlayer[uuid] ?: return
        set.remove(instance)
        if (set.isEmpty()) instancesByPlayer.remove(uuid, set)
    }
}
