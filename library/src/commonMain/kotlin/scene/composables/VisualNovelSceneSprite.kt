package scene.composables

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateOffset
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import animation.NovelAnimationService
import data.AssetStore
import data.model.assets.Animation
import data.model.assets.Sprite
import data.model.assets.resolveAnimations
import data.model.assets.resolveToSprite
import org.koin.compose.koinInject

@Composable
internal fun AnimatableVisualNovelSprite(
    sprite: Sprite,
    containerSize: IntSize,
    alignment: Alignment = Alignment.Center,
    novelAnimationService: NovelAnimationService = koinInject(),
    assetStore: AssetStore = koinInject(),
    modifier: Modifier = Modifier.fillMaxSize()
) {
    // Check if there is any active animations for this sprite
    val activeAnimations by novelAnimationService.activeAnimations.collectAsState()
    val spriteAnimations = sprite.resolveAnimations(assetStore)

    // Check for active transition animation and create animated properties
    val activeSpriteTransition = remember(sprite, assetStore, activeAnimations) {
        spriteAnimations.filterIsInstance<Animation.SpriteTransition>()
            .firstOrNull { animation -> activeAnimations.any { it.id == animation.id } }
    }
    val animatedSpriteProperties = rememberAnimatedSpriteProperties(
        sprite = sprite,
        activeAnimation = activeSpriteTransition,
        containerSize = containerSize
    )

    SpriteImage(
        sprite = sprite,
        offset = animatedSpriteProperties.offset,
        alpha = animatedSpriteProperties.alpha,
        rotation = animatedSpriteProperties.rotation,
        scaleX = animatedSpriteProperties.scaleX,
        scaleY = animatedSpriteProperties.scaleY,
        alignment = alignment,
        modifier = modifier
    )
}

@Composable
private fun SpriteImage(
    sprite: Sprite,
    offset: Offset,
    alpha: Float = sprite.opacity,
    rotation: Float = sprite.rotation,
    scaleX: Float = sprite.spriteScale.x,
    scaleY: Float = sprite.spriteScale.y,
    alignment: Alignment,
    modifier: Modifier
) {
    Image(
        bitmap = sprite.bitmap,
        contentDescription = sprite.id,
        contentScale = sprite.contentScale,
        alignment = alignment,
        modifier = modifier
            .offset { IntOffset(offset.x.toInt(), offset.y.toInt()) }
            .graphicsLayer {
                this.alpha = alpha
                this.rotationZ = rotation
                this.scaleX = scaleX
                this.scaleY = scaleY
                transformOrigin = TransformOrigin(0f, 0f)
            }
    )
}

data class AnimatedSpriteProperties(
    val offset: Offset,
    val alpha: Float,
    val rotation: Float,
    val scaleX: Float,
    val scaleY: Float
)

@Composable
private fun rememberAnimatedSpriteProperties(
    sprite: Sprite,
    activeAnimation: Animation.SpriteTransition?,
    containerSize: IntSize,
    novelAnimationService: NovelAnimationService = koinInject(),
    assetStore: AssetStore = koinInject(),
): AnimatedSpriteProperties {
    val transitionState = remember { MutableTransitionState<Sprite?>(null) }
    activeAnimation?.let {
        transitionState.targetState = it.resolveToSprite(assetStore)
    }
    val transition = rememberTransition(transitionState, label = "spriteTransition")
    val offset by transition.animateOffset(
        transitionSpec = {
            tween(
                durationMillis = activeAnimation?.durationMillis ?: 0,
                delayMillis = activeAnimation?.delayMillis ?: 0
            )
        },
        label = "offset"
    ) { calculateOffset(it?.offsetPercent ?: sprite.offsetPercent, containerSize) }
    val alpha by transition.animateFloat(
        transitionSpec = {
            tween(
                durationMillis = activeAnimation?.durationMillis ?: 0,
                delayMillis = activeAnimation?.delayMillis ?: 0
            )
        },
        label = "alpha"
    ) { it?.opacity ?: sprite.opacity }
    val rotation by transition.animateFloat(
        transitionSpec = {
            tween(
                durationMillis = activeAnimation?.durationMillis ?: 0,
                delayMillis = activeAnimation?.delayMillis ?: 0
            )
        },
        label = "rotation"
    ) { it?.rotation ?: sprite.rotation }
    val scaleX by transition.animateFloat(
        transitionSpec = {
            tween(
                durationMillis = activeAnimation?.durationMillis ?: 0,
                delayMillis = activeAnimation?.delayMillis ?: 0
            )
        },
        label = "scaleX"
    ) { it?.spriteScale?.x ?: sprite.spriteScale.x }
    val scaleY by transition.animateFloat(
        transitionSpec = {
            tween(
                durationMillis = activeAnimation?.durationMillis ?: 0,
                delayMillis = activeAnimation?.delayMillis ?: 0
            )
        },
        label = "scaleY"
    ) { it?.spriteScale?.y ?: sprite.spriteScale.y }

    LaunchedEffect(transition.currentState, transition.targetState) {
        if (transition.currentState == transition.targetState) {
            if (activeAnimation != null) {
                novelAnimationService.notifyAnimationComplete(activeAnimation)
            }
        }
    }

    return AnimatedSpriteProperties(offset, alpha, rotation, scaleX, scaleY)
}

private fun calculateOffset(spriteOffset: Sprite.OffsetPercent, containerSize: IntSize): Offset =
    Offset(
        x = spriteOffset.x * containerSize.width,
        y = spriteOffset.y * containerSize.height
    )