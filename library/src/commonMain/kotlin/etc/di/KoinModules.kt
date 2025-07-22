package etc.di

import api.engine.Configuration
import api.engine.VisualNovelEngine
import api.engine.VisualNovelEngineImpl
import api.engine.VisualNovelStoryPlayer
import api.engine.VisualNovelStoryPlayerImpl
import kotlinx.coroutines.CoroutineScope
import main.contract.StoryEngine
import org.koin.dsl.module
import service.AssetStore
import service.AssetStoreImpl
import service.NovelAnimationService
import service.NovelAnimationServiceImpl
import service.StoryRenderController

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
        NovelAnimationServiceImpl(
            coroutineScope = coroutineScope
        )
    }
    single<StoryEngine> {
        StoryEngine.instance
    }

    factory<StoryRenderController> {
        StoryRenderController(
            assetStore = get(),
            coroutineScope = coroutineScope
        )
    }
    factory<VisualNovelStoryPlayer> {
        VisualNovelStoryPlayerImpl(
            assetStore = get(),
            animationService = get(),
            storyRenderController = get(),
            storyEngine = get(),
            coroutineScope = coroutineScope,
            soundEngine = null
        )
    }
    factory<VisualNovelEngine> {
        VisualNovelEngineImpl(
            storyPlayer = get(),
            assetStore = get()
        )
    }
}