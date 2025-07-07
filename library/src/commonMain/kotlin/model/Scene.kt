package model

import androidx.compose.ui.graphics.ImageBitmap
import data.model.StoryPassageNovelEvent
import kotlin.uuid.Uuid

enum class CharacterPosition {
    LEFT,
    CENTER,
    RIGHT
}

data class RenderedCharacter(
    val name: String,
    val bitmap: ImageBitmap,
    val opacity: Float = 1f,
    val position: CharacterPosition = CharacterPosition.CENTER
)

data class RenderedEnvironment(
    val name: String,
    val bitmap: ImageBitmap,
    val opacity: Float = 1f
)

sealed interface RenderedText {
    val id: Uuid
    val text: String

    class Info(
        override val id: Uuid,
        override val text: String
    ) : RenderedText

    class Player(
        override val id: Uuid,
        override val text: String
    ) : RenderedText

    class Character(
        override val id: Uuid,
        override val text: String
    ) : RenderedText

    class Link(
        override val id: Uuid,
        override val text: String
    ) : RenderedText
}

fun StoryPassageNovelEvent.toRenderedText(): RenderedText? {
    return when (this) {
        is StoryPassageNovelEvent.CharacterAction -> {
            RenderedText.Character(identifier, text)
        }
        is StoryPassageNovelEvent.InformationalText -> {
            RenderedText.Info(identifier, value)
        }
        is StoryPassageNovelEvent.Link -> {
            // TODO: how to handle links without text?
            // should that link be selected automatically?
            RenderedText.Link(identifier, linkText ?: "No Link Text.")
        }
        is StoryPassageNovelEvent.PlayerText -> {
            RenderedText.Player(identifier, value)
        }
        else -> null
    }
}

data class SceneRenderState(
    val environment: RenderedEnvironment? = null,
    val characters: List<RenderedCharacter> = emptyList(),
    val activeCharacter: RenderedCharacter? = null,
    val textBoxes: List<RenderedText> = emptyList()
)