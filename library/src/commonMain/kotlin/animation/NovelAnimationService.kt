package animation

import androidx.compose.ui.Alignment
import kotlinx.coroutines.flow.StateFlow
import kotlin.uuid.Uuid

sealed class AnimationCommandIdentifier(
    val idPrefix: String,
    open val id: Uuid
) {
    data class Text(
        override val id: Uuid
    ) : AnimationCommandIdentifier("AnimateText", id)

    data class SpriteAlpha(
        override val id: Uuid
    ) : AnimationCommandIdentifier("AnimateSpriteAlpha", id)

    data class SpriteCharacterPosition(
        override val id: Uuid
    ) : AnimationCommandIdentifier("AnimateSpriteCharacterPosition", id)

    override fun toString(): String {
        return "${idPrefix}_${id}"
    }
}

/**
 * Represents a single animation unit that the service will manage.
 */
sealed interface AnimationCommand {
    val commandId: AnimationCommandIdentifier

    data class AnimateText(
        val id: Uuid,
        val text: String,
        val animationDelayMillis: Long = 25L,
    ) : AnimationCommand {
        override val commandId = AnimationCommandIdentifier.Text(id)
    }

    data class AnimateSpriteAlpha(
        val id: Uuid,
        val spriteName: String,
        val fromAlpha: Float,
        val toAlpha: Float,
        val durationMillis: Long
    ) : AnimationCommand {
        override val commandId = AnimationCommandIdentifier.SpriteAlpha(id)
    }

    data class AnimateSpriteCharacterPosition(
        val id: Uuid,
        val spriteName: String,
        val fromAlignment: Alignment,
        val toAlignment: Alignment,
        val durationMillis: Long
    ) : AnimationCommand {
        override val commandId = AnimationCommandIdentifier.SpriteCharacterPosition(id)
    }
}

interface NovelAnimationService {
    /**
     * A flow emitting the current list of active animation commands.
     * UI components can observe this flow to render the animations.
     * The list represents animations that are currently expected to be playing or queued.
     */
    val activeAnimations: StateFlow<List<AnimationCommand>>

    /**
     * Submits a list of animation commands to be played.
     * These commands will be made available via the [activeAnimations] flow.
     * The [onAllAnimationsComplete] callback will be invoked once all
     * commands in this specific batch have been reported as completed
     * via [notifyAnimationComplete].
     *
     * @param commands The list of animation commands to play. These should have unique IDs.
     * @param onAllAnimationsComplete A callback to be invoked when all submitted commands are complete.
     * @throws IllegalArgumentException If the list of commands contains duplicate IDs.
     */
    fun playAnimationBatch(vararg commands: AnimationCommand, onAllAnimationsComplete: () -> Unit)

    /**
     * Notifies the service that a specific animation command has completed.
     * UI components rendering animations (e.g., an AnimatedText Composable)
     * should call this method upon completion of their animation.
     *
     * @param commandId The ID of the completed animation command.
     * @throws IllegalArgumentException If the provided command ID is not found in the current batch.
     */
    fun notifyAnimationComplete(commandId: AnimationCommandIdentifier)

    /**
     * Clears all current animations and resets any pending completion callbacks for batches.
     * This is useful for stopping all current animations and starting fresh,
     * for example, when navigating to a new scene or skipping the current dialogue.
     */
    fun clearAllAnimations()
}
