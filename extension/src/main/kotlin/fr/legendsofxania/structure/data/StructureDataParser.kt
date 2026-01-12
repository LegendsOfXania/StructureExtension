package fr.legendsofxania.structure.data

import com.typewritermc.engine.paper.entry.entries.binaryData
import com.typewritermc.engine.paper.entry.entries.hasData
import com.typewritermc.engine.paper.utils.server
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import org.bukkit.block.data.BlockData
import org.bukkit.entity.EntityType
import org.bukkit.util.Vector
import java.util.UUID

object StructureDataParser {
    suspend fun loadTemplate(entry: StructureTemplateEntry): StructureData? {
        if (!entry.hasData()) return null

        val compound = entry.binaryData()
            ?.inputStream()
            ?.use { NbtIo.readCompressed(it, NbtAccounter.unlimitedHeap()) }
            ?: return null

        return parseStructureData(compound)
    }

    private fun parseStructureData(compound: CompoundTag): StructureData? {
        val size = parseSize(compound) ?: return null
        val blockPalette = parsePalette(compound) ?: return null
        val blocks = parseBlocks(compound, blockPalette)
        val entities = parseEntities(compound)

        return StructureData(UUID.randomUUID(), blocks, entities, size)
    }

    private fun parseSize(compound: CompoundTag): Vector? {
        val sizeList = compound.get("size") as? ListTag ?: return null
        return Vector(
            sizeList.getInt(0).orElse(0),
            sizeList.getInt(1).orElse(0),
            sizeList.getInt(2).orElse(0)
        )
    }

    private fun parsePalette(compound: CompoundTag): List<BlockData>? {
        val paletteList = compound.get("palette") as? ListTag ?: return null
        return List(paletteList.size) { i ->
            parseBlockData(paletteList.getCompound(i).orElseThrow())
        }
    }

    private fun parseBlocks(
        compound: CompoundTag,
        blockPalette: List<BlockData>
    ): List<StructureData.StructureBlockData> {
        val blocksList = compound.get("blocks") as? ListTag ?: return emptyList()

        return (0 until blocksList.size).mapNotNull { i ->
            val blockCompound = blocksList.getCompound(i).orElse(null) ?: return@mapNotNull null

            val position = parsePosition(blockCompound.get("pos") as? ListTag) ?: return@mapNotNull null
            val stateIndex = blockCompound.getInt("state").orElse(0)
            val blockData = blockPalette.getOrNull(stateIndex) ?: return@mapNotNull null
            val tileEntityNBT = blockCompound.getCompound("nbt").orElse(null)

            StructureData.StructureBlockData(position, blockData, tileEntityNBT)
        }
    }

    private fun parseEntities(compound: CompoundTag): List<StructureData.StructureEntityData> {
        val entitiesList = compound.get("entities") as? ListTag ?: return emptyList()

        return (0 until entitiesList.size).mapNotNull { i ->
            val entityCompound = entitiesList.getCompound(i).orElse(null) ?: return@mapNotNull null

            val position = parsePositionDouble(entityCompound.get("pos") as? ListTag) ?: return@mapNotNull null
            val entityNBT = entityCompound.getCompound("nbt").orElse(null) ?: return@mapNotNull null
            val entityId = entityNBT.getString("id").orElse("minecraft:pig")
            val entityType = parseEntityType(entityId)
            val rotation = parseRotation(entityNBT.get("Rotation") as? ListTag)

            StructureData.StructureEntityData(position, entityType, entityNBT, rotation)
        }
    }

    private fun parsePosition(posList: ListTag?): Vector? {
        if (posList == null) return null
        return Vector(
            posList.getInt(0).orElse(0),
            posList.getInt(1).orElse(0),
            posList.getInt(2).orElse(0)
        )
    }

    private fun parsePositionDouble(posList: ListTag?): Vector? {
        if (posList == null) return null
        return Vector(
            posList.getDouble(0).orElse(0.0),
            posList.getDouble(1).orElse(0.0),
            posList.getDouble(2).orElse(0.0)
        )
    }

    private fun parseRotation(rotationList: ListTag?): StructureData.StructureEntityData.EntityRotation {
        return if (rotationList != null && rotationList.size >= 2) {
            StructureData.StructureEntityData.EntityRotation(
                yaw = rotationList.getFloat(0).orElse(0f),
                pitch = rotationList.getFloat(1).orElse(0f)
            )
        } else {
            StructureData.StructureEntityData.EntityRotation(0f, 0f)
        }
    }

    private fun parseBlockData(compound: CompoundTag): BlockData {
        val name = compound.getString("Name").orElse("minecraft:air")
        val blockStateString = buildBlockStateString(name, compound)
        return server.createBlockData(blockStateString)
    }

    private fun buildBlockStateString(name: String, compound: CompoundTag): String {
        if (!compound.contains("Properties")) return name

        val properties = compound.getCompound("Properties").orElse(null) ?: return name

        val tagsField = CompoundTag::class.java.getDeclaredField("tags").apply {
            isAccessible = true
        }

        @Suppress("UNCHECKED_CAST")
        val tags = tagsField.get(properties) as Map<String, *>

        val propsString = tags.keys.joinToString(",") { key ->
            "$key=${properties.getString(key).orElse("")}"
        }

        return "$name[$propsString]"
    }

    private fun parseEntityType(minecraftId: String): EntityType {
        val cleanId = minecraftId.removePrefix("minecraft:")

        return EntityType.entries.firstOrNull {
            it.name.equals(cleanId, ignoreCase = true) ||
                    it.key.key.equals(cleanId, ignoreCase = true)
        } ?: EntityType.UNKNOWN
    }
}