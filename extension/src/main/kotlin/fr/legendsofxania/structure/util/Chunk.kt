package fr.legendsofxania.structure.util

import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.utils.server
import io.papermc.paper.event.packet.PlayerChunkLoadEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

fun chunkKey(x: Int, z: Int): Long = (x.toLong() shl 32) or (z.toLong() and 0xFFFFFFFFL)

object ChunkTracker : Listener {
    private const val JOIN_GRACE_MS = 3000L

    private val sent = ConcurrentHashMap<UUID, MutableSet<Long>>()
    private val joinedAt = ConcurrentHashMap<UUID, Long>()

    init {
        server.pluginManager.registerEvents(this, plugin)
    }

    fun hasChunk(player: Player, x: Int, z: Int): Boolean {
        if (sent[player.uniqueId]?.contains(chunkKey(x, z)) == true) return true
        val since = joinedAt[player.uniqueId]
        val gracePassed = since == null || System.currentTimeMillis() - since > JOIN_GRACE_MS
        return gracePassed && player.world.isChunkLoaded(x, z)
    }

    @EventHandler
    fun onPLayerJoin(event: PlayerJoinEvent) {
        joinedAt[event.player.uniqueId] = System.currentTimeMillis()
    }

    @EventHandler
    fun onChunkLoad(event: PlayerChunkLoadEvent) {
        sent.getOrPut(event.player.uniqueId) { ConcurrentHashMap.newKeySet() }
            .add(chunkKey(event.chunk.x, event.chunk.z))
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        sent.remove(event.player.uniqueId)
        joinedAt.remove(event.player.uniqueId)
    }
}
