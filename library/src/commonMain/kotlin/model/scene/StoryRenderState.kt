package model.scene

import main.contract.StoryPlaythroughRecord

sealed interface StoryRenderState {
    data object Initializing : StoryRenderState
    data object Loading : StoryRenderState
    data class Ended(
        val playthroughRecord: StoryPlaythroughRecord
    ) : StoryRenderState
    data class Rendering(
        val scene: SceneRenderState
    ) : StoryRenderState
    data class Error(
        val message: String,
    ) : StoryRenderState
}