package fr.legendsofxania.structure.util

import com.github.retrooper.packetevents.protocol.world.Location as PacketLocation
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.block.structure.StructureRotation
import org.bukkit.entity.Player
import org.bukkit.structure.Structure

fun packBlockPos(x: Int, y: Int, z: Int): Long =
    ((x.toLong() and 0x3FFFFFFL) shl 38) or
            ((y.toLong() and 0xFFFL) shl 26) or
            (z.toLong() and 0x3FFFFFFL)

private fun locationOf(world: World, packed: Long): Location {
    val x = (packed shr 38).toInt()
    val y = ((packed shl 26) shr 52).toInt()
    val z = ((packed shl 38) shr 38).toInt()
    return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
}

private fun rotate(x: Int, z: Int, sizeX: Int, sizeZ: Int, rotation: StructureRotation): Pair<Int, Int> =
    when (rotation) {
        StructureRotation.NONE -> x to z
        StructureRotation.CLOCKWISE_90 -> (sizeZ - 1 - z) to x
        StructureRotation.CLOCKWISE_180 -> (sizeX - 1 - x) to (sizeZ - 1 - z)
        StructureRotation.COUNTERCLOCKWISE_90 -> z to (sizeX - 1 - x)
    }

class StructureData(val blocks: Map<Long, BlockData>, val chunks: Set<Long>)

fun structureBlockChanges(
    structure: Structure,
    rotation: StructureRotation,
    ignoreAir: Boolean,
    origin: Location,
): StructureData {
    val palette = structure.palettes.firstOrNull() ?: return StructureData(emptyMap(), emptySet())
    val size = structure.size
    val blocks = HashMap<Long, BlockData>(palette.blocks.size)
    val chunks = HashSet<Long>()

    for (bs in palette.blocks) {
        if (ignoreAir && bs.type == Material.AIR) continue
        val (rx, rz) = rotate(bs.x, bs.z, size.blockX, size.blockZ, rotation)
        val x = origin.blockX + rx
        val z = origin.blockZ + rz
        val data = bs.blockData.clone()
        data.rotate(rotation)
        blocks[packBlockPos(x, origin.blockY + bs.y, z)] = data
        chunks.add(chunkKey(x shr 4, z shr 4))
    }
    return StructureData(blocks, chunks)
}

fun Player.sendMultiBlockChange(changes: Map<Long, BlockData>, world: World) {
    if (changes.isEmpty()) return
    sendMultiBlockChange(changes.mapKeys { (key, _) -> locationOf(world, key) })
}

fun Player.restoreBlockChanges(changes: Map<Long, BlockData>, world: World) {
    if (changes.isEmpty()) return
    val byLocation = changes.keys.associate { key ->
        val loc = locationOf(world, key)
        loc to loc.block.blockData
    }
    sendMultiBlockChange(byLocation)
}

fun buildStructureEntities(structure: Structure, origin: Location): List<WrapperEntity> {
    return structure.entities.map { entity ->
        val loc = entity.location
        val wrapper = WrapperEntity(SpigotConversionUtil.fromBukkitEntityType(entity.type))
        wrapper.spawn(
            PacketLocation(
                origin.blockX + loc.x,
                origin.blockY + loc.y,
                origin.blockZ + loc.z,
                loc.yaw,
                loc.pitch,
            )
        )
        wrapper
    }
}
