package scene.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.IntSize
import model.Sprite

@Composable
internal fun VisualNovelSceneEnvironment(
    environment: Sprite.Environment,
    containerSize: IntSize
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .clipToBounds()
    ) {
        // Draw the environment sprite
        VisualNovelSceneSprite(
            sprite = environment,
            containerSize = containerSize
        )

        // Draw props on top
        environment.props.forEach { prop ->
            VisualNovelSceneSprite(
                sprite = prop,
                containerSize = containerSize
            )
        }
    }
}