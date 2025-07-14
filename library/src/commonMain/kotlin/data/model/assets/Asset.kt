package data.model.assets

/**
 * A base interface for all assets.
 * This includes sprites, texts, sounds, animations, etc.
 */
sealed interface Asset {
    /**
     * A unique identifier for the asset.
     */
    val id: String
}
