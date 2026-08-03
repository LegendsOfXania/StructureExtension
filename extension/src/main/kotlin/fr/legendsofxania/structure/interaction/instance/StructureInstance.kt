package fr.legendsofxania.structure.interaction.instance

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.utils.UntickedAsync
import com.typewritermc.core.utils.launch
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.plugin
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import fr.legendsofxania.structure.manager.InstanceManager
import fr.legendsofxania.structure.util.structure.packBlockPos
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import kotlinx.coroutines.Dispatchers
import org.bukkit.Location
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class StructureInstance(
    template: Ref<StructureTemplateEntry>,
    position: Position,
    rotation: StructureRotation,
    ignoreAir: Boolean,
    spawnEntities: Boolean,
) {
    private val loader = StructureLoader(template, position, rotation, ignoreAir, spawnEntities)
    private val tracker = StructureChunkTracker()
    private val viewers: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    fun addViewer(player: Player) {
        Dispatchers.UntickedAsync.launch {
            val structure = loader.load(player.world) ?: return@launch
            viewers.add(player.uniqueId)
            InstanceManager.track(player.uniqueId, this@StructureInstance)

            plugin.server.scheduler.runTask(plugin) { _ ->
                if (tracker.awaitChunks(player, structure.chunks)) {
                    StructureRenderer.show(player, structure)
                }
            }
        }
    }

    fun removeViewer(player: Player) {
        val uuid = player.uniqueId
        tracker.forget(uuid)

        val structure = loader.cached
        if (viewers.remove(uuid) && structure != null) {
            StructureRenderer.hide(player, structure)
        }
        InstanceManager.untrack(uuid, this)
    }

    fun dispose() {
        viewers.forEach { uuid -> InstanceManager.untrack(uuid, this) }
        loader.cached?.entities?.forEach { it.remove() }
        loader.clear()
        tracker.clear()
        viewers.clear()
    }

    internal fun occupies(uuid: UUID, x: Int, y: Int, z: Int): Boolean {
        val blocks = loader.cached?.blocks ?: return false
        return uuid in viewers && packBlockPos(x, y, z) in blocks
    }


    internal fun onPlayerDigging(uuid: UUID, packet: WrapperPlayClientPlayerDigging) {
        if (uuid !in viewers) return
        val pos = packet.blockPosition
        val expected = loader.cached?.blocks?.get(packBlockPos(pos.x, pos.y, pos.z)) ?: return
        val player = plugin.server.getPlayer(uuid) ?: return

        plugin.server.scheduler.runTask(plugin) { _ ->
            player.sendBlockChange(
                Location(player.world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble()),
                expected,
            )
        }
    }

    internal fun onBlockChangeSend(uuid: UUID, packet: WrapperPlayServerBlockChange) {
        if (uuid !in viewers) return
        val pos = packet.blockPosition
        val expected = loader.cached?.blocks?.get(packBlockPos(pos.x, pos.y, pos.z)) ?: return
        packet.setBlockID(SpigotConversionUtil.fromBukkitBlockData(expected).globalId)
    }

    internal fun onChunkLoad(player: Player, key: Long) {
        if (!tracker.onChunkLoad(player.uniqueId, key)) return
        val structure = loader.cached ?: return
        StructureRenderer.show(player, structure)
    }

    internal fun onChunkUnload(uuid: UUID, key: Long) {
        val structure = loader.cached ?: return
        tracker.onChunkUnload(uuid, key, structure.chunks)
    }
}
