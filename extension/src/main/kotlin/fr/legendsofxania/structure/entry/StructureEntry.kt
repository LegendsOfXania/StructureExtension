package fr.legendsofxania.structure.entry

import com.typewritermc.core.entries.Entry
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.Default
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.Var
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Player

@Tags("structure_template_setter")
interface StructureTemplateSetterEntry : Entry {
    override val id: String
    override val name: String

    fun configuration(player: Player): StructureConfiguration
}

data class StructureConfiguration(
    val template: Ref<StructureTemplateEntry>,
    val location: Position,
    val rotation: StructureRotation,
    val ignoreAir: Boolean,
    val entities: Boolean,
)
