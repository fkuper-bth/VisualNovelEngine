package etc.utils

import model.RenderedCharacter
import model.RenderedEnvironment

expect class AssetLoader {
    fun loadCharacter(name: String): RenderedCharacter
    fun loadBackground(name: String): RenderedEnvironment
}