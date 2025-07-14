package data.model.assets

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import data.AssetStore

sealed interface Sprite : Asset {
    val bitmap: ImageBitmap
    val offsetPercent: OffsetPercent
    val spriteScale: Scale
    val contentScale: ContentScale
    val rotation: Float
    val opacity: Float
    val animationIds: List<String>

    data class Character(
        override val id: String,
        override val bitmap: ImageBitmap,
        override val rotation: Float = 0f,
        override val opacity: Float = 1f,
        override val spriteScale: Scale = Scale(),
        override val contentScale: ContentScale = ContentScale.Companion.Fit,
        override val offsetPercent: OffsetPercent = OffsetPercent(),
        override val animationIds: List<String> = emptyList(),
        val alignment: Alignment = Alignment.Companion.BottomCenter,
    ) : Sprite

    class Environment(
        override val id: String,
        override val bitmap: ImageBitmap,
        override val spriteScale: Scale = Scale(),
        override val contentScale: ContentScale = ContentScale.Companion.Fit,
        override val offsetPercent: OffsetPercent = OffsetPercent(),
        override val opacity: Float = 1f,
        override val rotation: Float = 0f,
        override val animationIds: List<String> = emptyList(),
        val propIds: List<String> = emptyList(),
    ) : Sprite

    class Prop(
        override val id: String,
        override val bitmap: ImageBitmap,
        override val spriteScale: Scale = Scale(),
        override val contentScale: ContentScale = ContentScale.Companion.Fit,
        override val offsetPercent: OffsetPercent = OffsetPercent(),
        override val rotation: Float = 0f,
        override val opacity: Float = 1f,
        override val animationIds: List<String> = emptyList(),
    ) : Sprite

    data class Scale(val x: Float = 1f, val y: Float = 1f)

    data class OffsetPercent(val x: Float = 0f, val y: Float = 0f)
}

fun Sprite.resolveAnimations(assetStore: AssetStore): List<Animation> {
    return animationIds.mapNotNull { assetStore.assets.value[it] as? Animation }
}

fun Sprite.Environment.resolveProps(assetStore: AssetStore): List<Sprite.Prop> {
    return propIds.mapNotNull { assetStore.assets.value[it] as? Sprite.Prop }
}