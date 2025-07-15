package api.engine

import model.assets.Asset
import fk.story.engine.main.utils.StoryPassagePlayResult
import kotlinx.coroutines.flow.StateFlow
import model.scene.SceneRenderState
import model.scene.SceneRenderStateIds

/**
 * The visual novel engine interface providing access to all the functionality required to display
 * and interact with a visual novel.
 */
interface VisualNovelEngine {
    /**
     * Loads a list of assets into the engine.
     * These can then be accessed and used in a visual novel scene.
     * @param assets The list of assets to load.
     */
    fun loadAssets(assets: List<Asset>)

    /**
     * Loads a new scene state into the engine.
     *
     * @param state The new scene state to load.
     * Each [Asset] should be loaded already and passed in via its identifier.
     */
    fun loadVisualNovelSceneState(state: SceneRenderStateIds)

    /**
     * Handles a story passage play result, updating the engine's state accordingly.
     * @param passageData The result of the story passage play to display.
     */
    fun handleStoryPassagePlay(passageData: StoryPassagePlayResult.DataReady)

    /**
     * Clears the current scene and resets internal state.
     */
    fun reset()

    /**
     * The current visual scene state, used by the UI for rendering.
     */
    val sceneState: StateFlow<SceneRenderState>

    /**
     * Whether the engine is currently executing a visual transition (e.g. fade, move).
     * Useful to block user input during transitions.
     */
    val isBusy: StateFlow<Boolean>
}