package fr.legendsofxania.structure.enum

import org.bukkit.entity.Player

enum class ShiftType {
    ANY,
    SHIFT,
    NO_SHIFT;

    fun isApplicable(player: Player): Boolean {
        return when (this) {
            ANY -> true
            SHIFT -> player.isSneaking
            NO_SHIFT -> !player.isSneaking
        }
    }
}