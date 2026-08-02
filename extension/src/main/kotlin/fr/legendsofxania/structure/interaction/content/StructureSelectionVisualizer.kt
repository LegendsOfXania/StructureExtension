package fr.legendsofxania.structure.interaction.content

import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes
import com.github.retrooper.packetevents.util.Vector3f
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.tofaa.entitylib.meta.display.BlockDisplayMeta
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import kotlin.math.abs

class StructureSelectionVisualizer(
    private val player: Player
) {
    private val edgesBlockState = SpigotConversionUtil.fromBukkitBlockData(Material.RED_TERRACOTTA.createBlockData())

    private val edges = arrayOfNulls<WrapperEntity>(EDGE_PAIRS.size / 2)

    companion object {
        private const val THICKNESS = 0.08f
        private const val HALF_THICKNESS = THICKNESS / 2f

        private val EDGE_PAIRS = intArrayOf(
            0, 4, 2, 6, 1, 5, 3, 7,
            0, 2, 4, 6, 1, 3, 5, 7,
            0, 1, 4, 5, 2, 3, 6, 7,
        )
    }

    private fun axis(vertex: Int, shift: Int, min: Double, max: Double): Double =
        if ((vertex shr shift) and 1 == 1) max else min

    fun update(selection: StructureSelection) {
        val (c1, c2) = selection.corners() ?: run { clear(); return }
        val world = c1.world ?: return

        val minX = minOf(c1.blockX, c2.blockX).toDouble()
        val minY = minOf(c1.blockY, c2.blockY).toDouble()
        val minZ = minOf(c1.blockZ, c2.blockZ).toDouble()
        val maxX = maxOf(c1.blockX, c2.blockX) + 1.0
        val maxY = maxOf(c1.blockY, c2.blockY) + 1.0
        val maxZ = maxOf(c1.blockZ, c2.blockZ) + 1.0

        var edgeIndex = 0
        var i = 0
        while (i < EDGE_PAIRS.size) {
            val a = EDGE_PAIRS[i]
            val b = EDGE_PAIRS[i + 1]
            i += 2

            val ax = axis(a, 2, minX, maxX)
            val ay = axis(a, 1, minY, maxY)
            val az = axis(a, 0, minZ, maxZ)
            val bx = axis(b, 2, minX, maxX)
            val by = axis(b, 1, minY, maxY)
            val bz = axis(b, 0, minZ, maxZ)

            val ox = minOf(ax, bx)
            val oy = minOf(ay, by)
            val oz = minOf(az, bz)
            val length = maxOf(abs(bx - ax), abs(by - ay), abs(bz - az)).toFloat()

            val scale: Vector3f
            val translation: Vector3f
            when {
                ax != bx -> {
                    scale = Vector3f(length, THICKNESS, THICKNESS)
                    translation = Vector3f(0f, -HALF_THICKNESS, -HALF_THICKNESS)
                }

                ay != by -> {
                    scale = Vector3f(THICKNESS, length, THICKNESS)
                    translation = Vector3f(-HALF_THICKNESS, 0f, -HALF_THICKNESS)
                }

                else -> {
                    scale = Vector3f(THICKNESS, THICKNESS, length)
                    translation = Vector3f(-HALF_THICKNESS, -HALF_THICKNESS, 0f)
                }
            }

            setEdge(edgeIndex, Location(world, ox, oy, oz), scale, translation)
            edgeIndex++
        }
    }

    private fun setEdge(index: Int, origin: Location, scale: Vector3f, translation: Vector3f) {
        val packetLocation = SpigotConversionUtil.fromBukkitLocation(origin)
        var entity = edges[index]

        if (entity == null) {
            entity = WrapperEntity(EntityTypes.BLOCK_DISPLAY)
            val meta = entity.entityMeta as BlockDisplayMeta
            meta.blockState = edgesBlockState
            meta.brightnessOverride = (15 shl 4) or 15
            entity.spawn(packetLocation)
            entity.addViewer(player.uniqueId)
            edges[index] = entity
        } else {
            entity.teleport(packetLocation)
        }

        val meta = entity.entityMeta as BlockDisplayMeta
        meta.scale = scale
        meta.translation = translation
    }

    fun clear() {
        for (i in edges.indices) {
            edges[i]?.remove()
            edges[i] = null
        }
    }
}
