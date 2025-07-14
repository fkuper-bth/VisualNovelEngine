package scene.composables

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import etc.utils.fadingEdge
import data.model.scene.SceneRenderState

@Composable
internal fun VisualNovelSceneMainContent(
    scene: SceneRenderState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(modifier) {
        if (scene.textBoxes.isNotEmpty()) {
            VisualNovelSceneTextBoxes(
                textBoxes = scene.textBoxes,
                modifier = Modifier
                    .fadingEdge(textBoxColumnFadeBrush())
                    .fillMaxWidth()
                    .fillMaxHeight(fraction = 0.5f)
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(2.dp)
            )
            // TODO: improve scroll animation to consider height event views
            LaunchedEffect(scene.textBoxes.size) {
                scrollState.animateScrollTo(scrollState.maxValue)
            }
        } else {
            Spacer(Modifier.weight(1f))
        }

        // FIXME: when a SpriteTransition animation is done, we replace the sprite in the data layer,
        // if the animated sprite was the active character, this animation triggers again...
        // how to fix?
        Crossfade(
            targetState = scene.activeCharacter,
            animationSpec = tween(500),
            modifier = Modifier.fillMaxHeight(0.5f)
        ) { activeCharacter ->
            if (activeCharacter != null) {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val size = IntSize(constraints.maxWidth, constraints.maxHeight)
                    AnimatableVisualNovelSprite(
                        sprite = activeCharacter,
                        containerSize = size,
                        alignment = activeCharacter.alignment
                    )
                }
            } else {
                Spacer(Modifier.fillMaxHeight(0.5f))
            }
        }
    }
}

private fun textBoxColumnFadeBrush(): Brush {
    val colorStops = arrayOf(
        0f to Color.Transparent,
        0.04f to Color.Red, 0.96f to Color.Red,
        1f to Color.Transparent
    )
    return Brush.verticalGradient(colorStops = colorStops)
}