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
import fr.legendsofxania.structure.enum.InteractionType
import fr.legendsofxania.structure.enum.ShiftType
import fr.legendsofxania.structure.enum.hasItemInHand
import fr.legendsofxania.structure.event.AsyncInteractFakeBlockEvent
import org.bukkit.Material
import java.util.Optional
import kotlin.reflect.KClass

@Entry(
    "on_interact_fake_block_event",
    "When a player interacts with a fake block.",
    Colors.YELLOW,
    "mingcute:pickax-line"
)
@ContextKeys(InteractFakeBlockContextKeys::class)
/**
 * The `Interact Fake Block Event` is triggered when a player interacts with a fake block by right-clicking it.
 *
 * ## How could this be used?
 *
 * This could be used to create special interactions with blocks, such as opening a secret door when you right-click a certain block, or a block that requires a key to open.
 */
class OnInteractFakeBlockEventEntry(
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
    val interactionType: InteractionType = InteractionType.ALL,
    val shiftType: ShiftType = ShiftType.ANY,
    override val cancel: Var<Boolean> = ConstVar(false)
) : CancelableEventEntry

enum class InteractFakeBlockContextKeys(override val klass: KClass<*>) : EntryContextKey {
    @KeyType(Position::class)
    POSITION(Position::class),

    @KeyType(Item::class)
    MAIN_HAND_ITEM(Item::class),

    @KeyType(Item::class)
    OFF_HAND_ITEM(Item::class),
}

@EntryListener(OnInteractFakeBlockEventEntry::class)
fun onInteractFakeBlock(event: AsyncInteractFakeBlockEvent, query: Query<OnInteractFakeBlockEventEntry>) {
    val player = event.player
    val block = event.block
    val location = block.location

    val entries = query.findWhere { entry ->
        // Check if the player is sneaking
        if (!entry.shiftType.isApplicable(player)) return@findWhere false

        // Check if the player is interacting with the block in the correct way
        if (!entry.interactionType.actions.contains(event.action)) return@findWhere false

        // Check if the player clicked on the correct location
        if (!entry.location.map { it.get(player).sameBlock(location.toPosition()) }
                .orElse(true)) return@findWhere false

        // Check if the player is holding the correct item
        if (!hasItemInHand(player, entry.hand, entry.itemInHand.get(player))) return@findWhere false

        entry.blocks.map { it.contains(block.type) }.orElse(true)
    }.toList()
    if (entries.isEmpty()) return

    entries.startDialogueWithOrNextDialogue(player) {
        InteractFakeBlockContextKeys.POSITION += location.toPosition()
        InteractFakeBlockContextKeys.MAIN_HAND_ITEM += player.inventory.itemInMainHand.toItem()
        InteractFakeBlockContextKeys.OFF_HAND_ITEM += player.inventory.itemInOffHand.toItem()
    }

    if (entries.shouldCancel(player)) event.isCancelled = true
}