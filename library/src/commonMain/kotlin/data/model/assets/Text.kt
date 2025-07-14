package data.model.assets

import data.model.StoryPassageNovelEvent

sealed interface Text : Asset {
    val text: String

    val animationProps: Animation.Text get() = Animation.Text(
        baseName = "Text",
        name = id
    )

    class Info(
        override val id: String,
        override val text: String
    ) : Text

    class Player(
        override val id: String,
        override val text: String
    ) : Text

    class Character(
        override val id: String,
        override val text: String
    ) : Text

    class Link(
        override val id: String,
        override val text: String
    ) : Text
}

fun StoryPassageNovelEvent.toText(): Text? {
    return when (this) {
        is StoryPassageNovelEvent.CharacterAction -> {
            Text.Character(identifier.toString(), text)
        }
        is StoryPassageNovelEvent.InformationalText -> {
            Text.Info(identifier.toString(), value)
        }
        is StoryPassageNovelEvent.Link -> {
            // TODO: how to handle links without text?
            // should that link be selected automatically?
            Text.Link(identifier.toString(), linkText ?: "No Link Text.")
        }
        is StoryPassageNovelEvent.PlayerText -> {
            Text.Player(identifier.toString(), value)
        }
        else -> null
    }
}