package fr.legendsofxania.structure.interaction.content

import com.typewritermc.core.entries.Query
import com.typewritermc.core.utils.failure
import com.typewritermc.core.utils.ok
import com.typewritermc.engine.paper.content.ContentContext
import com.typewritermc.engine.paper.content.ContentMode
import com.typewritermc.engine.paper.content.components.bossBar
import com.typewritermc.engine.paper.content.components.exit
import com.typewritermc.engine.paper.content.entryId
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import net.kyori.adventure.bossbar.BossBar
import org.bukkit.Location
import org.bukkit.entity.Player

class StructureSelection {
    var corner1: Location? = null
    var corner2: Location? = null

    fun corners(): Pair<Location, Location>? {
        val corner1 = corner1 ?: return null
        val corner2 = corner2 ?: return null
        return Pair(corner1, corner2)
    }
}

class StructureTemplateContentMode(
    context: ContentContext,
    player: Player
) : ContentMode(context, player) {

    private val visualizer = StructureSelectionVisualizer(player)

    override suspend fun setup(): Result<Unit> {
        bossBar {
            title = "Structure Template Mode"
            color = BossBar.Color.BLUE
            progress = 1.0f
        }

        val entryId = context.entryId
            ?: return failure("Entry ID not found in context.")

        val entry = Query.findById<StructureTemplateEntry>(entryId)
            ?: return failure("StructureTemplateEntry with ID: $entryId, not found")

        val selectionTool = StructureSelectionTool(entry, StructureSelection(), visualizer)
        +selectionTool

        exit()
        return ok(Unit)
    }

    override suspend fun dispose() {
        super.dispose()
        visualizer.clear()
    }
}
