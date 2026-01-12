package fr.legendsofxania.structure.data

import net.minecraft.nbt.CompoundTag
import org.bukkit.block.data.BlockData
import org.bukkit.entity.EntityType
import org.bukkit.util.Vector
import java.util.UUID

data class StructureData(
    val id: UUID,
    val blocks: List<StructureBlockData>,
    val entities: List<StructureEntityData>,
    val size: Vector
) {
    data class StructureBlockData(
        val position: Vector,
        val data: BlockData,
        val nbt: CompoundTag? = null
    )

    data class StructureEntityData(
        val position: Vector,
        val type: EntityType,
        val nbt: CompoundTag,
        val rotation: EntityRotation
    ) {
        data class EntityRotation(
            val yaw: Float,
            val pitch: Float
        )
    }
}
