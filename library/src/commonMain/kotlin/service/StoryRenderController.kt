package service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import model.assets.Sprite
import model.assets.Text
import model.scene.SceneRenderState
import model.scene.SceneRenderStateIds
import model.scene.StoryRenderState

internal class StoryRenderController(
    assetStore: AssetStore,
    coroutineScope: CoroutineScope
) {
    private val _requestedSceneIds = MutableStateFlow(
        value = SceneRenderStateIds()
    )
    val requestedSceneIds: StateFlow<SceneRenderStateIds> = _requestedSceneIds.asStateFlow()
    private val _storyRenderState = MutableStateFlow<StoryRenderState>(
        value = StoryRenderState.Initializing
    )
    val storyRenderState: StateFlow<StoryRenderState> = _storyRenderState.asStateFlow()

    private val assetFlow: Flow<StoryRenderState> = combine(
        _requestedSceneIds,
        assetStore.assets
    ) { ids, assets ->
        if (ids == SceneRenderStateIds() && assets.isEmpty()) {
            return@combine StoryRenderState.Rendering(SceneRenderState())
        }

        val background = ids.backgroundId?.let { assets[it] as? Sprite.Environment }
        val foreground = ids.foregroundId?.let { assets[it] as? Sprite.Environment }
        val characters = ids.characterIds.mapNotNull { assets[it] as? Sprite.Character }
        val activeCharacter = ids.activeCharacterId?.let { assets[it] as? Sprite.Character }
        val textBoxes = ids.textBoxIds.mapNotNull { assets[it] as? Text }

        StoryRenderState.Rendering(
            SceneRenderState(
                background = background,
                foreground = foreground,
                characters = characters,
                activeCharacter = activeCharacter,
                textBoxes = textBoxes
            )
        )
    }

    init {
        coroutineScope.launch {
            assetFlow
                .onStart {
                    _storyRenderState.value = StoryRenderState.Loading
                }
                .collectLatest { newRenderState ->
                    _storyRenderState.value = newRenderState
                }
        }
    }

    fun setScene(ids: SceneRenderStateIds = SceneRenderStateIds()) {
        _requestedSceneIds.value = ids
    }

    fun setErrorState(message: String) {
        _storyRenderState.value = StoryRenderState.Error(message)
    }
}