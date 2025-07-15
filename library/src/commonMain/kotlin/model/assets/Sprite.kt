package model.assets

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import service.AssetStore

/**
 * Represents a sprite asset.
 */
sealed interface Sprite : Asset {
    /**
     * The image bitmap of the sprite.
     */
    val bitmap: ImageBitmap

    /**
     * The offset of the sprite in percent.
     */
    val offsetPercent: OffsetPercent

    /**
     * The scaling of the sprite. Applied independently of [contentScale].
     */
    val spriteScale: Scale

    /**
     * The content scale of the sprite.
     */
    val contentScale: ContentScale

    /**
     * The rotation of the sprite in degrees.
     */
    val rotation: Float

    /**
     * The opacity of the sprite.
     */
    val opacity: Float

    /**
     * IDs of any animations associated with this sprite.
     */
    val animationIds: List<String>

    /**
     * Represents a character sprite.
     */
    data class Character(
        override val id: String,
        override val bitmap: ImageBitmap,
        override val rotation: Float = 0f,
        override val opacity: Float = 1f,
        override val spriteScale: Scale = Scale(),
        override val contentScale: ContentScale = ContentScale.Fit,
        override val offsetPercent: OffsetPercent = OffsetPercent(),
        override val animationIds: List<String> = emptyList(),
        /**
         * The alignment of the character's sprite.
         */
        val alignment: Alignment = Alignment.BottomCenter,
    ) : Sprite

    /**
     * Represents an environment sprite.
     */
    class Environment(
        override val id: String,
        override val bitmap: ImageBitmap,
        override val spriteScale: Scale = Scale(),
        override val contentScale: ContentScale = ContentScale.Fit,
        override val offsetPercent: OffsetPercent = OffsetPercent(),
        override val opacity: Float = 1f,
        override val rotation: Float = 0f,
        override val animationIds: List<String> = emptyList(),
        /**
         * The IDs of any [Prop] associated with this environment.
         */
        val propIds: List<String> = emptyList(),
    ) : Sprite

    /**
     * Represents a prop sprite.
     */
    class Prop(
        override val id: String,
        override val bitmap: ImageBitmap,
        override val spriteScale: Scale = Scale(),
        override val contentScale: ContentScale = ContentScale.Fit,
        override val offsetPercent: OffsetPercent = OffsetPercent(),
        override val rotation: Float = 0f,
        override val opacity: Float = 1f,
        override val animationIds: List<String> = emptyList(),
    ) : Sprite

    data class Scale(val x: Float = 1f, val y: Float = 1f)

    data class OffsetPercent(val x: Float = 0f, val y: Float = 0f)
}

internal fun Sprite.resolveAnimations(assetStore: AssetStore): List<Animation> {
    return animationIds.mapNotNull { assetStore.assets.value[it] as? Animation }
}

internal fun Sprite.Environment.resolveProps(assetStore: AssetStore): List<Sprite.Prop> {
    return propIds.mapNotNull { assetStore.assets.value[it] as? Sprite.Prop }
}