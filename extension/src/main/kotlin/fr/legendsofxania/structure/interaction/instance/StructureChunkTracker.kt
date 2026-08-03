package fr.legendsofxania.structure.interaction.instance

import fr.legendsofxania.structure.util.ChunkTracker
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class StructureChunkTracker {
    private val pendingChunks = ConcurrentHashMap<UUID, MutableSet<Long>>()

    fun awaitChunks(player: Player, chunks: Set<Long>): Boolean {
        val missing = chunks
            .filterNot { ChunkTracker.hasChunk(player, (it shr 32).toInt(), it.toInt()) }
            .toMutableSet()

        if (missing.isEmpty()) return true
        pendingChunks[player.uniqueId] = ConcurrentHashMap.newKeySet<Long>().apply { addAll(missing) }
        return false
    }

    fun onChunkLoad(uuid: UUID, key: Long): Boolean {
        val remaining = pendingChunks[uuid] ?: return false
        if (!remaining.remove(key) || remaining.isNotEmpty()) return false
        pendingChunks.remove(uuid)
        return true
    }

    fun onChunkUnload(uuid: UUID, key: Long, structureChunks: Set<Long>) {
        if (key !in structureChunks) return
        pendingChunks.getOrPut(uuid) { ConcurrentHashMap.newKeySet() }.add(key)
    }

    fun forget(uuid: UUID) {
        pendingChunks.remove(uuid)
    }

    fun clear() {
        pendingChunks.clear()
    }
}
