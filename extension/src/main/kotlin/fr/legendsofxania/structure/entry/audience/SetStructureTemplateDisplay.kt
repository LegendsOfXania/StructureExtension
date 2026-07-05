package fr.legendsofxania.structure.entry.audience

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
import com.typewritermc.engine.paper.entry.entries.AudienceDisplay
import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.utils.server
import com.typewritermc.engine.paper.utils.toBukkitLocation
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import fr.legendsofxania.structure.manager.TemplateManager
import fr.legendsofxania.structure.util.PlayerChunkTracker
import fr.legendsofxania.structure.util.buildStructureEntities
import fr.legendsofxania.structure.util.chunkKey
import fr.legendsofxania.structure.util.packBlockPos
import fr.legendsofxania.structure.util.restoreBlockChanges
import fr.legendsofxania.structure.util.sendMultiBlockChange
import fr.legendsofxania.structure.util.structureBlockChanges
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import io.papermc.paper.event.packet.PlayerChunkLoadEvent
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
import kotlinx.coroutines.Dispatchers
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SetStructureTemplateDisplay(
    private val template: Ref<StructureTemplateEntry>,
    private val position: Position,
    private val rotation: StructureRotation,
    private val ignoreAir: Boolean,
    private val entities: Boolean,
) : AudienceDisplay() {
    private var packetListener: PacketListenerAbstract? = null
    private var bukkitListener: Listener? = null

    private data class LoadedState(
        val origin: Location,
        val blocks: Map<Long, BlockData>,
        val chunks: Set<Long>,
        val entities: List<WrapperEntity>,
    )

    private val loadMutex = Mutex()

    @Volatile
    private var state: LoadedState? = null
    private val viewers: MutableSet<UUID> = ConcurrentHashMap.newKeySet()
    private val pendingChunks = ConcurrentHashMap<UUID, MutableSet<Long>>()

    override fun initialize() {
        packetListener = object : PacketListenerAbstract() {
            override fun onPacketReceive(event: PacketReceiveEvent) {
                if (event.packetType != PacketType.Play.Client.PLAYER_DIGGING) return
                val uuid = event.user.uuid
                if (uuid !in viewers) return
                val blocks = state?.blocks ?: return

                val packet = WrapperPlayClientPlayerDigging(event)
                if (packet.action != DiggingAction.START_DIGGING && packet.action != DiggingAction.FINISHED_DIGGING) return

                val pos = packet.blockPosition
                val expected = blocks[packBlockPos(pos.x, pos.y, pos.z)] ?: return
                val player = server.getPlayer(uuid) ?: return

                server.scheduler.runTask(plugin) { _ ->
                    player.sendBlockChange(
                        Location(player.world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()),
                        expected
                    )
                }
            }

            override fun onPacketSend(event: PacketSendEvent) {
                if (event.packetType != PacketType.Play.Server.BLOCK_CHANGE) return
                val uuid = event.user.uuid
                if (uuid !in viewers) return
                val blocks = state?.blocks ?: return

                val packet = WrapperPlayServerBlockChange(event)
                val pos = packet.blockPosition
                val expected = blocks[packBlockPos(pos.x, pos.y, pos.z)] ?: return
                packet.setBlockID(SpigotConversionUtil.fromBukkitBlockData(expected).globalId)
            }
        }
        PacketEvents.getAPI().eventManager.registerListener(packetListener!!)

        bukkitListener = object : Listener {
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

            @EventHandler
            fun onChunkLoad(event: PlayerChunkLoadEvent) {
                val remaining = pendingChunks[event.player.uniqueId] ?: return
                if (remaining.remove(chunkKey(event.chunk.x, event.chunk.z)) && remaining.isEmpty()) {
                    pendingChunks.remove(event.player.uniqueId)
                    flush(event.player)
                }
            }
        }
        server.pluginManager.registerEvents(bukkitListener!!, plugin)
    }

    private fun flush(player: Player) {
        val loaded = state ?: return
        if (player.uniqueId !in viewers) return
        player.sendMultiBlockChange(loaded.blocks, loaded.origin.world)
        loaded.entities.forEach { it.addViewer(player.uniqueId) }
    }

    private suspend fun ensureLoaded(player: Player): LoadedState? {
        state?.let { return it }
        return loadMutex.withLock {
            state?.let { return@withLock it }
            val structure = template.entry?.let { TemplateManager.loadTemplate(it) } ?: return@withLock null
            val origin = position.toBukkitLocation(player.world)
            val data = structureBlockChanges(structure, rotation, ignoreAir, origin)
            val ents = if (entities) buildStructureEntities(structure, origin) else emptyList()
            LoadedState(origin, data.blocks, data.chunks, ents).also { state = it }
        }
    }

    override fun onPlayerAdd(player: Player) {
        Dispatchers.UntickedAsync.launch {
            val loaded = ensureLoaded(player) ?: return@launch
            viewers.add(player.uniqueId)
            server.scheduler.runTask(plugin) { _ ->
                val missing = loaded.chunks
                    .filterNot { PlayerChunkTracker.hasChunk(player, (it shr 32).toInt(), it.toInt()) }
                    .toMutableSet()
                if (missing.isEmpty()) flush(player) else pendingChunks[player.uniqueId] = missing
            }
        }
    }

    override fun onPlayerRemove(player: Player) {
        val uuid = player.uniqueId
        val loaded = state
        pendingChunks.remove(uuid)
        if (viewers.remove(uuid) && loaded != null) {
            player.restoreBlockChanges(loaded.blocks, loaded.origin.world)
        }
        loaded?.entities?.forEach { it.removeViewer(uuid) }
    }

    override fun dispose() {
        packetListener?.let {
            PacketEvents.getAPI().eventManager.unregisterListener(it)
            packetListener = null
        }
        bukkitListener?.let {
            HandlerList.unregisterAll(it)
            bukkitListener = null
        }
        state?.entities?.forEach { it.remove() }
        state = null
        viewers.clear()
        pendingChunks.clear()
    }
}
