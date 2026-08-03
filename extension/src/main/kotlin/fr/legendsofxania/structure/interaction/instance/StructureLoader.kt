package fr.legendsofxania.structure.interaction.instance

import com.typewritermc.core.entries.Ref
import com.typewritermc.core.utils.point.Position
import com.typewritermc.engine.paper.utils.toBukkitLocation
import fr.legendsofxania.structure.entry.static.template.StructureTemplateEntry
import fr.legendsofxania.structure.manager.TemplateManager
import fr.legendsofxania.structure.util.structure.buildStructureEntities
import fr.legendsofxania.structure.util.structure.structureBlockChanges
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.tofaa.entitylib.wrapper.WrapperEntity
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.block.structure.StructureRotation
import kotlin.collections.emptyList

data class LoadedStructure(
    val origin: Location,
    val chunks: Set<Long>,
    val blocks: Map<Long, BlockData>,
    val entities: List<WrapperEntity>,
)

class StructureLoader(
    private val template: Ref<StructureTemplateEntry>,
    private val position: Position,
    private val rotation: StructureRotation,
    private val ignoreAir: Boolean,
    private val spawnEntities: Boolean,
) {
    @Volatile
    var cached: LoadedStructure? = null
        private set

    private val mutex = Mutex()

    suspend fun load(world: World): LoadedStructure? {
        cached?.let { return it }
        return mutex.withLock {
            cached?.let { return@withLock it }

            val structure = template.entry?.let { TemplateManager.loadTemplate(it) } ?: return@withLock null
            val origin = position.toBukkitLocation(world)
            val data = structureBlockChanges(structure, rotation, ignoreAir, origin)
            val entities = if (spawnEntities) buildStructureEntities(structure, origin, rotation) else emptyList()

            LoadedStructure(origin, data.chunks, data.blocks, entities).also { cached = it }
        }
    }

    fun clear() {
        cached = null
    }
}
