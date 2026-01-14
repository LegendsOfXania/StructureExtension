package fr.legendsofxania.structure.enum

import com.typewritermc.core.interaction.context
import com.typewritermc.engine.paper.utils.item.Item
import org.bukkit.entity.Player

enum class HoldingHand(val main: Boolean, val off: Boolean) {
    BOTH(true, true),
    MAIN(true, false),
    OFF(false, true),
}

fun hasItemInHand(player: Player, hand: HoldingHand, item: Item): Boolean {
    if (hand.main && item.isSameAs(player, player.inventory.itemInMainHand, context())) return true
    if (hand.off && item.isSameAs(player, player.inventory.itemInOffHand, context())) return true
    return false
}