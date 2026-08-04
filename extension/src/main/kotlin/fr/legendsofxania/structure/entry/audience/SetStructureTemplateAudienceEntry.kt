package fr.legendsofxania.structure.entry.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.AudienceDisplay
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var
import fr.legendsofxania.structure.entry.StructureConfiguration
import fr.legendsofxania.structure.entry.StructureTemplateSetterEntry
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import fr.legendsofxania.structure.interaction.instance.StructureInstance
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Player

@Entry(
    "set_structure_audience",
    "Paste a template for an audience.",
    Colors.GREEN,
    "fluent:apps-48-filled"
)
/**
 * The `Set Structure` audience entry allows you to paste a structure template at a specified location for the audience.
 *
 * ## How could this be used?
 * This audience entry can be used in various scenarios, such as:
 * - Displaying structures to players during events or presentations.
 * - Creating dynamic environments that change based on audience interactions.
 */
class SetStructureTemplateAudienceEntry(
    override val id: String = "",
    override val name: String = "",
    val template: Ref<StructureTemplateEntry> = emptyRef(),
    val location: Position = Position.ORIGIN,
    val rotation: StructureRotation = StructureRotation.NONE,
    val ignoreAir: Boolean = false,
    val entities: Boolean = false,
) : AudienceEntry, StructureTemplateSetterEntry {
    override suspend fun display(): AudienceDisplay = SetStructureTemplateDisplay(this)

    override fun configuration(player: Player): StructureConfiguration =
        StructureConfiguration(
            template,
            location,
            rotation,
            ignoreAir,
            entities,
        )
}

class SetStructureTemplateDisplay(
    private val entry: SetStructureTemplateAudienceEntry,
) : AudienceDisplay() {
    private var instance: StructureInstance? = null

    override fun onPlayerAdd(player: Player) {
        val instance = instance ?: StructureInstance(entry.configuration(player)).also { instance = it }
        instance.addViewer(player)
    }

    override fun onPlayerRemove(player: Player) {
        instance?.removeViewer(player)
    }

    override fun dispose() {
        instance?.dispose()
        instance = null
    }
}
