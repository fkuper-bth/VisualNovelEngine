package api.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import api.engine.VisualNovelEngine
import data.model.StoryPassageNovelEvent
import model.scene.SceneRenderState
import org.koin.compose.KoinIsolatedContext
import scene.composables.VisualNovelSceneEnvironment
import scene.composables.VisualNovelSceneMainContent

/**
 * Composable for displaying a visual novel scene.
 *
 * @param scene The scene to display.
 * @param onLinkClick The callback to invoke when a link is clicked.
 * @param aspectRatio The aspect ratio of the scene.
 * @param modifier The modifier to apply to the scene.
 *
 * @throws IllegalStateException If a [VisualNovelEngine] has not been initialized using [VisualNovelEngine.init].
 */
@Composable
fun VisualNovelScene(
    scene: SceneRenderState,
    onLinkClick: (StoryPassageNovelEvent.Link) -> Unit,
    aspectRatio: Float = 9f / 16f,
    modifier: Modifier = Modifier
) {
    if (VisualNovelEngine.koinApp == null) {
        throw IllegalStateException("Engine must be initialized using ")
    }

    KoinIsolatedContext(context = VisualNovelEngine.koinApp!!) {
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
}