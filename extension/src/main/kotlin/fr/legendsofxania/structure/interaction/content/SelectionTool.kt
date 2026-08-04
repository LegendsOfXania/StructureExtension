package fr.legendsofxania.structure.interaction.content

import com.typewritermc.core.utils.UntickedAsync
import com.typewritermc.core.utils.launch
import com.typewritermc.engine.paper.content.ContentComponent
import com.typewritermc.engine.paper.content.components.IntractableItem
import com.typewritermc.engine.paper.content.components.ItemComponent
import com.typewritermc.engine.paper.content.components.ItemInteractionType
import com.typewritermc.engine.paper.content.components.onInteract
import com.typewritermc.engine.paper.utils.asMini
import com.typewritermc.engine.paper.utils.msg
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import fr.legendsofxania.structure.manager.TemplateManager
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import kotlinx.coroutines.Dispatchers
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class SelectionTool(
    private val entry: StructureTemplateEntry,
    private val selection: Selection,
    private val visualizer: SelectionVisualizer,
) : ContentComponent, ItemComponent {

    override fun item(player: Player): Pair<Int, IntractableItem> {
        val item = ItemStack(Material.BREEZE_ROD).apply {
            setData(DataComponentTypes.ITEM_NAME, "<aqua>Structure Template Selection</aqua>".asMini())
            setData(
                DataComponentTypes.LORE, ItemLore.lore().addLines(
                    """
                    <!i><gray><white>Left-click</white> to select the first corner.</gray>
                    <!i><gray><white>Right-click</white> to select the second corner.</gray>
                    <!i><gray><white>Shift + Left-click</white> to save the structure.</gray>
                """.trimIndent().lines().map { it.asMini() }
                ))
        } onInteract { event ->
            val location = event.clickedBlock?.location ?: player.location
            when (event.type) {
                ItemInteractionType.LEFT_CLICK -> {
                    selection.corner1 = location
                    visualizer.update(selection)
                    player.msg("First corner selected at <blue>${location.blockX}</blue>, <blue>${location.blockY}</blue>, <blue>${location.blockZ}</blue>.")
                }

                ItemInteractionType.RIGHT_CLICK -> {
                    selection.corner2 = location
                    visualizer.update(selection)
                    player.msg("Second corner selected at <blue>${location.blockX}</blue>, <blue>${location.blockY}</blue>, <blue>${location.blockZ}</blue>.")
                }

                ItemInteractionType.SHIFT_LEFT_CLICK -> save(player)
                else -> Unit
            }
        }
        return 4 to item
    }

    private fun save(player: Player) {
        val (c1, c2) = selection.corners() ?: run {
            player.msg("<red>You must select both corners before saving the room.</red>")
            return
        }
        Dispatchers.UntickedAsync.launch {
            TemplateManager.saveTemplate(c1, c2, entry)
                .onSuccess { player.msg("Template saved successfully.") }
                .onFailure { player.msg("Failed to save Template: ${it.message}") }
        }
    }
}
