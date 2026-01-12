package fr.legendsofxania.structure.entry.audience

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.AudienceDisplay
import com.typewritermc.engine.paper.entry.entries.AudienceEntry
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var
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
    @Help("The structure template to paste.")
    val template: Var<Ref<StructureTemplateEntry>> = ConstVar(emptyRef()),
    @Help("The location where to paste the structure.")
    val location: Var<Position> = ConstVar(Position.ORIGIN),
    @Help("The rotation to apply to the structure when pasting it.")
    val rotation: Var<StructureRotation> = ConstVar(StructureRotation.NONE),
    @Help("Whether to ignore the entities present in the template when pasting the structure.")
    val ignoreEntities: Boolean = false,
    @Help("Whether to ignore air blocks when pasting the structure.")
    val ignoreAir: Boolean = false
) : AudienceEntry {
    override suspend fun display(): AudienceDisplay {
        return SetStructureTemplateDisplay(template, location, rotation, ignoreEntities, ignoreAir)
    }
}