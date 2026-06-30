package fr.legendsofxania.structure.entry.audience

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.event.PacketListenerAbstract
import com.github.retrooper.packetevents.event.PacketReceiveEvent
import com.github.retrooper.packetevents.protocol.packettype.PacketType
import com.github.retrooper.packetevents.protocol.player.DiggingAction
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.utils.UntickedAsync
import com.typewritermc.core.utils.launch
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.AudienceDisplay
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.plugin
import com.typewritermc.engine.paper.utils.server
import com.typewritermc.engine.paper.utils.toBukkitLocation
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import fr.legendsofxania.structure.manager.TemplateManager
import fr.legendsofxania.structure.util.restoreStructureBlocks
import fr.legendsofxania.structure.util.sendStructureBlocks
import kotlinx.coroutines.Dispatchers
import org.bukkit.Location
import org.bukkit.block.data.BlockData
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class SetStructureTemplateDisplay(
    private val template: Var<Ref<StructureTemplateEntry>>,
    private val position: Var<Position>,
    private val rotation: Var<StructureRotation>,
    private val ignoreAir: Boolean
) : AudienceDisplay() {
    private var listener: PacketListenerAbstract? = null
    private val displayedBlocks = ConcurrentHashMap<UUID, Map<Location, BlockData>>()

    override fun initialize() {
        listener = object : PacketListenerAbstract() {
            override fun onPacketReceive(event: PacketReceiveEvent) {
                if (event.packetType != PacketType.Play.Client.PLAYER_DIGGING) return
                val packet = WrapperPlayClientPlayerDigging(event)
                if (packet.action != DiggingAction.START_DIGGING && packet.action != DiggingAction.FINISHED_DIGGING) return

                val uuid = event.user.uuid
                val player = server.getPlayer(uuid) ?: return
                val blocks = displayedBlocks[uuid] ?: return

                val pos = packet.blockPosition
                val loc = Location(
                    player.world,
                    pos.x.toDouble(),
                    pos.y.toDouble(),
                    pos.z.toDouble()
                )

                val expected = blocks[loc] ?: return

                server.scheduler.runTask(plugin) { _ ->
                    player.sendBlockChange(loc, expected)
                }

            }
        }
        PacketEvents.getAPI().eventManager.registerListener(listener!!)
    }

    override fun onPlayerAdd(player: Player) {
        Dispatchers.UntickedAsync.launch {
            val structure = template.get(player).entry?.let { TemplateManager.loadTemplate(it) } ?: return@launch
            val origin = position.get(player).toBukkitLocation(player.world)
            displayedBlocks[player.uniqueId] = player.sendStructureBlocks(structure, origin, ignoreAir)
        }
    }

    override fun onPlayerRemove(player: Player) {
        displayedBlocks.remove(player.uniqueId)?.let { player.restoreStructureBlocks(it) }
    }

    override fun dispose() {
        listener?.let {
            PacketEvents.getAPI().eventManager.unregisterListener(it)
            listener = null
        }
    }
}
