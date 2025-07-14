package data.model.scene

import data.model.assets.Sprite
import data.model.assets.Text

data class SceneRenderState(
    val background: Sprite.Environment? = null,
    val foreground: Sprite.Environment? = null,
    val characters: List<Sprite.Character> = emptyList(),
    val activeCharacter: Sprite.Character? = null,
    val textBoxes: List<Text> = emptyList()
)