package fr.legendsofxania.structure.enum

import org.bukkit.event.block.Action

enum class InteractionType(vararg val actions: Action) {
    ALL(
        Action.RIGHT_CLICK_BLOCK,
        Action.RIGHT_CLICK_AIR,
        Action.LEFT_CLICK_BLOCK,
        Action.LEFT_CLICK_AIR,
        Action.PHYSICAL
    ),
    CLICK(Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR, Action.LEFT_CLICK_BLOCK, Action.LEFT_CLICK_AIR),
    RIGHT_CLICK(Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR),
    LEFT_CLICK(Action.LEFT_CLICK_BLOCK, Action.LEFT_CLICK_AIR),
    PHYSICAL(Action.PHYSICAL),
}