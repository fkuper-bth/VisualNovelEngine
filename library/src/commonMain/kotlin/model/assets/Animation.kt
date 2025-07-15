package model.assets

import service.AssetStore

/**
 * Represents an animation asset.
 */
sealed interface Animation : Asset {
    /**
     * A unique identifier for the animation.
     */
    override val id: String get() = createIdentifier(baseName, name)
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
        internal fun createIdentifier(baseName: String, animationName: String): String {
            return "${baseName}_${animationName}"
        }
    }

    /**
     * Represents a text animation.
     */
    data class Text(
        override val baseName: String,
        override val name: String,
        override val durationMillis: Int = 25,
        override val delayMillis: Int = 0,
        override val repeat: Boolean = false,
    ) : Animation

    /**
     * Represents a sprite sheet animation composed of multiple frames.
     */
    data class SpriteSheet(
        override val baseName: String,
        override val name: String,
        override val durationMillis: Int = 500,
        override val delayMillis: Int = 0,
        override val repeat: Boolean = false,
        val animationFrames: List<SpriteAnimationFrame>
    ) : Animation

    /**
     * Represents a sprite transition animation.
     * Animates the sprite's transition from one sprite to another.
     */
    data class SpriteTransition(
        override val baseName: String,
        override val name: String,
        override val durationMillis: Int = 500,
        override val delayMillis: Int = 0,
        override val repeat: Boolean = false,
        /**
         * The ID of the sprite from which the transition starts.
         */
        val fromSpriteId: String,
        /**
         * The ID of the sprite to which the transition goes.
         */
        val toSpriteId: String,
    ) : Animation

    data class SpriteAnimationFrame(
        val spriteName: String,
        val frameIndex: Int,
        val durationMillis: Long
    )
}

internal fun Animation.SpriteTransition.resolveToSprite(assetStore: AssetStore): Sprite? {
    return assetStore.assets.value[toSpriteId] as? Sprite
}