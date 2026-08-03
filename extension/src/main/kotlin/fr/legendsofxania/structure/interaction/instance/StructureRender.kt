package fr.legendsofxania.structure.interaction.instance

import fr.legendsofxania.structure.util.restoreBlockChanges
import fr.legendsofxania.structure.util.sendMultiBlockChange
import org.bukkit.entity.Player

object StructureRenderer {
    fun show(player: Player, structure: LoadedStructure) {
        player.sendMultiBlockChange(structure.blocks, structure.origin.world)
        structure.entities.forEach { it.addViewer(player.uniqueId) }
    }

    fun hide(player: Player, structure: LoadedStructure) {
        player.restoreBlockChanges(structure.blocks, structure.origin.world)
        structure.entities.forEach { it.removeViewer(player.uniqueId) }
    }
}
