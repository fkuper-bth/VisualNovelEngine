package data.model.assets

import data.AssetStore

sealed interface Animation : Asset {
    /**
     * A unique identifier for the animation.
     */
    override val id: String get() = createAnimationIdentifier(baseName, name)
    /**
     * The base name of the animation.
     * Since one sprite can have multiple associated animations,
     * the id is generated from the base name and the animation name.
     */
    val baseName: String
    /**
     * The name of the animation.
     */
    val name: String
    /**
     * The duration of the animation in milliseconds.
     */
    val durationMillis: Int

    /**
     * The delay in milliseconds before the animation starts.
     */
    val delayMillis: Int
    /**
     * Whether the animation should be repeated.
     */
    val repeat: Boolean

    companion object {
        fun createAnimationIdentifier(baseName: String, animationName: String): String {
            return "${baseName}_${animationName}"
        }
    }

    data class Text(
        override val baseName: String,
        override val name: String,
        override val durationMillis: Int = 25,
        override val delayMillis: Int = 0,
        override val repeat: Boolean = false,
    ) : Animation

    data class SpriteSheet(
        override val baseName: String,
        override val name: String,
        override val durationMillis: Int = 500,
        override val delayMillis: Int = 0,
        override val repeat: Boolean = false,
        val animationFrames: List<SpriteAnimationFrame>
    ) : Animation

    data class SpriteTransition(
        override val baseName: String,
        override val name: String,
        override val durationMillis: Int = 500,
        override val delayMillis: Int = 0,
        override val repeat: Boolean = false,
        val fromSpriteId: String,
        val toSpriteId: String,
    ) : Animation

    data class SpriteAnimationFrame(
        val spriteName: String,
        val frameIndex: Int,
        val durationMillis: Long
    )
}

fun Animation.SpriteTransition.resolveToSprite(assetStore: AssetStore): Sprite? {
    return assetStore.assets.value[toSpriteId] as? Sprite
}