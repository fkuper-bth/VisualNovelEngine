package etc.di

import service.NovelAnimationService
import service.NovelAnimationServiceImpl
import api.engine.VisualNovelEngine
import api.engine.VisualNovelEngineImpl
import service.AssetStore
import service.AssetStoreImpl
import service.SceneRenderController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

internal val sharedModule = module {
    single<AssetStore> {
        AssetStoreImpl()
    }
    single<NovelAnimationService> {
        NovelAnimationServiceImpl()
    }

    factory<SceneRenderController> {
        SceneRenderController(
            assetStore = get(),
            coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
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