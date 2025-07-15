package model.assets

sealed interface Sound : Asset {
    data class SoundEffect(
        override val id: String
    ) : Sound
    data class Music(
        override val id: String
    ) : Sound
}