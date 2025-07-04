package etc.di

import androidx.compose.animation.core.animate
import animation.NovelAnimationService
import animation.NovelAnimationServiceImpl
import api.VisualNovelEngine
import api.VisualNovelEngineImpl
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val sharedModule = module {
    singleOf(::NovelAnimationServiceImpl) {
        bind<NovelAnimationService>()
    }

    factory<VisualNovelEngine> {
        VisualNovelEngineImpl(
            animationService = get(),
            assetLoader = null,
            soundEngine = null
        )
    }
}