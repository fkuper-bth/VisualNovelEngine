package model.assets

import data.service.contract.StoryImportService
import data.service.utils.StoryImportResult
import main.contract.StoryEngine

import data.model.Story as StoryModel
import model.assets.Story as StoryAsset

class Story(
    override val id: String,
    /**
     * The content of the story.
     * @see StoryModel
     */
    val content: StoryModel
) : Asset {
    private constructor(content: StoryModel) : this(id = content.uuid, content = content)

    companion object {
        /**
         * Tries to create a [model.assets.Story] instance from a JSON string.
         *
         * @property storyJsonContent The JSON content of the story.
         * @property importService The service used to import the story.
         * @throws IllegalStateException if the story import fails.
         */
        fun fromJsonContent(
            storyJsonContent: String,
            importService: StoryImportService = StoryEngine.instance.importService
        ): StoryAsset {
            val result = importService.importStory(storyJsonContent)
            when (result) {
                is StoryImportResult.Success -> {
                    return StoryAsset(result.story)
                }
                is StoryImportResult.Failure -> {
                    throw IllegalStateException(
                        "Story import failed: ${result.reason}"
                    )
                }
            }
        }

        /**
         * Creates a [StoryAsset] instance from a [StoryModel].
         */
        fun fromStoryContent(content: StoryModel): StoryAsset {
            return StoryAsset(content)
        }
    }
}