package scene

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import animation.AnimationCommand
import animation.NovelAnimationService
import api.VisualNovelEngine
import etc.di.sharedModule
import etc.utils.PreviewData
import fk.visualnovel.engine.library.generated.resources.Res
import fk.visualnovel.engine.library.generated.resources.bank_environment
import fk.visualnovel.engine.library.generated.resources.bank_full_character
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .verticalScroll(scrollState)
                        .fillMaxWidth()
                        .padding(16.dp)
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
            }

            // Spacer to push characters to the bottom if there's a text box
            // Or just to act as the main content area between text and characters
            Spacer(modifier = Modifier.weight(1f))

            if (scene.characters.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.5f)
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Filter for characters that should be visible
                    val visibleCharacters = scene.characters.filter { it.opacity > 0f }

                    visibleCharacters.forEach { character ->
                        val alignmentModifier = when (character.position) {
                            CharacterPosition.LEFT -> Modifier.weight(1f).align(Alignment.Bottom)
                            CharacterPosition.CENTER -> Modifier.weight(1f).align(Alignment.Bottom)
                            CharacterPosition.RIGHT -> Modifier.weight(1f).align(Alignment.Bottom)
                        }

                        Image(
                            bitmap = character.bitmap,
                            contentDescription = character.name,
                            modifier = Modifier
                                .fillMaxHeight()
                                .then(if (visibleCharacters.size > 1) alignmentModifier else Modifier.align(Alignment.Bottom))
                                .graphicsLayer(alpha = character.opacity)
                                .padding(horizontal = 4.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            } else {
                // If no characters, ensure the layout still respects the bottom portion
                Spacer(Modifier.fillMaxHeight(0.5f))
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

        val characterImage = imageResource(Res.drawable.bank_full_character)
        val environmentImage = imageResource(Res.drawable.bank_environment)

        LaunchedEffect(Unit) {
            visualNovelEngine.loadCharacterAsset(
                name = "banker_left",
                bitmap = characterImage,
                position = CharacterPosition.LEFT
            )
            visualNovelEngine.loadCharacterAsset(
                name = "banker_right",
                bitmap = characterImage,
                position = CharacterPosition.RIGHT
            )
            visualNovelEngine.loadCharacterAsset(
                name = "banker_center",
                bitmap = characterImage,
                position = CharacterPosition.CENTER
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