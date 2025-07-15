package api.engine

// TODO: what configuration options should be available?
// also this config object is currently not used anywhere, start using it to apply the configuration

/**
 * Configuration object for the engine.
 */
data class Configuration(
    val textRenderingSpeed: Float = 50f
)