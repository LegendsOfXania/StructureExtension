package fr.legendsofxania.structure.interaction.instance

import fr.legendsofxania.structure.data.StructureData
import org.bukkit.Location
import java.util.UUID

class StructureInstance(
    val playerId: UUID,
    val data: StructureData,
    val location: Location
) {
    fun show() {
        // Implementation to display the structure instance at the specified location
    }
    fun destroy() {
        // Implementation to completely remove the structure instance
    }
}