package api.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import model.assets.Animation
import api.engine.VisualNovelEngine
import data.model.StoryPassageNovelEvent
import etc.di.sharedModule
import etc.utils.PreviewData
import fk.visualnovel.engine.library.generated.resources.Res
import fk.visualnovel.engine.library.generated.resources.bank_character_alt_cropped
import fk.visualnovel.engine.library.generated.resources.bank_character_cropped
import fk.visualnovel.engine.library.generated.resources.bank_environment
import fk.visualnovel.engine.library.generated.resources.bank_foreground
import fk.visualnovel.engine.library.generated.resources.glass
import fk.visualnovel.engine.library.generated.resources.plant
import model.scene.SceneRenderState
import model.scene.SceneRenderStateIds
import model.assets.Sprite
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import scene.composables.VisualNovelSceneEnvironment
import scene.composables.VisualNovelSceneMainContent

/**
 * Composable for displaying a visual novel scene.
 * @param scene The scene to display.
 * @param onLinkClick The callback to invoke when a link is clicked.
 * @param aspectRatio The aspect ratio of the scene.
 * @param modifier The modifier to apply to the scene.
 */
@Composable
fun VisualNovelScene(
    scene: SceneRenderState,
    onLinkClick: (StoryPassageNovelEvent.Link) -> Unit,
    aspectRatio: Float = 9f / 16f,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentWidth(align = Alignment.CenterHorizontally)
            .aspectRatio(aspectRatio)
    ) {
        scene.background?.let {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val size = IntSize(constraints.maxWidth, constraints.maxHeight)
                VisualNovelSceneEnvironment(environment = it, containerSize = size)
            }
        }

        VisualNovelSceneMainContent(
            scene = scene,
            onLinkClick = onLinkClick,
            modifier = Modifier.fillMaxSize()
        )

        scene.foreground?.let {
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val size = IntSize(constraints.maxWidth, constraints.maxHeight)
                VisualNovelSceneEnvironment(environment = it, containerSize = size)
            }
        }
    }
}

@Preview
@Composable
private fun ExampleScenePreview() {
    KoinApplication(application = {
        modules(sharedModule)
    }) {
        val visualNovelEngine: VisualNovelEngine = koinInject()
        val scene by visualNovelEngine.sceneState.collectAsState()

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
            contentScale = ContentScale.FillWidth,
            propIds = listOf(plantProp.id)
        )
        val foreground = Sprite.Environment(
            id = "bank_foreground",
            bitmap = foregroundImage,
            contentScale = ContentScale.FillWidth,
            offsetPercent = Sprite.OffsetPercent(y = 0.47f),
            propIds = listOf(glassProp.id)
        )
        // Define characters and animations
        val secondaryBankerSpritePre = Sprite.Character(
            id = "banker_secondary",
            bitmap = secondaryCharacterImage,
            alignment = Alignment.BottomEnd
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
            alignment = Alignment.BottomStart
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
            visualNovelEngine.loadAssets(listOf(
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
            ))

            // 2. - load scene state via asset IDs
            visualNovelEngine.loadVisualNovelSceneState(
                state = SceneRenderStateIds(
                    backgroundId = background.id,
                    foregroundId = foreground.id,
                    characterIds = listOf(mainBankerSprite.id, secondaryBankerSprite.id)
                )
            )

            // 3. - handle story passage play
            visualNovelEngine.handleStoryPassagePlay(PreviewData.passageData)
        }
        VisualNovelScene(scene, onLinkClick = {})
    }
}