package model.scene

sealed interface StoryRenderState {
    data object Initializing : StoryRenderState
    data object Loading : StoryRenderState
    data class Rendering(
        val scene: SceneRenderState
    ) : StoryRenderState
    data class Error(
        val message: String,
    ) : StoryRenderState
}