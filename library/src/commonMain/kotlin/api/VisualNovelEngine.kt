package api

import data.model.assets.Asset
import fk.story.engine.main.utils.StoryPassagePlayResult
import kotlinx.coroutines.flow.StateFlow
import data.model.scene.SceneRenderState
import data.model.scene.SceneRenderStateIds

interface VisualNovelEngine {
    /**
     * Loads a list of assets into the engine.
     */
    fun loadAssets(assets: List<Asset>)

    /**
     * Loads a new scene state into the engine.
     */
    fun loadVisualNovelSceneState(state: SceneRenderStateIds)

    /**
     * Handles a story passage play result, updating the engine's state accordingly.
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