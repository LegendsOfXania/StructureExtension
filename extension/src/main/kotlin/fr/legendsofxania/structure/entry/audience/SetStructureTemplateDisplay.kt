package fr.legendsofxania.structure.entry.audience

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.entry.entries.AudienceDisplay
import com.typewritermc.engine.paper.entry.entries.TickableDisplay
import com.typewritermc.engine.paper.entry.entries.Var
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Player

class SetStructureTemplateDisplay(
    private val template: Var<Ref<StructureTemplateEntry>>,
    private val location: Var<Position>,
    private val rotation: Var<StructureRotation>,
    private val ignoreEntities: Boolean,
    private val ignoreAir: Boolean
) : AudienceDisplay(), TickableDisplay {
    override fun onPlayerAdd(player: Player) {
        TODO("Not yet implemented")
    }

    override fun tick() {
        TODO("Not yet implemented")
    }

    override fun onPlayerRemove(player: Player) {
        TODO("Not yet implemented")
    }
}