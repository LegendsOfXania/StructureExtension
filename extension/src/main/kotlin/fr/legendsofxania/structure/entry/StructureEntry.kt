package fr.legendsofxania.structure.entry

import com.typewritermc.core.entries.Entry
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.Var
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import org.bukkit.block.structure.StructureRotation

@Tags("structure_template_setter")
interface StructureTemplateSetterEntry : Entry {
    override val id: String
    override val name: String

    @Help("The structure template to paste.")
    val template: Var<Ref<StructureTemplateEntry>>

    @Help("The location where to paste the structure.")
    val location: Var<Position>

    @Help("The rotation to apply to the structure when pasting it.")
    val rotation: Var<StructureRotation>

    @Help("Ignore air blocks when pasting the structure?")
    val ignoreAir: Var<Boolean>

    @Help("Spawn entities present in the template?")
    val entities: Var<Boolean>
}
