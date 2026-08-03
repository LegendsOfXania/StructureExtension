package fr.legendsofxania.structure.entry.audience

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.AudienceDisplay
import com.typewritermc.engine.paper.entry.entries.TickableDisplay
import com.typewritermc.engine.paper.utils.server
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import fr.legendsofxania.structure.interaction.instance.StructureInstance
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private const val STRUCTURE_UPDATE_INTERVAL = 10

private data class StructureConfiguration(
    val template: Ref<StructureTemplateEntry>,
    val location: Position,
    val rotation: StructureRotation,
    val ignoreAir: Boolean,
    val entities: Boolean,
)

private data class PlayerStructure(
    val instance: StructureInstance,
    val configuration: StructureConfiguration,
)

class SetStructureTemplateDisplay(
    private val entry: SetStructureTemplateAudienceEntry,
) : AudienceDisplay(), TickableDisplay {
    private val structures = ConcurrentHashMap<UUID, PlayerStructure>()

    private var tickCounter = 0

    override fun tick() {
        if (++tickCounter < STRUCTURE_UPDATE_INTERVAL) return
        tickCounter = 0

        for ((uuid, playerStructure) in structures) {
            val player = server.getPlayer(uuid) ?: continue

            val configuration = configuration(player)
            if (configuration == playerStructure.configuration) continue

            playerStructure.instance.removeViewer(player)

            val instance = StructureInstance(
                configuration.template,
                configuration.location,
                configuration.rotation,
                configuration.ignoreAir,
                configuration.entities,
            )

            structures[uuid] = PlayerStructure(instance, configuration)
            instance.addViewer(player)
        }
    }

    override fun onPlayerAdd(player: Player) {
        val configuration = configuration(player)

        val instance = StructureInstance(
            configuration.template,
            configuration.location,
            configuration.rotation,
            configuration.ignoreAir,
            configuration.entities,
        )

        structures[player.uniqueId] = PlayerStructure(instance, configuration)
        instance.addViewer(player)
    }

    override fun onPlayerRemove(player: Player) {
        structures.remove(player.uniqueId)?.instance?.removeViewer(player)
    }

    private fun configuration(player: Player): StructureConfiguration =
        StructureConfiguration(
            entry.template.get(player),
            entry.location.get(player),
            entry.rotation.get(player),
            entry.ignoreAir.get(player),
            entry.entities.get(player),
        )
}
