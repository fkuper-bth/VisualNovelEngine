package api.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import api.engine.VisualNovelEngine
import api.engine.VisualNovelStoryPlayer
import org.koin.compose.KoinIsolatedContext
import model.scene.StoryRenderState

/**
 * Composable for displaying a visual novel story.
 *
 * @param storyPlayer The story player to use.
 * @param aspectRatio The aspect ratio of the scene.
 * @param modifier The modifier to apply to the story.
 *
 * @throws IllegalStateException If a [VisualNovelEngine] has not been initialized using [VisualNovelEngine.init].
 */
@Composable
fun VisualNovelStory(
    storyPlayer: VisualNovelStoryPlayer,
    aspectRatio: Float = 9f / 16f,
    modifier: Modifier = Modifier
) {
    if (VisualNovelEngine.koinApp == null) {
        throw IllegalStateException(
            "Engine must be initialized using VisualNovelEngine.init"
        )
    }

    val uiState by storyPlayer.uiState.collectAsState()
    val storyPlayerIsBusy by storyPlayer.isBusy.collectAsState()

    KoinIsolatedContext(context = VisualNovelEngine.koinApp!!) {
        Box(modifier = modifier
            .fillMaxWidth()
            .wrapContentWidth(align = Alignment.CenterHorizontally)
            .aspectRatio(aspectRatio)
        ) {
            when (uiState) {
                StoryRenderState.Initializing,
                StoryRenderState.Loading -> {
                    Text(
                        text = "Loading...",
                        modifier = Modifier.padding(12.dp)
                    )
                }

                is StoryRenderState.Rendering -> {
                    val scene = (uiState as StoryRenderState.Rendering).scene

                    VisualNovelScene(
                        scene = scene,
                        aspectRatio = aspectRatio,
                        onLinkClick = {
                            if (storyPlayerIsBusy) {
                                return@VisualNovelScene
                            }
                            storyPlayer.chooseStoryPassage(it)
                        }
                    )
                }

                is StoryRenderState.Error -> {
                    val errorMessage = (uiState as StoryRenderState.Error).message

                    Text(
                        text = "Error: $errorMessage",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}