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
import fr.legendsofxania.structure.interaction.instance.StructureInstance
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
    private var instance: StructureInstance? = null

    override fun initialize() {
        instance = StructureInstance(
            template,
            position,
            rotation,
            ignoreAir,
            entities
        )
    }

    override fun onPlayerAdd(player: Player) {
        instance?.addViewer(player)
    }

    override fun onPlayerRemove(player: Player) {
        instance?.removeViewer(player)
    }

    override fun dispose() {
        instance?.dispose()
        instance = null
    }
}
