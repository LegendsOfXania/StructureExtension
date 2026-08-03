package fr.legendsofxania.structure.util.structure

import org.bukkit.Location
import org.bukkit.World

/**
 * Packs a block position into a single Long using the vanilla Minecraft format.
 */
fun packBlockPos(x: Int, y: Int, z: Int): Long =
    ((x.toLong() and 0x3FFFFFFL) shl 38) or
            ((y.toLong() and 0xFFFL) shl 26) or
            (z.toLong() and 0x3FFFFFFL)

/**
 * Extracts the X coordinate from a packed block position.
 */
fun unpackBlockX(packed: Long): Int =
    (packed shr 38).toInt()

/**
 * Extracts the Y coordinate from a packed block position.
 */
fun unpackBlockY(packed: Long): Int =
    ((packed shl 26) shr 52).toInt()

/**
 * Extracts the Z coordinate from a packed block position.
 */
fun unpackBlockZ(packed: Long): Int =
    ((packed shl 38) shr 38).toInt()

/**
 * Converts a packed block position to a Bukkit Location.
 */
fun Long.toBlockLocation(world: World): Location =
    Location(
        world,
        unpackBlockX(this).toDouble(),
        unpackBlockY(this).toDouble(),
        unpackBlockZ(this).toDouble()
    )
