package fr.legendsofxania.structure.entry.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.utils.UntickedAsync
import com.typewritermc.core.utils.launch
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.*
import com.typewritermc.engine.paper.interaction.interactionContext
import com.typewritermc.engine.paper.utils.Sync
import com.typewritermc.engine.paper.utils.toBukkitLocation
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import fr.legendsofxania.structure.manager.TemplateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import java.util.Random

@Entry("set_structure", "Place a StructureTemplate at a specified location.", Colors.RED, "fluent:apps-48-filled")
class SetStructureActionEntry(
    override val id: String = "",
    override val name: String = "",
    override val criteria: List<Criteria> = emptyList(),
    override val modifiers: List<Modifier> = emptyList(),
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @Help("The structure template to paste.")
    val template: Var<Ref<StructureTemplateEntry>> = ConstVar(emptyRef()),
    @Help("The location where to paste the structure.")
    val location: Var<Position> = ConstVar(Position.ORIGIN),
    @Help("The rotation to apply to the structure when pasting it.")
    val rotation: Var<StructureRotation> = ConstVar(StructureRotation.NONE),
    @Help("Whether to ignore the entities present in the template when pasting the structure.")
    val ignoreEntities: Boolean = false
) : ActionEntry {
    override fun ActionTrigger.execute() {
        Dispatchers.UntickedAsync.launch {
            val player = player
            val context = player.interactionContext
            val structure = template.get(player, context).entry?.let { TemplateManager.loadTemplateAsStructure(it) } ?: return@launch

            withContext(Dispatchers.Sync) {
                structure.place(
                    location.get(player, context).toBukkitLocation(),
                    ignoreEntities,
                    rotation.get(player, context),
                    Mirror.NONE,
                    0,
                    1f,
                    Random()
                )
            }
        }
    }
}