package fr.legendsofxania.structure.util

import com.github.retrooper.packetevents.protocol.particle.Particle
import com.github.retrooper.packetevents.protocol.particle.type.ParticleTypes
import com.github.retrooper.packetevents.util.Vector3f
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerParticle
import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.util.Vector3d
import com.typewritermc.engine.paper.utils.toVector
import org.bukkit.entity.Player
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector

class BoundingBoxViewer(
    private val player: Player,
    boundingBox: BoundingBox
) {
    private val corners: List<Vector>
    private val edges: List<Pair<Int, Int>>

    private val particle = Particle(ParticleTypes.OMINOUS_SPAWNING)

    init {
        val min = boundingBox.min.toVector()
        val max = boundingBox.max.toVector()

        corners = listOf(
            Vector(min.x, min.y, min.z),
            Vector(max.x, min.y, min.z),
            Vector(max.x, min.y, max.z),
            Vector(min.x, min.y, max.z),
            Vector(min.x, max.y, min.z),
            Vector(max.x, max.y, min.z),
            Vector(max.x, max.y, max.z),
            Vector(min.x, max.y, max.z)
        )

        edges = listOf(
            Pair(0, 1), Pair(1, 2), Pair(2, 3), Pair(3, 0),
            Pair(4, 5), Pair(5, 6), Pair(6, 7), Pair(7, 4),
            Pair(0, 4), Pair(1, 5), Pair(2, 6), Pair(3, 7)
        )
    }

    fun drawnBox() {
        edges.forEach { (start, end) ->
            drawnLine(corners[start], corners[end])
        }
    }

    private fun drawnLine(start: Vector, end: Vector) {
        val distance = start.distance(end)
        val steps = (distance / 0.5).toInt()

        val dx = (end.x - start.x) / steps
        val dy = (end.y - start.y) / steps
        val dz = (end.z - start.z) / steps

        val playerManager = PacketEvents.getAPI().playerManager

        for (i in 0..steps) {
            val x = start.x + dx * i
            val y = start.y + dy * i
            val z = start.z + dz * i

            val vector3d = Vector3d(x, y, z)
            val vector3f = Vector3f(0f, 0f, 0f)

            val packet = WrapperPlayServerParticle(
                particle,
                true,
                vector3d,
                vector3f,
                0f,
                1
            )

            playerManager.sendPacket(player, packet)
        }
    }
}