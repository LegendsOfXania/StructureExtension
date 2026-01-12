package fr.legendsofxania.structure.manager

import com.typewritermc.engine.paper.entry.entries.binaryData
import com.typewritermc.engine.paper.entry.entries.hasData
import com.typewritermc.engine.paper.utils.server
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import org.bukkit.Location
import org.bukkit.structure.Structure
import java.io.ByteArrayOutputStream

object TemplateManager {
    suspend fun saveTemplate(
        corner1: Location,
        corner2: Location,
        entry: StructureTemplateEntry
    ): Result<Unit> = runCatching {
        val world = corner1.world
        val (minCorner, maxCorner) = calculateCorners(corner1, corner2, world)

        val structure = server.structureManager.createStructure().apply {
            fill(minCorner, maxCorner, entry.saveEntities)
        }

        val bytes = ByteArrayOutputStream().use { out ->
            server.structureManager.saveStructure(out, structure)
            out.toByteArray()
        }

        entry.binaryData(bytes)
    }

    suspend fun loadTemplate(entry: StructureTemplateEntry): Structure? {
        if (!entry.hasData()) return null

        return entry.binaryData()
            ?.inputStream()
            ?.use { server.structureManager.loadStructure(it) }
    }

    private fun calculateCorners(corner1: Location, corner2: Location, world: org.bukkit.World) =
        Location(
            world,
            minOf(corner1.blockX, corner2.blockX).toDouble(),
            minOf(corner1.blockY, corner2.blockY).toDouble(),
            minOf(corner1.blockZ, corner2.blockZ).toDouble()
        ) to Location(
            world,
            maxOf(corner1.blockX, corner2.blockX).toDouble() + 1,
            maxOf(corner1.blockY, corner2.blockY).toDouble() + 1,
            maxOf(corner1.blockZ, corner2.blockZ).toDouble() + 1
        )
}