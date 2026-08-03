package fr.legendsofxania.structure.interaction.instance

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.event.PacketSendEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.DiggingAction
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.utils.UntickedAsync
import com.typewritermc.core.utils.launch
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.utils.toBukkitLocation
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import fr.legendsofxania.structure.manager.TemplateManager
import fr.legendsofxania.structure.util.*
import fr.legendsofxania.structure.util.structure.buildStructureEntities
import fr.legendsofxania.structure.util.structure.packBlockPos
import fr.legendsofxania.structure.util.structure.structureBlockChanges
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.papermc.paper.event.packet.PlayerChunkLoadEvent
import io.papermc.paper.event.packet.PlayerChunkUnloadEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class StructureInstance(
    private val template: Ref<StructureTemplateEntry>,
    private val position: Position,
    private val rotation: StructureRotation,
    private val ignoreAir: Boolean,
    private val entities: Boolean,
) {
    private data class State(
        val origin: Location,
        val chunks: Set<Long>,
        val blocks: Map<Long, BlockData>,
        val entities: List<WrapperEntity>,
    )

    @Volatile
    private var state: State? = null

    private val mutex = Mutex()
    private val viewers: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
    private val pendingChunks = ConcurrentHashMap<UUID, MutableSet<Long>>()

    private val packetListener: PacketListenerAbstract = object : PacketListenerAbstract() {
        override fun onPacketReceive(event: PacketReceiveEvent) {
            if (event.packetType != PacketType.Play.Client.PLAYER_DIGGING) return

            val uuid = event.user.uuid
            if (!viewers.contains(uuid)) return

            val packet = WrapperPlayClientPlayerDigging(event)
            if (packet.action != DiggingAction.START_DIGGING && packet.action != DiggingAction.FINISHED_DIGGING) return

            val pos = packet.blockPosition
            val blocks = state?.blocks ?: return
            val expected = blocks[packBlockPos(pos.x, pos.y, pos.z)] ?: return
            val player = plugin.server.getPlayer(uuid) ?: return

            plugin.server.scheduler.runTask(plugin) { _ ->
                player.sendBlockChange(
                    Location(player.world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()),
                    expected
                )
            }

        }

        override fun onPacketSend(event: PacketSendEvent) {
            if (event.packetType != PacketType.Play.Server.BLOCK_CHANGE) return

            val uuid = event.user.uuid
            if (!viewers.contains(uuid)) return

            val packet = WrapperPlayServerBlockChange(event)
            val pos = packet.blockPosition
            val blocks = state?.blocks ?: return
            val expected = blocks[packBlockPos(pos.x, pos.y, pos.z)] ?: return

            packet.setBlockID(SpigotConversionUtil.fromBukkitBlockData(expected).globalId)
        }
    }

    private val bukkitListener: Listener = object : Listener {
        @EventHandler(ignoreCancelled = true)
        fun onBlockPlace(event: BlockPlaceEvent) {
            if (event.player.uniqueId !in viewers) return
            val b = event.block
            if (packBlockPos(b.x, b.y, b.z) in (state?.blocks ?: return)) event.isCancelled = true
        }

        @EventHandler(ignoreCancelled = true)
        fun onBlockBreak(event: BlockBreakEvent) {
            if (event.player.uniqueId !in viewers) return
            val b = event.block
            if (packBlockPos(b.x, b.y, b.z) in (state?.blocks ?: return)) event.isCancelled = true
        }

        @EventHandler(ignoreCancelled = true)
        fun onChunkLoad(event: PlayerChunkLoadEvent) {
            val remaining = pendingChunks[event.player.uniqueId] ?: return
            if (remaining.remove(chunkKey(event.chunk.x, event.chunk.z)) && remaining.isEmpty()) {
                pendingChunks.remove(event.player.uniqueId)
                flush(event.player)
            }
        }

        @EventHandler(ignoreCancelled = true)
        fun onChunkUnload(event: PlayerChunkUnloadEvent) {
            val uuid = event.player.uniqueId
            if (uuid !in viewers) return
            val key = chunkKey(event.chunk.x, event.chunk.z)
            val loaded = state ?: return
            if (key !in loaded.chunks) return
            pendingChunks.getOrPut(uuid) { ConcurrentHashMap.newKeySet() }.add(key)
        }
    }

    init {
        PacketEvents.getAPI().eventManager.registerListener(packetListener)
        plugin.server.pluginManager.registerEvents(bukkitListener, plugin)
    }

    private fun flush(player: Player) {
        val loaded = state ?: return
        if (player.uniqueId !in viewers) return
        player.sendMultiBlockChange(loaded.blocks, loaded.origin.world)
        loaded.entities.forEach { it.addViewer(player.uniqueId) }
    }

    private suspend fun ensureLoaded(player: Player): State? {
        state?.let { return it }
        return mutex.withLock {
            state?.let { return@withLock it }
            val structure = template.entry?.let { TemplateManager.loadTemplate(it) } ?: return@withLock null
            val origin = position.toBukkitLocation(player.world)
            val data = structureBlockChanges(structure, rotation, ignoreAir, origin)
            val entities = if (entities) buildStructureEntities(structure, origin, rotation) else emptyList()
            State(origin, data.chunks, data.blocks, entities).also { state = it }
        }
    }


    fun addViewer(player: Player) {
        Dispatchers.UntickedAsync.launch {
            val loaded = ensureLoaded(player) ?: return@launch
            viewers.add(player.uniqueId)
            plugin.server.scheduler.runTask(plugin) { _ ->
                val missing = loaded.chunks
                    .filterNot { ChunkTracker.hasChunk(player, (it shr 32).toInt(), it.toInt()) }
                    .toMutableSet()
                if (missing.isEmpty()) flush(player) else pendingChunks[player.uniqueId] = missing
            }
        }
    }

    fun removeViewer(player: Player) {
        val uuid = player.uniqueId
        val loaded = state
        pendingChunks.remove(uuid)
        if (viewers.remove(uuid) && loaded != null) {
            player.restoreBlockChanges(loaded.blocks, loaded.origin.world)
        }
        loaded?.entities?.forEach { it.removeViewer(uuid) }
    }

    fun dispose() {
        PacketEvents.getAPI().eventManager.unregisterListener(packetListener)
        HandlerList.unregisterAll(bukkitListener)
        state?.entities?.forEach { it.remove() }
        state = null
        viewers.clear()
        pendingChunks.clear()
    }
}
