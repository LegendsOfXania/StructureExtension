package fr.legendsofxania.structure.util

import io.github.retrooper.packetevents.util.SpigotConversionUtil
import com.github.retrooper.packetevents.protocol.world.Location as PacketLocation
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player
import org.bukkit.structure.Structure

fun Player.sendStructureBlocks(structure: Structure, origin: Location, ignoreAir: Boolean): Map<Location, BlockData> {
    val palette = structure.palettes.firstOrNull() ?: return emptyMap()
    val changes: Map<Location, BlockData> = palette.blocks
        .filter { !ignoreAir || it.type != Material.AIR }
        .associate { bs ->
            Location(
                origin.world,
                origin.blockX + bs.x.toDouble(),
                origin.blockY + bs.y.toDouble(),
                origin.blockZ + bs.z.toDouble()
            ) to bs.blockData
        }
    sendMultiBlockChange(changes)
    return changes
}

fun Player.restoreStructureBlocks(blocks: Map<Location, BlockData>) {
    sendMultiBlockChange(blocks.keys.associateWith { it.block.blockData })
}
