package model

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import data.model.StoryPassageNovelEvent
import kotlin.uuid.Uuid

data class SceneRenderState(
    val background: Sprite.Environment? = null,
    val foreground: Sprite.Environment? = null,
    val characters: List<Sprite.Character> = emptyList(),
    val activeCharacter: Sprite.Character? = null,
    val textBoxes: List<RenderText> = emptyList()
)

sealed class Sprite(
    val name: String,
    val bitmap: ImageBitmap,
    val offsetPercent: OffsetPercent = OffsetPercent(),
    val spriteScale: Scale = Scale(),
    val contentScale: ContentScale = ContentScale.Fit,
    val rotation: Float = 0f,
    val opacity: Float = 1f
) {
    data class Scale(val x: Float = 1f, val y: Float = 1f)
    data class OffsetPercent(val x: Float = 0f, val y: Float = 0f)

    class Character(
        name: String,
        bitmap: ImageBitmap,
        opacity: Float = 1f,
        spriteScale: Scale = Scale(),
        contentScale: ContentScale = ContentScale.Fit,
        offsetPercent: OffsetPercent = OffsetPercent(),
        val alignment: Alignment = Alignment.BottomCenter,
    ) : Sprite(
        name = name, bitmap = bitmap, offsetPercent = offsetPercent, spriteScale = spriteScale,
        contentScale = contentScale, opacity = opacity
    )

    class Environment(
        name: String,
        bitmap: ImageBitmap,
        spriteScale: Scale = Scale(),
        contentScale: ContentScale = ContentScale.Fit,
        offsetPercent: OffsetPercent = OffsetPercent(),
        opacity: Float = 1f,
        val props: List<Prop> = emptyList()
    ) : Sprite(
        name = name, bitmap = bitmap, offsetPercent = offsetPercent, spriteScale = spriteScale,
        contentScale = contentScale, opacity = opacity
    )

    class Prop(
        name: String,
        bitmap: ImageBitmap,
        spriteScale: Scale = Scale(),
        contentScale: ContentScale = ContentScale.Fit,
        offsetPercent: OffsetPercent = OffsetPercent(),
        rotation: Float = 0f,
        opacity: Float = 1f,
    ) : Sprite(
        name = name, bitmap = bitmap, offsetPercent = offsetPercent, spriteScale = spriteScale,
        contentScale = contentScale, rotation = rotation, opacity = opacity
    )
}

sealed interface RenderText {
    val id: Uuid
    val text: String

    class Info(
        override val id: Uuid,
        override val text: String
    ) : RenderText

    class Player(
        override val id: Uuid,
        override val text: String
    ) : RenderText

    class Character(
        override val id: Uuid,
        override val text: String
    ) : RenderText

    class Link(
        override val id: Uuid,
        override val text: String
    ) : RenderText
}

fun StoryPassageNovelEvent.toRenderedText(): RenderText? {
    return when (this) {
        is StoryPassageNovelEvent.CharacterAction -> {
            RenderText.Character(identifier, text)
        }
        is StoryPassageNovelEvent.InformationalText -> {
            RenderText.Info(identifier, value)
        }
        is StoryPassageNovelEvent.Link -> {
            // TODO: how to handle links without text?
            // should that link be selected automatically?
            RenderText.Link(identifier, linkText ?: "No Link Text.")
        }
        is StoryPassageNovelEvent.PlayerText -> {
            RenderText.Player(identifier, value)
        }
        else -> null
    }
}