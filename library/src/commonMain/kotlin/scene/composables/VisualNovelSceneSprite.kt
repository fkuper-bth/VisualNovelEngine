package scene.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import model.Sprite

@Composable
internal fun VisualNovelSceneSprite(
    sprite: Sprite,
    containerSize: IntSize,
    alignment: Alignment = Alignment.Center
) {
    val positionPx = Offset(
        x = sprite.offsetPercent.x * containerSize.width,
        y = sprite.offsetPercent.y * containerSize.height
    )

    Image(
        bitmap = sprite.bitmap,
        contentDescription = sprite.name,
        alignment = alignment,
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(positionPx.x.toInt(), positionPx.y.toInt()) }
            .graphicsLayer {
                rotationZ = sprite.rotation
                scaleX = sprite.spriteScale.x
                scaleY = sprite.spriteScale.y
                alpha = sprite.opacity
                // align top-left
                transformOrigin = TransformOrigin(0f, 0f)
            },
        contentScale = sprite.contentScale
    )
}