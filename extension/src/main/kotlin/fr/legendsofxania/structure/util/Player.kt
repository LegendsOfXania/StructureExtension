package fr.legendsofxania.structure.util

import fr.legendsofxania.structure.util.structure.toBlockLocation
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player

/**
 * Sends multiple fake block changes to the player.
 */
fun Player.sendMultiBlockChange(
    changes: Map<Long, BlockData>,
    world: World,
) {
    if (changes.isEmpty()) {
        return
    }

    val converted = HashMap<Location, BlockData>(changes.size)

    for ((packedPos, blockData) in changes) {
        converted[packedPos.toBlockLocation(world)] = blockData
    }

    sendMultiBlockChange(converted)
}

/**
 * Restores the player's view to the real world state.
 */
fun Player.restoreBlockChanges(
    changes: Map<Long, BlockData>,
    world: World,
) {
    if (changes.isEmpty()) {
        return
    }

    val restored = HashMap<Location, BlockData>(changes.size)

    for (packedPos in changes.keys) {
        val location = packedPos.toBlockLocation(world)
        restored[location] = location.block.blockData
    }

    sendMultiBlockChange(restored)
}
