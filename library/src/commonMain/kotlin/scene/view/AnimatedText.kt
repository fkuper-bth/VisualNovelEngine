package scene.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import animation.AnimationCommandIdentifier
import animation.NovelAnimationService
import kotlin.uuid.Uuid

@Composable
internal fun AnimatedText(
    text: String,
    textView: @Composable (displayedText: String) -> Unit,
    eventIdentifier: Uuid,
    animationService: NovelAnimationService,
    animationDelayMillis: Long = 25L
) {
    val commandId = AnimationCommandIdentifier.Text(eventIdentifier)
    // TODO: do these key params make sense?
    var displayedText by remember(text, commandId.toString()) { mutableStateOf("") }

    LaunchedEffect(key1 = text, key2 = commandId.toString()) {
        displayedText = ""
        if (text.isNotEmpty()) {
            text.forEach { char ->
                displayedText += char
                delay(animationDelayMillis)
            }
        }
        // Notify the service that this specific animation command has completed
        // TODO: elevate this service call to a viewmodel and handle exceptions there
        animationService.notifyAnimationComplete(commandId)
    }

    textView(displayedText)
}
