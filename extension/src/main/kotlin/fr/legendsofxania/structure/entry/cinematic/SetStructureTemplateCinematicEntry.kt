package fr.legendsofxania.structure.entry.cinematic

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Segments
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.entries.*
import fr.legendsofxania.structure.entry.StructureConfiguration
import fr.legendsofxania.structure.entry.StructureTemplateSetterEntry
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
    val template: Var<Ref<StructureTemplateEntry>> = ConstVar(emptyRef()),
    val location: Var<Position> = ConstVar(Position.ORIGIN),
    val rotation: Var<StructureRotation> = ConstVar(StructureRotation.NONE),
    val ignoreAir: Var<Boolean> = ConstVar(false),
    val entities: Var<Boolean> = ConstVar(false),
    @Segments(Colors.RED, "fluent:apps-48-filled")
    val segments: List<SetStructureTemplateCinematicSegment> = emptyList(),
) : CinematicEntry, StructureTemplateSetterEntry {
    override fun create(player: Player): CinematicAction = SetStructureTemplateCinematicAction(player, this)

    override fun configuration(player: Player): StructureConfiguration =
        StructureConfiguration(
            template.get(player),
            location.get(player),
            rotation.get(player),
            ignoreAir.get(player),
            entities.get(player),
        )
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
        instance = StructureInstance(entry.configuration(player))
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
