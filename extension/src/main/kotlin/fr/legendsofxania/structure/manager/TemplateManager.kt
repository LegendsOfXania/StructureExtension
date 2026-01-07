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
        val structureManager = server.structureManager

        val minCorner = Location(
            world,
            minOf(corner1.blockX, corner2.blockX).toDouble(),
            minOf(corner1.blockY, corner2.blockY).toDouble(),
            minOf(corner1.blockZ, corner2.blockZ).toDouble()
        )

        val maxCorner = Location(
            world,
            maxOf(corner1.blockX, corner2.blockX).toDouble() + 1,
            maxOf(corner1.blockY, corner2.blockY).toDouble() + 1,
            maxOf(corner1.blockZ, corner2.blockZ).toDouble() + 1
        )

        val structure = structureManager.createStructure().apply {
            fill(minCorner, maxCorner, entry.saveEntities)
        }

        val bytes = ByteArrayOutputStream().use { out ->
            structureManager.saveStructure(out, structure)
            out.toByteArray()
        }

        entry.binaryData(bytes)
    }

    suspend fun loadTemplate(entry: StructureTemplateEntry): Structure? {
        return entry.binaryData()
            ?.inputStream()
            ?.use { server.structureManager.loadStructure(it) }
            .takeIf { entry.hasData() }
    }
}