package fr.legendsofxania.structure.entry.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.engine.paper.utils.server
import fr.legendsofxania.structure.entry.StructureConfiguration
import fr.legendsofxania.structure.entry.StructureTemplateSetterEntry
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import fr.legendsofxania.structure.interaction.instance.StructureInstance
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Entry(
    "advanced_set_structure_audience",
    "Paste a template for an audience, reacting to per variable changes.",
    Colors.GREEN,
    "fluent:apps-48-filled"
)
class AdvancedSetStructureTemplateAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    val template: Var<Ref<StructureTemplateEntry>> = ConstVar(emptyRef()),
    val location: Var<Position> = ConstVar(Position.ORIGIN),
    val rotation: Var<StructureRotation> = ConstVar(StructureRotation.NONE),
    val ignoreAir: Var<Boolean> = ConstVar(false),
    val entities: Var<Boolean> = ConstVar(false),
    @Help("The interval in ticks between two checks for variable updates.")
    @Default("10")
    val updateInterval: Int = 10,
) : AudienceEntry, StructureTemplateSetterEntry {
    override suspend fun display(): AudienceDisplay = AdvancedSetStructureTemplateDisplay(this)

    override fun configuration(player: Player): StructureConfiguration =
        StructureConfiguration(
            template.get(player),
            location.get(player),
            rotation.get(player),
            ignoreAir.get(player),
            entities.get(player),
        )
}

class AdvancedSetStructureTemplateDisplay(
    private val entry: AdvancedSetStructureTemplateAudienceEntry,
) : AudienceDisplay(), TickableDisplay {
    private val structures = ConcurrentHashMap<UUID, StructureInstance>()

    private var tickCounter = 0

    override fun tick() {
        tickCounter++

        for ((uuid, instance) in structures) {
            if ((tickCounter + uuid.hashCode()) % entry.updateInterval != 0) continue

            val player = server.getPlayer(uuid) ?: continue

            val configuration = entry.configuration(player)
            if (configuration == instance.configuration) continue

            instance.removeViewer(player)

            val newInstance = StructureInstance(configuration)
            structures[uuid] = newInstance
            newInstance.addViewer(player)
        }
    }

    override fun onPlayerAdd(player: Player) {
        val instance = StructureInstance(entry.configuration(player))
        structures[player.uniqueId] = instance
        instance.addViewer(player)
    }

    override fun onPlayerRemove(player: Player) {
        structures.remove(player.uniqueId)?.removeViewer(player)
    }

    override fun dispose() {
        structures.values.forEach { it.dispose() }
        structures.clear()
    }
}
