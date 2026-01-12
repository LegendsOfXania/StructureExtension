package fr.legendsofxania.structure.entry.static.template

import com.typewritermc.core.books.pages.Colors
import com.typewritermc.core.extension.annotations.ContentEditor
import com.typewritermc.core.extension.annotations.Entry
import com.typewritermc.core.extension.annotations.Help
import com.typewritermc.core.extension.annotations.Tags
import com.typewritermc.engine.paper.entry.entries.ArtifactEntry
import fr.legendsofxania.structure.interaction.content.StructureTemplateContentMode

@Entry(
    "structure_template",
    "The template of a structure.",
    Colors.PINK,
    "tabler:building-bridge-2"
)
@Tags("structure_template")
/**
 * The `Structure Template` entry is used to store the room's data.
 *
 * ## How could this be used?
 *
 * This could be used to store the structure's data and reuse them.
 */
class StructureTemplateEntry(
    override val id: String = "",
    override val name: String = "",
    @ContentEditor(StructureTemplateContentMode::class)
    override val artifactId: String = "",
    @Help("Should you save the entities present in your template?")
    val saveEntities: Boolean = false
) : ArtifactEntry