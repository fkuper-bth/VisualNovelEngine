package scene.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.IntSize
import data.AssetStore
import data.model.assets.Sprite
import data.model.assets.resolveProps
import org.koin.compose.koinInject

@Composable
internal fun VisualNovelSceneEnvironment(
    environment: Sprite.Environment,
    containerSize: IntSize,
    assetStore: AssetStore = koinInject()
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .clipToBounds()
    ) {
        // Draw the environment sprite
        AnimatableVisualNovelSprite(
            sprite = environment,
            containerSize = containerSize
        )

        // Draw props on top
        environment.resolveProps(assetStore).forEach { prop ->
            AnimatableVisualNovelSprite(
                sprite = prop,
                containerSize = containerSize
            )
        }
    }
}