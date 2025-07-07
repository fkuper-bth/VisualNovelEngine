package scene

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import animation.AnimationCommand
import animation.NovelAnimationService
import api.VisualNovelEngine
import etc.di.sharedModule
import etc.utils.PreviewData
import etc.utils.fadingEdge
import fk.visualnovel.engine.library.generated.resources.Res
import fk.visualnovel.engine.library.generated.resources.bank_character_alt_cropped
import fk.visualnovel.engine.library.generated.resources.bank_character_cropped
import fk.visualnovel.engine.library.generated.resources.bank_environment
import model.CharacterPosition
import model.RenderedText
import model.SceneRenderState
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import scene.view.AnimatedText
import scene.view.CharacterTextView
import scene.view.InfoTextView
import scene.view.LinkView
import scene.view.PlayerTextView
import kotlin.uuid.Uuid

@Composable
fun VisualNovelScene(
    scene: SceneRenderState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(modifier.fillMaxSize().background(Color.Black)) {
        // Environment background
        scene.environment?.let {
            Image(
                bitmap = it.bitmap,
                contentDescription = it.name,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = it.opacity),
                contentScale = ContentScale.Crop
            )
        }

        // Text & Character rendered over the environment background
        Column(modifier.fillMaxSize()) {
            if (scene.textBoxes.isNotEmpty()) {
                val colorStops = arrayOf(
                    0f to Color.Transparent,
                    0.03f to Color.Red, 0.97f to Color.Red,
                    1f to Color.Transparent
                )
                val topBottomFade = Brush.verticalGradient(colorStops = colorStops)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fadingEdge(topBottomFade)
                        .fillMaxWidth()
                        .fillMaxHeight(fraction = 0.5f)
                        .weight(1f)
                        .verticalScroll(scrollState)
                        .padding(2.dp)
                ) {
                    scene.textBoxes.forEach {
                        when (it) {
                            is RenderedText.Info -> {
                                AnimatableNovelEventTextView(
                                    eventAnimationId = it.id,
                                    staticTextContent = { InfoTextView(it.text) },
                                    animateTextContent = { animationService ->
                                        AnimatedText(
                                            text = it.text,
                                            textView = { displayedText -> InfoTextView(displayedText) },
                                            eventIdentifier = it.id,
                                            animationService = animationService
                                        )
                                    }
                                )
                            }
                            is RenderedText.Player -> {
                                AnimatableNovelEventTextView(
                                    eventAnimationId = it.id,
                                    staticTextContent = { PlayerTextView(it.text) },
                                    animateTextContent = { animationService ->
                                        AnimatedText(
                                            text = it.text,
                                            textView = { displayedText -> PlayerTextView(displayedText) },
                                            eventIdentifier = it.id,
                                            animationService = animationService
                                        )
                                    }
                                )
                            }
                            is RenderedText.Character -> {
                                AnimatableNovelEventTextView(
                                    eventAnimationId = it.id,
                                    staticTextContent = { CharacterTextView(it.text) },
                                    animateTextContent = { animationService ->
                                        AnimatedText(
                                            text = it.text,
                                            textView = { displayedText -> CharacterTextView(displayedText) },
                                            eventIdentifier = it.id,
                                            animationService = animationService
                                        )
                                    }
                                )
                            }
                            is RenderedText.Link -> {
                                LinkView(onClick = {
                                    // TODO: callback to handle playing next passage
                                }, text = it.text)
                            }
                        }
                    }
                }
                // TODO: improve scroll animation to consider height event views
                LaunchedEffect(scene.textBoxes.size) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                }
            } else {
                Spacer(Modifier.weight(1f))
            }

            Crossfade(
                targetState = scene.activeCharacter,
                animationSpec = tween(500),
                modifier = Modifier.fillMaxHeight(0.5f)
            ) { activeCharacter ->
                if (activeCharacter != null) {
                    val characterPosition = when (activeCharacter.position) {
                        CharacterPosition.CENTER -> Alignment.BottomCenter
                        CharacterPosition.LEFT -> Alignment.BottomStart
                        CharacterPosition.RIGHT -> Alignment.BottomEnd
                    }
                    Image(
                        bitmap = activeCharacter.bitmap,
                        contentDescription = activeCharacter.name,
                        alignment = characterPosition,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(alpha = activeCharacter.opacity),
                        contentScale = ContentScale.FillHeight
                    )
                } else {
                    Spacer(Modifier.fillMaxHeight(0.5f))
                }
            }
        }
    }
}

@Composable
private fun AnimatableNovelEventTextView(
    eventAnimationId: Uuid,
    novelAnimationService: NovelAnimationService = koinInject(),
    staticTextContent: @Composable () -> Unit,
    animateTextContent: @Composable (animationService: NovelAnimationService) -> Unit
) {
    val activeAnimations by novelAnimationService.activeAnimations.collectAsState()
    val isCurrentlyAnimatingThisEvent = activeAnimations.any { cmd ->
        cmd is AnimationCommand.AnimateText && cmd.id == eventAnimationId
    }
    if (isCurrentlyAnimatingThisEvent) {
        animateTextContent(novelAnimationService)
    } else {
        staticTextContent()
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
        val environmentImage = imageResource(Res.drawable.bank_environment)

        LaunchedEffect(Unit) {
            visualNovelEngine.loadCharacterAsset(
                name = "banker_main",
                bitmap = mainCharacterImage,
                position = CharacterPosition.LEFT
            )
            visualNovelEngine.loadCharacterAsset(
                name = "banker_secondary",
                bitmap = secondaryCharacterImage,
                position = CharacterPosition.RIGHT
            )
            visualNovelEngine.loadEnvironmentAsset(
                name = "bank",
                bitmap = environmentImage
            )
            visualNovelEngine.handleStoryPassagePlay(PreviewData.passageData)
        }
        VisualNovelScene(scene)
    }
}