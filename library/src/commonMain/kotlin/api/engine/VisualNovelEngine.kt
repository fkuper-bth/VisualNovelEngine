package api.engine

import etc.di.createVisualNovelEngineModule
import model.assets.Asset
import fk.story.engine.main.utils.StoryPassagePlayResult
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import model.scene.SceneRenderState
import model.scene.SceneRenderStateIds
import org.koin.core.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.dsl.koinApplication

/**
 * The visual novel engine interface providing access to all the functionality required to display
 * and interact with a visual novel.
 */
interface VisualNovelEngine {
    /**
     * Loads a list of assets into the engine.
     * These can then be accessed and used in a visual novel scene.
     * @param assets The list of assets to load.
     */
    fun loadAssets(assets: List<Asset>)

    /**
     * Loads a new scene state into the engine.
     *
     * @param state The new scene state to load.
     * Each [Asset] should be loaded already and passed in via its identifier.
     */
    fun loadVisualNovelSceneState(state: SceneRenderStateIds)

    /**
     * Handles a story passage play result, updating the engine's state accordingly.
     * @param passageData The result of the story passage play to display.
     */
    fun handleStoryPassagePlay(passageData: StoryPassagePlayResult.DataReady)

    /**
     * Clears the current scene and resets internal state.
     */
    fun reset()

    /**
     * The current visual scene state, used by the UI for rendering.
     */
    val sceneState: StateFlow<SceneRenderState>

    /**
     * Whether the engine is currently executing a visual transition (e.g. fade, move).
     * Useful to block user input during transitions.
     */
    val isBusy: StateFlow<Boolean>

    companion object {
        internal var koinApp: KoinApplication? = null; private set
        private var engineInstance: VisualNovelEngine? = null
        private val lock = SynchronizedObject()

        /**
         * Initializes the engine and returns the singleton instance of [VisualNovelEngine].
         * If the engine has already been initialized, this method will return the existing instance
         * and ignore subsequent parameters.
         *
         * It's recommended to call this method only once during the application's setup phase.
         *
         * @param applicationScope A CoroutineScope that the VisualNovelEngine and its components
         *                         will use for their operations.
         * @param config Optional configuration data for the engine, applied on first init.
         * @return The singleton instance of [VisualNovelEngine].
         */
        fun init(
            applicationScope: CoroutineScope,
            config: Configuration = Configuration()
        ) : VisualNovelEngine {
            synchronized(lock) {
                if (engineInstance == null) {
                    if (koinApp == null) {
                        koinApp = koinApplication {
                            modules(
                                createVisualNovelEngineModule(
                                    coroutineScope = applicationScope,
                                    config = config
                                )
                            )
                        }
                    }
                    engineInstance = koinApp!!.koin.get<VisualNovelEngine>()
                }
                return engineInstance!!
            }
        }

        /**
         * Shuts down the VisualNovelEngine and its internal Koin context.
         * After calling this, init() can be used to create a new instance.
         * Call this when the engine is definitively no longer needed in the application.
         */
        fun dispose() {
            synchronized(lock) {
                engineInstance?.reset()
                stopKoinInternal()
                engineInstance = null
            }
        }

        private fun stopKoinInternal() {
            if (koinApp != null) {
                stopKoin()
                koinApp = null
                println("Internal Koin context stopped.")
            }
        }
    }
}