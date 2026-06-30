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
import org.bukkit.entity.Player

class StructureTemplateContentMode(
    context: ContentContext,
    player: Player
) : ContentMode(context, player) {

    override suspend fun setup(): Result<Unit> {
        bossBar {
            title = "<aqua>Structure Template Mode</aqua>"
            color = BossBar.Color.BLUE
            progress = 1.0f
        }

        val entryId = context.entryId
            ?: return failure("Entry ID not found in context.")

        val entry = Query.findById<StructureTemplateEntry>(entryId)
            ?: return failure("RoomTemplateEntry not found for ID: $entryId")

        val selectionTool = StructureSelectionTool(entry)
        +selectionTool

        exit()
        return ok(Unit)
    }
}
