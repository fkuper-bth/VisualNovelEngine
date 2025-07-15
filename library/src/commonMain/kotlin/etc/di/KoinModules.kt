package etc.di

import api.engine.Configuration
import api.engine.VisualNovelEngine
import api.engine.VisualNovelEngineImpl
import fk.story.engine.main.contract.StoryEngine
import kotlinx.coroutines.CoroutineScope
import org.koin.dsl.module
import service.AssetStore
import service.AssetStoreImpl
import service.NovelAnimationService
import service.NovelAnimationServiceImpl
import service.SceneRenderController

/**
 * Creates a Koin module for the visual novel engine.
 * @param coroutineScope The coroutine scope to use for the visual novel engine.
 * @param config The configuration to use for the visual novel engine.
 */
internal fun createVisualNovelEngineModule(
    coroutineScope: CoroutineScope,
    config: Configuration = Configuration()
) = module {
    single<Configuration> {
        config
    }
    single<AssetStore> {
        AssetStoreImpl()
    }
    single<NovelAnimationService> {
        NovelAnimationServiceImpl()
    }
    single<StoryEngine> {
        StoryEngine.instance
    }

    factory<SceneRenderController> {
        SceneRenderController(
            assetStore = get(),
            coroutineScope = coroutineScope
        )
    }
    factory<VisualNovelEngine> {
        VisualNovelEngineImpl(
            assetStore = get(),
            animationService = get(),
            sceneRenderController = get(),
            soundEngine = null
        )
    }
}