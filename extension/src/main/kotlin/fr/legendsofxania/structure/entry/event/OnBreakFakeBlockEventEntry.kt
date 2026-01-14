package fr.legendsofxania.structure.entry.event

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.entries.Query
import com.typewritermc.core.entries.Ref
import com.typewritermc.core.extension.annotations.ContextKeys
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.EntryListener
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.KeyType
import com.typewritermc.core.extension.annotations.MaterialProperties
import com.typewritermc.core.extension.annotations.MaterialProperty
import com.typewritermc.core.interaction.EntryContextKey
import com.typewritermc.core.utils.point.Position
import com.typewritermc.core.utils.point.toBlockPosition
import com.typewritermc.engine.paper.entry.TriggerableEntry
import com.typewritermc.engine.paper.entry.entries.CancelableEventEntry
import com.typewritermc.engine.paper.entry.entries.ConstVar
import com.typewritermc.engine.paper.entry.entries.Var
import com.typewritermc.engine.paper.entry.entries.shouldCancel
import com.typewritermc.engine.paper.entry.startDialogueWithOrNextDialogue
import com.typewritermc.engine.paper.utils.item.Item
import com.typewritermc.engine.paper.utils.item.toItem
import com.typewritermc.engine.paper.utils.toPosition
import fr.legendsofxania.structure.enum.HoldingHand
import fr.legendsofxania.structure.enum.hasItemInHand
import fr.legendsofxania.structure.event.AsyncBreakFakeBlockEvent
import org.bukkit.Material
import java.util.Optional
import kotlin.reflect.KClass

@Entry(
    "on_break_fake_block_event",
    "When a player breaks a fake block.",
    Colors.YELLOW,
    "mingcute:pickax-line"
)
@ContextKeys(BlockBreakContextKeys::class)
class OnBreakFakeBlockEventEntry(
    override val id: String = "",
    override val name: String = "",
    override val triggers: List<Ref<TriggerableEntry>> = emptyList(),
    @MaterialProperties(MaterialProperty.BLOCK)
    val blocks: Optional<List<Material>> = Optional.empty(),
    val location: Optional<Var<Position>> = Optional.empty(),
    @Help("The item the player must be holding when the block is broken.")
    val itemInHand: Var<Item> = ConstVar(Item.Empty),
    @Help("The hand the player must be holding the item in")
    val hand: HoldingHand = HoldingHand.BOTH,
    override val cancel: Var<Boolean> = ConstVar(false)
) : CancelableEventEntry

enum class BlockBreakContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Material::class)
    TYPE(Material::class),

    @KeyType(Position::class)
    BLOCK_POSITION(Position::class),

    @KeyType(Position::class)
    CENTER_POSITION(Position::class),

    @KeyType(Item::class)
    MAIN_HAND_ITEM(Item::class),

    @KeyType(Item::class)
    OFF_HAND_ITEM(Item::class),
}

@EntryListener(OnBreakFakeBlockEventEntry::class, ignoreCancelled = true)
fun onFakeBlockBreak(event: AsyncBreakFakeBlockEvent, query: Query<OnBreakFakeBlockEventEntry>) {
    val player = event.player
    val position = event.block.location.toPosition()
    val entries = query.findWhere { entry ->
        // Check if the player clicked on the correct location
        if (!entry.location.map { it.get(player) == position }.orElse(true)) return@findWhere false

        // Check if the player is holding the correct item
        if (!hasItemInHand(player, entry.hand, entry.itemInHand.get(player))) return@findWhere false

        // Check if block type is correct
        entry.blocks.map { it.contains(event.block.type) }.orElse(true)
    }.toList()

    entries.startDialogueWithOrNextDialogue(player) {
        BlockBreakContextKeys.TYPE += event.block.type
        BlockBreakContextKeys.BLOCK_POSITION += position.toBlockPosition()
        BlockBreakContextKeys.CENTER_POSITION += position.mid()
        BlockBreakContextKeys.MAIN_HAND_ITEM += player.inventory.itemInMainHand.toItem()
        BlockBreakContextKeys.OFF_HAND_ITEM += player.inventory.itemInOffHand.toItem()
    }

    if (entries.shouldCancel(player)) event.isCancelled = true
}