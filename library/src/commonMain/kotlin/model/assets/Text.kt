package model.assets

import data.model.StoryPassageNovelEvent

/**
 * Represents a text asset.
 */
sealed interface Text : Asset {
    /**
     * This text assets value.
     */
    val value: String

    /**
     * The animation object associated with text assets.
     */
    val animationProps: Animation.Text get() = Animation.Text(
        baseName = "Text",
        name = id
    )

    /**
     * Represents an info text asset.
     */
    class Info(
        override val id: String,
        override val value: String
    ) : Text

    /**
     * Represents a player text asset.
     */
    class Player(
        override val id: String,
        override val value: String
    ) : Text

    /**
     * Represents a character text asset.
     */
    class Character(
        override val id: String,
        override val value: String
    ) : Text

    /**
     * Represents a link text asset.
     */
    class Link(
        override val id: String,
        override val value: String
    ) : Text
}

internal fun StoryPassageNovelEvent.toText(): Text? {
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