package fr.legendsofxania.structure.util.structure

import com.github.retrooper.packetevents.protocol.world.Location as PacketLocation
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.Location
import org.bukkit.block.structure.StructureRotation
import org.bukkit.structure.Structure

/**
 * Builds the fake entities contained in a structure.
 */
fun buildStructureEntities(
    structure: Structure,
    origin: Location,
    rotation: StructureRotation,
): List<WrapperEntity> {
    val size = structure.size

    val originX = origin.x
    val originY = origin.y
    val originZ = origin.z

    return structure.entities.map { entityInfo ->
        val entityLocation = entityInfo.location

        val rotated = rotateEntityPos(
            x = entityLocation.x,
            z = entityLocation.z,
            sizeX = size.blockX,
            sizeZ = size.blockZ,
            rotation = rotation,
        )

        WrapperEntity(
            SpigotConversionUtil.fromBukkitEntityType(entityInfo.type)
        ).apply {
            spawn(
                PacketLocation(
                    originX + rotated.x,
                    originY + entityLocation.y,
                    originZ + rotated.z,
                    rotateYaw(entityLocation.yaw, rotation),
                    entityLocation.pitch,
                )
            )
        }
    }
}
