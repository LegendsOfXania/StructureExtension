package fr.legendsofxania.structure.entry.action

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.entries.emptyRef
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.core.utils.UntickedAsync
import com.typewritermc.core.utils.launch
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.Criteria
import com.typewritermc.engine.paper.entry.Modifier
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.ActionEntry
import com.typewritermc.engine.paper.entry.entries.ActionTrigger
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.interaction.interactionContext
import com.typewritermc.engine.paper.utils.Sync
import com.typewritermc.engine.paper.utils.toBukkitLocation
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import fr.legendsofxania.structure.manager.TemplateManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.block.structure.Mirror
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Player
import java.util.Random

@Entry(
    "set_structure",
    "Place a StructureTemplate at a specified location.",
    Colors.RED,
    "fluent:apps-48-filled"
)
@Tags("set_structure")
/**
 * The `Set Structure` action entry allows you to place a structure at a specified location.
 *
 * ## How could this be used?
 * This action can be used in various scenarios, such as:
 * - Creating dynamic environments by placing structures based on player actions.
 * - Generating buildings or landmarks in response to game events.
 */
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
            setStructure(player)
        }
    }

    private suspend fun setStructure(player: Player) {
        val templateEntry = template.get(player, player.interactionContext).entry ?: return
        val structure = TemplateManager.loadTemplateAsStructure(templateEntry) ?: return

        withContext(Dispatchers.Sync) {
            structure.place(
                location.get(player, player.interactionContext).toBukkitLocation(),
                ignoreEntities,
                rotation.get(player, player.interactionContext),
                Mirror.NONE,
                0,
                1f,
                Random()
            )
        }
    }
}
