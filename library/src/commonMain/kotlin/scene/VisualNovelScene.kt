package scene

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
import api.VisualNovelEngine
import etc.di.sharedModule
import etc.utils.PreviewData
import fk.visualnovel.engine.library.generated.resources.Res
import fk.visualnovel.engine.library.generated.resources.bank_character_alt_cropped
import fk.visualnovel.engine.library.generated.resources.bank_character_cropped
import fk.visualnovel.engine.library.generated.resources.bank_environment
import fk.visualnovel.engine.library.generated.resources.bank_foreground
import fk.visualnovel.engine.library.generated.resources.glass
import fk.visualnovel.engine.library.generated.resources.plant
import model.SceneRenderState
import model.Sprite
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import scene.composables.VisualNovelSceneEnvironment
import scene.composables.VisualNovelSceneMainContent

@Composable
fun VisualNovelScene(
    scene: SceneRenderState,
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

        VisualNovelSceneMainContent(scene = scene, modifier = Modifier.fillMaxSize())

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

        LaunchedEffect(Unit) {
            visualNovelEngine.loadVisualNovelSceneState(
                state = SceneRenderState(
                    background = Sprite.Environment(
                        name = "bank_background",
                        bitmap = backgroundImage,
                        contentScale = ContentScale.FillWidth,
                        props = listOf(
                            Sprite.Prop(
                                name = "plant",
                                bitmap = plantImage,
                                offsetPercent = Sprite.OffsetPercent(x = 0.62f, y = 0.57f),
                                spriteScale = Sprite.Scale(x = 0.4f, y = 0.4f)
                            )
                        )
                    ),
                    foreground = Sprite.Environment(
                        name = "bank_foreground",
                        bitmap = foregroundImage,
                        contentScale = ContentScale.FillWidth,
                        offsetPercent = Sprite.OffsetPercent(y = 0.47f),
                        props = listOf(
                            Sprite.Prop(
                                name = "glass",
                                bitmap = glassImage,
                                offsetPercent = Sprite.OffsetPercent(x = 0.05f, y = 0.85f),
                                spriteScale = Sprite.Scale(x = 0.15f, y = 0.15f),
                            )
                        )
                    ),
                    characters = listOf(
                        Sprite.Character(
                            name = "banker_main",
                            bitmap = mainCharacterImage,
                            alignment = Alignment.BottomStart
                        ),
                        Sprite.Character(
                            name = "banker_secondary",
                            bitmap = secondaryCharacterImage,
                            alignment = Alignment.BottomEnd
                        )
                    )
                )
            )
            visualNovelEngine.handleStoryPassagePlay(PreviewData.passageData)
        }
        VisualNovelScene(scene)
    }
}