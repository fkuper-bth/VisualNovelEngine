package model.scene

import model.assets.Sprite
import model.assets.Text

/**
 * Represents a visual novel scene's rendering state.
 */
data class SceneRenderState(
    /**
     * The background environment for this scene.
     */
    val background: Sprite.Environment? = null,
    /**
     * The foreground environment for this scene.
     */
    val foreground: Sprite.Environment? = null,
    /**
     * The characters in this scene.
     */
    val characters: List<Sprite.Character> = emptyList(),
    /**
     * The active character in this scene.
     */
    val activeCharacter: Sprite.Character? = null,
    /**
     * The text boxes in this scene.
     */
    val textBoxes: List<Text> = emptyList()
)