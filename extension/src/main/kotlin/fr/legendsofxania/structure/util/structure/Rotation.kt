package fr.legendsofxania.structure.util.structure

import org.bukkit.block.structure.StructureRotation

data class BlockRotation(
    val x: Int,
    val z: Int,
)

data class EntityRotation(
    val x: Double,
    val z: Double,
)

/**
 * Rotates a block position inside a structure.
 */
fun rotateBlockPos(
    x: Int,
    z: Int,
    sizeX: Int,
    sizeZ: Int,
    rotation: StructureRotation,
): BlockRotation =
    when (rotation) {
        StructureRotation.NONE ->
            BlockRotation(x, z)

        StructureRotation.CLOCKWISE_90 ->
            BlockRotation(sizeZ - 1 - z, x)

        StructureRotation.CLOCKWISE_180 ->
            BlockRotation(sizeX - 1 - x, sizeZ - 1 - z)

        StructureRotation.COUNTERCLOCKWISE_90 ->
            BlockRotation(z, sizeX - 1 - x)
    }

/**
 * Rotates an entity position inside a structure.
 */
fun rotateEntityPos(
    x: Double,
    z: Double,
    sizeX: Int,
    sizeZ: Int,
    rotation: StructureRotation,
): EntityRotation =
    when (rotation) {
        StructureRotation.NONE ->
            EntityRotation(x, z)

        StructureRotation.CLOCKWISE_90 ->
            EntityRotation(sizeZ - z, x)

        StructureRotation.CLOCKWISE_180 ->
            EntityRotation(sizeX - x, sizeZ - z)

        StructureRotation.COUNTERCLOCKWISE_90 ->
            EntityRotation(z, sizeX - x)
    }

/**
 * Rotates an entity yaw according to the structure rotation.
 */
fun rotateYaw(
    yaw: Float,
    rotation: StructureRotation,
): Float {
    val offset = when (rotation) {
        StructureRotation.NONE -> 0f
        StructureRotation.CLOCKWISE_90 -> 90f
        StructureRotation.CLOCKWISE_180 -> 180f
        StructureRotation.COUNTERCLOCKWISE_90 -> 270f
    }

    return (yaw + offset).mod(360f)
}
