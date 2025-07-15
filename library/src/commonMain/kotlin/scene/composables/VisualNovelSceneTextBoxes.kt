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
import service.NovelAnimationService
import kotlinx.coroutines.delay
import model.assets.Text
import org.koin.compose.koinInject
import kotlin.text.forEach

@Composable
internal fun VisualNovelSceneTextBoxes(
    textBoxes: List<Text>,
    onLinkClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        textBoxes.forEach {
            when (it) {
                is Text.Info -> {
                    AnimatableTextComposable(
                        textAsset = it,
                        textComposable = { displayedText ->
                            InfoTextComposable(displayedText)
                        },
                    )
                }

                is Text.Player -> {
                    AnimatableTextComposable(
                        textAsset = it,
                        textComposable = { displayedText ->
                            PlayerTextComposable(displayedText)
                        },
                    )
                }

                is Text.Character -> {
                    AnimatableTextComposable(
                        textAsset = it,
                        textComposable = { displayedText ->
                            CharacterTextComposable(displayedText)
                        },
                    )
                }

                is Text.Link -> {
                    LinkTextComposable(onClick = onLinkClick, text = it.value)
                }
            }
        }
    }
}

@Composable
private fun AnimatableTextComposable(
    textAsset: Text,
    animationService: NovelAnimationService = koinInject(),
    textComposable: @Composable (displayedText: String) -> Unit,
) {
    val activeAnimations by animationService.activeAnimations.collectAsState()
    val isCurrentlyAnimatingThisEvent = activeAnimations.any { animation ->
        animation.id == textAsset.animationProps.id
    }

    if (isCurrentlyAnimatingThisEvent) {
        var displayedText by remember(textAsset.value, textAsset.animationProps.id) {
            mutableStateOf("")
        }

        LaunchedEffect(textAsset.value, textAsset.animationProps.id) {
            displayedText = ""
            if (textAsset.value.isNotEmpty()) {
                textAsset.value.forEach { char ->
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
        textComposable(textAsset.value)
    }
}