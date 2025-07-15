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
     * @param link The link novel event associated with this asset.
     */
    class Link(
        val link: StoryPassageNovelEvent.Link
    ) : Text {
        override val id: String = link.identifier.toString()
        override val value: String = link.linkText ?: ""
    }
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
            Text.Link(this)
        }
        is StoryPassageNovelEvent.PlayerText -> {
            Text.Player(identifier.toString(), value)
        }
        else -> null
    }
}