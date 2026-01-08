package fr.legendsofxania.structure.entry.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var
import org.bukkit.block.structure.StructureRotation

@Entry(
    "set_template",
    "Place a StructureTemplate at a specified location.",
    Colors.RED,
    "fluent:apps-48-filled"
)
class SetTemplateActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("The StructureTemplate to place.")
    val template: Var<Ref<SetTemplateActionEntry>> = ConstVar(emptyRef()),
    @Help("The location where the StructureTemplate will be placed.")
    val location: Var<Position> = ConstVar(Position.ORIGIN),
    @Help("The rotation to apply to the StructureTemplate when placed.")
    val rotation: StructureRotation = StructureRotation.NONE
) : ActionEntry {
    override fun ActionTrigger.execute() {
        // TODO: Implement the logic to set the structure template at the specified location with the given rotation.
    }
}