package api.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import api.engine.VisualNovelEngine
import api.engine.VisualNovelStoryPlayer
import main.contract.StoryPlaythroughRecord
import model.scene.SceneRenderState
import model.scene.StoryRenderState
import org.koin.compose.KoinIsolatedContext

/**
 * Composable for displaying a visual novel story.
 *
 * @param storyPlayer The story player to use.
 * @param onStoryEnded Callback which is invoked when the story has ended. Passes the [StoryPlaythroughRecord] of the story.
 * @param onPlaybackError Callback which is invoked when an error occurs during playback. Passes the error message.
 * @param aspectRatio The aspect ratio of the scene.
 * @param modifier The modifier to apply to the story.
 *
 * @throws IllegalStateException If a [VisualNovelEngine] has not been initialized using [VisualNovelEngine.init].
 */
@Composable
fun VisualNovelStory(
    storyPlayer: VisualNovelStoryPlayer,
    onStoryEnded: (StoryPlaythroughRecord) -> Unit = {},
    onPlaybackError: (String) -> Unit = {},
    aspectRatio: Float = 9f / 16f,
    modifier: Modifier = Modifier
) {
    if (VisualNovelEngine.koinApp == null) {
        throw IllegalStateException(
            "Engine must be initialized using VisualNovelEngine.init"
        )
    }

    KoinIsolatedContext(context = VisualNovelEngine.koinApp!!) {
        val uiState by storyPlayer.uiState.collectAsState()
        val isBusy by storyPlayer.isBusy.collectAsState()

        val sceneToRender = remember { mutableStateOf<SceneRenderState?>(null) }
        val errorMessage = remember { mutableStateOf<String?>(null) }
        val loading = remember { mutableStateOf(false) }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentWidth(align = Alignment.CenterHorizontally)
                .aspectRatio(aspectRatio)
        ) {
            when (uiState) {
                StoryRenderState.Initializing,
                StoryRenderState.Loading -> {
                    loading.value = true
                }

                is StoryRenderState.Ended -> {
                    loading.value = false
                    onStoryEnded((uiState as StoryRenderState.Ended).playthroughRecord)
                }

                is StoryRenderState.Rendering -> {
                    loading.value = false
                    sceneToRender.value = (uiState as StoryRenderState.Rendering).scene
                }

                is StoryRenderState.Error -> {
                    loading.value = false
                    errorMessage.value = (uiState as StoryRenderState.Error).message
                }
            }
            sceneToRender.value?.let { sceneToRender ->
                val sceneModifier = if (loading.value) {
                    Modifier.fillMaxSize().blur(radius = 8.dp)
                } else {
                    Modifier.fillMaxSize()
                }
                Box(modifier = sceneModifier) {
                    VisualNovelScene(
                        scene = sceneToRender,
                        aspectRatio = aspectRatio,
                        onLinkClick = {
                            if (isBusy) {
                                return@VisualNovelScene
                            }
                            storyPlayer.chooseStoryPassage(it)
                        }
                    )
                }
            }
            errorMessage.value?.let { message ->
                ErrorMessageDialog(
                    onDismissRequest = {
                        // TODO: possibly perform some kind of error recovery via the engine here
                        // for example reset playback state to beginning or to before link was clicked
                        onPlaybackError(message)
                        errorMessage.value = null
                    },
                    message = message
                )
            }
            if (loading.value) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxSize(0.25f)
                            .padding(8.dp),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorMessageDialog(
    onDismissRequest: () -> Unit,
    message: String
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text(
                text = message,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.Center),
                textAlign = TextAlign.Center,
            )
        }
    }
}