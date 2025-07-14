package scene.composables

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import animation.NovelAnimationService
import kotlinx.coroutines.delay
import data.model.assets.Text
import org.koin.compose.koinInject
import kotlin.text.forEach

@Composable
internal fun VisualNovelSceneTextBoxes(
    textBoxes: List<Text>,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        textBoxes.forEach {
            when (it) {
                is Text.Info -> {
                    AnimatableRenderText(
                        textAsset = it,
                        textComposable = { displayedText ->
                            InfoTextView(displayedText)
                        },
                    )
                }

                is Text.Player -> {
                    AnimatableRenderText(
                        textAsset = it,
                        textComposable = { displayedText ->
                            PlayerTextView(displayedText)
                        },
                    )
                }

                is Text.Character -> {
                    AnimatableRenderText(
                        textAsset = it,
                        textComposable = { displayedText ->
                            CharacterTextView(displayedText)
                        },
                    )
                }

                is Text.Link -> {
                    LinkView(onClick = {
                        // TODO: callback to handle playing next passage
                    }, text = it.text)
                }
            }
        }
    }
}

@Composable
private fun AnimatableRenderText(
    textAsset: Text,
    animationService: NovelAnimationService = koinInject(),
    textComposable: @Composable (displayedText: String) -> Unit,
) {
    val activeAnimations by animationService.activeAnimations.collectAsState()
    val isCurrentlyAnimatingThisEvent = activeAnimations.any { animation ->
        animation.id == textAsset.animationProps.id
    }

    if (isCurrentlyAnimatingThisEvent) {
        var displayedText by remember(textAsset.text, textAsset.animationProps.id) {
            mutableStateOf("")
        }

        LaunchedEffect(textAsset.text, textAsset.animationProps.id) {
            displayedText = ""
            if (textAsset.text.isNotEmpty()) {
                textAsset.text.forEach { char ->
                    displayedText += char
                    delay(textAsset.animationProps.durationMillis.toLong())
                }
            }

            // Notify the service that this specific animation command has completed
            // TODO: elevate this service call to a viewmodel and handle exceptions there
            animationService.notifyAnimationComplete(textAsset.animationProps)
        }

        textComposable(displayedText)
    } else {
        textComposable(textAsset.text)
    }
}