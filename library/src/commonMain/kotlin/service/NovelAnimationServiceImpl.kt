package service

import model.assets.Animation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class NovelAnimationServiceImpl() : NovelAnimationService {
    private val _activeAnimations = MutableStateFlow<List<Animation>>(emptyList())
    override val activeAnimations: StateFlow<List<Animation>> = _activeAnimations.asStateFlow()

    private val pendingAnimationsInCurrentBatch = mutableSetOf<Animation>()
    private var currentBatchCompletionCallback: ((playedAnimations: List<Animation>) -> Unit)? = null

    private val successfullyPlayedAnimations: MutableList<Animation> = mutableListOf()

    override fun playAnimationBatch(
        animations: List<Animation>,
        // TODO: timeout mechanism for this callback?
        onAllAnimationsComplete: (playedAnimations: List<Animation>) -> Unit
    ) {
        val distinctIds = animations.map { it.id }.toSet()
        if (distinctIds.size < animations.size) {
            val duplicateIds = animations
                .groupingBy { it.id }
                .eachCount()
                .filter { it.value > 1 }.keys
            val message = "Animation batch contained duplicate IDs. Found duplicates for: $duplicateIds"
            throw IllegalArgumentException(message)
        }

        _activeAnimations.value = animations
        pendingAnimationsInCurrentBatch.clear()
        currentBatchCompletionCallback = null

        if (animations.isNotEmpty()) {
            pendingAnimationsInCurrentBatch.addAll(animations)
            currentBatchCompletionCallback = onAllAnimationsComplete
        } else {
            onAllAnimationsComplete(emptyList())
        }
    }

    override fun notifyAnimationComplete(animation: Animation) {
        val removed = pendingAnimationsInCurrentBatch.remove(animation)
        if (!removed) {
            val message = "Received notification for unknown animation: ${animation.id}"
            throw IllegalArgumentException(message)
        }

        // Add the completed command to the list of successfully played commands
        // to allow the VisualNovelEngine to perform any necessary state updates
        successfullyPlayedAnimations.add(animation)

        // Update the activeAnimations flow to remove the completed command.
        // This ensures the flow reflects commands that are still supposed to be animating.
        _activeAnimations.update { currentAnimations ->
            currentAnimations.filterNot { it.id == animation.id }
        }

        // All commands in the current batch are complete
        if (pendingAnimationsInCurrentBatch.isEmpty()) {
            val callback = currentBatchCompletionCallback
            currentBatchCompletionCallback = null
            callback?.invoke(successfullyPlayedAnimations.toList())
            successfullyPlayedAnimations.clear()
        }
    }

    override fun clearAllAnimations() {
        _activeAnimations.value = emptyList()
        pendingAnimationsInCurrentBatch.clear()
        successfullyPlayedAnimations.clear()
        currentBatchCompletionCallback = null
    }
}
