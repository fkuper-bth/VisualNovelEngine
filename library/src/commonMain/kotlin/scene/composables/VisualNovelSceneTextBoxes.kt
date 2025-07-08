package scene.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import animation.AnimationCommand
import animation.NovelAnimationService
import model.RenderText
import org.koin.compose.koinInject
import kotlin.uuid.Uuid

@Composable
internal fun VisualNovelSceneTextBoxes(
    textBoxes: List<RenderText>,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        textBoxes.forEach {
            when (it) {
                is RenderText.Info -> {
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

                is RenderText.Player -> {
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

                is RenderText.Character -> {
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

                is RenderText.Link -> {
                    LinkView(onClick = {
                        // TODO: callback to handle playing next passage
                    }, text = it.text)
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