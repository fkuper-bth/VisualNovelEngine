package etc.utils.preview

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.ViewModel
import api.composable.VisualNovelScene
import api.engine.Configuration
import api.engine.VisualNovelEngine
import fk.visualnovel.engine.library.generated.resources.Res
import fk.visualnovel.engine.library.generated.resources.bank_character_alt_cropped
import fk.visualnovel.engine.library.generated.resources.bank_character_cropped
import fk.visualnovel.engine.library.generated.resources.bank_environment
import fk.visualnovel.engine.library.generated.resources.bank_foreground
import fk.visualnovel.engine.library.generated.resources.glass
import fk.visualnovel.engine.library.generated.resources.plant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import model.assets.Animation
import model.assets.Sprite
import model.scene.SceneRenderStateIds
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Example of another Koin application using this library.
 * @see exampleModule
 */
@Preview
@Composable
private fun ExampleConsumerApplication() {
    KoinApplication(application = {
        modules(exampleModule)
    }) {
        ExampleSceneComposable()
    }
}

/**
 * Example module of another Koin application using this library.
 * @see ExampleScenePreviewViewModel
 */
private val exampleModule = module {
    single<VisualNovelEngine> {
        VisualNovelEngine.init(
            applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            config = Configuration()
        )
    }
    viewModelOf(::ExampleScenePreviewViewModel)
}

/**
 * Example view model of another Koin application using this library.
 * @param visualNovelEngine VisualNovelEngine instance injected by Koin.
 */
private class ExampleScenePreviewViewModel(val visualNovelEngine: VisualNovelEngine) : ViewModel()

/**
 * Example composable of another Koin application using this library.
 * @param viewModel ExampleScenePreviewViewModel injected by Koin.
 */
@Composable
private fun ExampleSceneComposable(
    viewModel: ExampleScenePreviewViewModel = koinInject()
) {
    val scene by viewModel.visualNovelEngine.sceneState.collectAsState()

    val mainCharacterImage = imageResource(Res.drawable.bank_character_cropped)
    val secondaryCharacterImage = imageResource(Res.drawable.bank_character_alt_cropped)
    val backgroundImage = imageResource(Res.drawable.bank_environment)
    val foregroundImage = imageResource(Res.drawable.bank_foreground)
    val glassImage = imageResource(Res.drawable.glass)
    val plantImage = imageResource(Res.drawable.plant)

    // 0. - create assets
    // Define props
    val plantProp = Sprite.Prop(
        id = "plant",
        bitmap = plantImage,
        offsetPercent = Sprite.OffsetPercent(x = 0.62f, y = 0.57f),
        spriteScale = Sprite.Scale(x = 0.4f, y = 0.4f)
    )
    val glassProp = Sprite.Prop(
        id = "glass",
        bitmap = glassImage,
        offsetPercent = Sprite.OffsetPercent(x = 0.05f, y = 0.85f),
        spriteScale = Sprite.Scale(x = 0.15f, y = 0.15f),
    )
    // Define back & foreground environments
    val background = Sprite.Environment(
        id = "bank_background",
        bitmap = backgroundImage,
        contentScale = ContentScale.Companion.FillWidth,
        propIds = listOf(plantProp.id)
    )
    val foreground = Sprite.Environment(
        id = "bank_foreground",
        bitmap = foregroundImage,
        contentScale = ContentScale.Companion.FillWidth,
        offsetPercent = Sprite.OffsetPercent(y = 0.47f),
        propIds = listOf(glassProp.id)
    )
    // Define characters and animations
    val secondaryBankerSpritePre = Sprite.Character(
        id = "banker_secondary",
        bitmap = secondaryCharacterImage,
        alignment = Alignment.Companion.BottomEnd
    )
    val secondaryBankerSpritePost = secondaryBankerSpritePre.copy(
        id = "banker_secondary_post",
        rotation = 720f,
        offsetPercent = Sprite.OffsetPercent(x = 0.5f, y = 0.5f),
        spriteScale = Sprite.Scale(x = 0.5f, y = 0.5f)
    )
    val secondaryBankerSpriteTransition = Animation.SpriteTransition(
        baseName = "banker_secondary",
        name = "smiling",
        durationMillis = 500,
        delayMillis = 0,
        fromSpriteId = secondaryBankerSpritePre.id,
        toSpriteId = secondaryBankerSpritePost.id
    )
    val secondaryBankerSprite = secondaryBankerSpritePre.copy(
        animationIds = listOf(secondaryBankerSpriteTransition.id)
    )
    val mainBankerSpritePre = Sprite.Character(
        id = "banker_main",
        bitmap = mainCharacterImage,
        alignment = Alignment.Companion.BottomStart
    )
    val mainBankerSpritePost = mainBankerSpritePre.copy(
        id = "banker_main_post",
        rotation = 360f,
        offsetPercent = Sprite.OffsetPercent(x = 0f, y = 0f),
        spriteScale = Sprite.Scale(x = 1.5f, y = 1.5f),
        opacity = 0.5f
    )
    val mainBankerSpriteTransition = Animation.SpriteTransition(
        baseName = "banker_main",
        name = "smiling",
        durationMillis = 1000,
        delayMillis = 0,
        fromSpriteId = mainBankerSpritePre.id,
        toSpriteId = mainBankerSpritePost.id
    )
    val mainBankerSprite = mainBankerSpritePre.copy(
        animationIds = listOf(mainBankerSpriteTransition.id)
    )

    LaunchedEffect(Unit) {
        // 1. - load assets
        viewModel.visualNovelEngine.loadAssets(
            listOf(
                glassProp,
                plantProp,
                background,
                foreground,
                secondaryBankerSpritePost,
                secondaryBankerSpriteTransition,
                secondaryBankerSprite,
                mainBankerSpritePost,
                mainBankerSpriteTransition,
                mainBankerSprite
            )
        )

        // 2. - load scene state via asset IDs
        viewModel.visualNovelEngine.loadVisualNovelSceneState(
            state = SceneRenderStateIds(
                backgroundId = background.id,
                foregroundId = foreground.id,
                characterIds = listOf(mainBankerSprite.id, secondaryBankerSprite.id)
            )
        )

        // 3. - handle story passage play
        viewModel.visualNovelEngine.handleStoryPassagePlay(PreviewData.passageData)
    }

    VisualNovelScene(scene, onLinkClick = {
        println("Link clicked: '${it.linkText}', target passage: '${it.targetPassageName}'")
    })
}