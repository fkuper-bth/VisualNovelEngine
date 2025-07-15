package service

import model.assets.Sprite
import model.assets.Text
import model.scene.SceneRenderState
import model.scene.SceneRenderStateIds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

internal class SceneRenderController(
    assetStore: AssetStore,
    coroutineScope: CoroutineScope
) {
    private val _sceneIds = MutableStateFlow(SceneRenderStateIds())
    val sceneIds: StateFlow<SceneRenderStateIds> = _sceneIds

    val renderState: StateFlow<SceneRenderState> = combine(
        _sceneIds,
        assetStore.assets
    ) { ids, assets ->
        SceneRenderState(
            background = ids.backgroundId?.let { assets[it] as? Sprite.Environment },
            foreground = ids.foregroundId?.let { assets[it] as? Sprite.Environment },
            characters = ids.characterIds.mapNotNull { assets[it] as? Sprite.Character },
            activeCharacter = ids.activeCharacterId?.let { assets[it] as? Sprite.Character },
            textBoxes = ids.textBoxIds.mapNotNull { assets[it] as? Text }
        )
    }.stateIn(
        coroutineScope,
        started = SharingStarted.Companion.Eagerly,
        initialValue = SceneRenderState()
    )

    fun setScene(ids: SceneRenderStateIds = SceneRenderStateIds()) {
        _sceneIds.value = ids
    }
}