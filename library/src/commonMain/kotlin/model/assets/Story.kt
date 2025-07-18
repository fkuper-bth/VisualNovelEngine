package model.assets

import data.model.Story
import data.service.utils.StoryImportResult
import main.contract.StoryEngine

/**
 * Represents a story asset ready for use.
 *
 * @property id The unique identifier for the story.
 * @property jsonContent The JSON content of the story.
 * @property storyEngine The engine used to import the story.
 *
 * @constructor Creates a new [Story] instance.
 * @throws IllegalStateException if the story import fails.
 */
data class Story(
    override val id: String,
    val jsonContent: String,
    private val storyEngine: StoryEngine = StoryEngine.Companion.instance
) : Asset {
    /**
     * The content of the story.
     * @see data.model.Story
     */
    val content: Story

    init {
        val result = storyEngine.importService.importStory(jsonContent)
        when (result) {
            is StoryImportResult.Success -> {
                content = result.story
            }
            is StoryImportResult.Failure -> {
                throw IllegalStateException(
                    "Story import for $id failed: ${result.reason}"
                )
            }
        }
    }
}