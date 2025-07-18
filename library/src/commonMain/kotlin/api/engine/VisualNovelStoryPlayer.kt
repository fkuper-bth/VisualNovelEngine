package api.engine

import data.model.StoryPassageNovelEvent
import kotlinx.coroutines.flow.StateFlow
import model.assets.Asset
import model.scene.SceneRenderStateIds
import model.scene.StoryRenderState

/**
 * Represents a visual novel story player which provides functionality to play visual novel stories.
 */
// TODO: make this internal and move required public APIs to engine layer
interface VisualNovelStoryPlayer {
    /**
     * Starts playing a visual novel story.
     * @param storyId The ID of the story to play.
     */
    fun playStory(storyId: String)

    /**
     * Loads a new scene state into the engine.
     *
     * @param state The new scene state to load.
     * Each [Asset] should be loaded already and passed in via its identifier.
     */
    fun loadVisualNovelSceneState(state: SceneRenderStateIds)

    /**
     * Chooses a passage in the story.
     * @param link The link to the passage to choose.
     */
    fun chooseStoryPassage(link: StoryPassageNovelEvent.Link)

    /**
     * Clears the current scene and resets internal state.
     */
    fun reset()

    /**
     * The current UI state of the player.
     */
    val uiState: StateFlow<StoryRenderState>

    /**
     * Whether the engine is currently executing a visual transition (e.g. fade, move).
     * Useful to block user input during transitions.
     */
    val isBusy: StateFlow<Boolean>
}