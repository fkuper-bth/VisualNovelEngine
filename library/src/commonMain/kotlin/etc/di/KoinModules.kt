package etc.di

import animation.NovelAnimationService
import animation.NovelAnimationServiceImpl
import api.VisualNovelEngine
import api.VisualNovelEngineImpl
import data.AssetStore
import data.AssetStoreImpl
import data.SceneRenderController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val sharedModule = module {
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