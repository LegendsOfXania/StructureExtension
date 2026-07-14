package fr.legendsofxania.structure.entry.audience

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.AudienceDisplay
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import fr.legendsofxania.structure.interaction.instance.StructureInstance
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Player

class SetStructureTemplateDisplay(
    private val template: Ref<StructureTemplateEntry>,
    private val position: Position,
    private val rotation: StructureRotation,
    private val ignoreAir: Boolean,
    private val entities: Boolean,
) : AudienceDisplay() {
    private var instance: StructureInstance? = null

    override fun initialize() {
        instance = StructureInstance(
            template,
            position,
            rotation,
            ignoreAir,
            entities
        )
    }

    override fun onPlayerAdd(player: Player) {
        instance?.addViewer(player)
    }

    override fun onPlayerRemove(player: Player) {
        instance?.removeViewer(player)
    }

    override fun dispose() {
        instance?.dispose()
        instance = null
    }
}
