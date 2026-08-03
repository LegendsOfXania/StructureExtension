package fr.legendsofxania.structure.util.structure

import fr.legendsofxania.structure.util.chunkKey
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.structure.Structure
import org.bukkit.block.structure.StructureRotation

data class StructureData(
    val blocks: Map<Long, BlockData>,
    val chunks: Set<Long>,
)

/**
 * Builds the block changes required to display a structure at the given origin.
 */
fun structureBlockChanges(
    structure: Structure,
    rotation: StructureRotation,
    ignoreAir: Boolean,
    origin: Location,
): StructureData {
    val palette = structure.palettes.firstOrNull()
        ?: return StructureData(emptyMap(), emptySet())

    val size = structure.size

    val originX = origin.blockX
    val originY = origin.blockY
    val originZ = origin.blockZ

    val blocks = HashMap<Long, BlockData>(palette.blocks.size)
    val chunks = HashSet<Long>()

    for (block in palette.blocks) {
        if (ignoreAir && block.type == Material.AIR) {
            continue
        }

        val rotated = rotateBlockPos(
            x = block.x,
            z = block.z,
            sizeX = size.blockX,
            sizeZ = size.blockZ,
            rotation = rotation,
        )

        val worldX = originX + rotated.x
        val worldY = originY + block.y
        val worldZ = originZ + rotated.z

        val blockData = block.blockData.clone().apply {
            rotate(rotation)
        }

        blocks[packBlockPos(worldX, worldY, worldZ)] = blockData
        chunks += chunkKey(worldX shr 4, worldZ shr 4)
    }

    return StructureData(
        blocks = blocks,
        chunks = chunks,
    )
}
