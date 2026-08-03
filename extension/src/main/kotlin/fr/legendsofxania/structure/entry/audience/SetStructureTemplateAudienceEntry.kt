package fr.legendsofxania.structure.entry.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.AudienceDisplay
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var
import fr.legendsofxania.structure.entry.StructureTemplateSetterEntry
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import org.bukkit.block.structure.StructureRotation

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
    override val template: Var<Ref<StructureTemplateEntry>> = ConstVar(emptyRef()),
    override val location: Var<Position> = ConstVar(Position.ORIGIN),
    override val rotation: Var<StructureRotation> = ConstVar(StructureRotation.NONE),
    override val ignoreAir: Var<Boolean> = ConstVar(false),
    override val entities: Var<Boolean> = ConstVar(false),
) : AudienceEntry, StructureTemplateSetterEntry {
    override suspend fun display(): AudienceDisplay = SetStructureTemplateDisplay(this)
}
