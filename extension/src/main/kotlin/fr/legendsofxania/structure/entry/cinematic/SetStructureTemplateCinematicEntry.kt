package fr.legendsofxania.structure.entry.cinematic

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Segments
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.entries.CinematicAction
import com.typewritermc.engine.paper.entry.entries.CinematicEntry
import com.typewritermc.engine.paper.entry.entries.Segment
import com.typewritermc.engine.paper.entry.entries.canFinishAt
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import fr.legendsofxania.structure.interaction.instance.StructureInstance
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Player

@Entry(
    "set_structure_cinematic",
    "Paste a template in a cinematic.",
    Colors.RED,
    "fluent:apps-48-filled"
)
/**
 * The `Set Structure` cinematic entry allows you to paste a structure template at a specified location for the viewer of a cinematic.
 *
 * ## How could this be used?
 * This entry can be used to display structures to players during events or presentations,
 * or to create dynamic environments that change based on viewer interactions.
 */
class SetStructureTemplateCinematicEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    @Help("The structure template to paste.")
    val template: Ref<StructureTemplateEntry> = emptyRef(),
    @Help("The location where to paste the structure.")
    val location: Position = Position.ORIGIN,
    @Help("The rotation to apply to the structure when pasting it.")
    val rotation: StructureRotation = StructureRotation.NONE,
    @Help("Whether to ignore air blocks when pasting the structure.")
    val ignoreAir: Boolean = false,
    @Help("Spawn entities present in the template?")
    val entities: Boolean = false,
    @Segments(Colors.RED, "fluent:apps-48-filled")
    val segments: List<SetStructureTemplateCinematicSegment> = emptyList(),
) : CinematicEntry {
    override fun create(player: Player): CinematicAction = SetStructureTemplateCinematicAction(player, this)
}

data class SetStructureTemplateCinematicSegment(
    override val startFrame: Int = 0,
    override val endFrame: Int = 0,
) : Segment

class SetStructureTemplateCinematicAction(
    val player: Player,
    val entry: SetStructureTemplateCinematicEntry,
) : CinematicAction {
    private var instance: StructureInstance? = null

    override suspend fun setup() {
        instance = StructureInstance(
            entry.template,
            entry.location,
            entry.rotation,
            entry.ignoreAir,
            entry.entities
        )
    }

    override suspend fun tick(frame: Int) {
        entry.segments.forEach { segment ->
            when (frame) {
                segment.startFrame -> instance?.addViewer(player)
                segment.endFrame -> instance?.removeViewer(player)
            }
        }
    }

    override suspend fun teardown() {
        instance?.dispose()
        instance = null
    }

    override fun canFinish(frame: Int): Boolean = entry.segments canFinishAt frame
}
