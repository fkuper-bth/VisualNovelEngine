package service

import kotlinx.coroutines.flow.StateFlow
import model.assets.Animation

internal interface NovelAnimationService {
    /**
     * A flow emitting the current list of active animations.
     * UI components can observe this flow to render the animations.
     * The list represents animations that are currently expected to be playing or queued.
     */
    val activeAnimations: StateFlow<List<Animation>>

    /**
     * Submits a list of animations to be played.
     * These animations will be made available via the [activeAnimations] flow.
     * The [onAllAnimationsComplete] callback will be invoked once all
     * animations in this batch have been reported as completed via [notifyAnimationComplete].
     *
     * @param animations Animations to play. These should have unique IDs.
     * @param onAllAnimationsComplete A callback to be invoked when all submitted commands are
     * complete. Includes a list of completed animations which can be used to perform any necessary
     * state updates.
     * @throws IllegalArgumentException If the provided batch contains duplicate IDs.
     */
    fun playAnimationBatch(
        animations: List<Animation>,
        onAllAnimationsComplete: (playedAnimations: List<Animation>) -> Unit
    )

    /**
     * Notifies the service that a specific animation has completed.
     * UI components rendering animations (e.g., an AnimatedText Composable)
     * should call this method upon completion of their animation.
     *
     * @param animation The completed animation.
     * @throws IllegalArgumentException If the provided animation is not found in the current batch.
     */
    fun notifyAnimationComplete(animation: Animation)

    /**
     * Clears all current animations and resets any pending completion callbacks for batches.
     * This is useful for stopping all current animations and starting fresh,
     * for example, when navigating to a new scene or skipping the current dialogue.
     */
    fun clearAllAnimations()
}
