package sound

interface SoundEngine {
    fun playSoundEffect(name: String)
    fun playMusic(name: String, loop: Boolean = true)
    fun stopMusic()
}