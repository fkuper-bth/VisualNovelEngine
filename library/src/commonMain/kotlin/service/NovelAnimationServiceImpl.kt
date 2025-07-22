package service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import model.assets.Animation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal class NovelAnimationServiceImpl(
    private val coroutineScope: CoroutineScope
) : NovelAnimationService {
    private val _activeAnimations = MutableStateFlow<List<Animation>>(emptyList())
    override val activeAnimations: StateFlow<List<Animation>> = _activeAnimations.asStateFlow()

    private val pendingAnimationsInCurrentBatch = mutableSetOf<Animation>()
    private var currentBatchCompletionCallback: AnimationBatchCompletionHandler? = null
    private var animationBatchTimeoutJob: Job? = null

    private val successfullyPlayedAnimations: MutableList<Animation> = mutableListOf()

    companion object {
        private const val DEFAULT_ANIMATION_BATCH_TIMEOUT_MILLIS = 10_000
    }

    override fun playAnimationBatch(
        animations: List<Animation>,
        onAllAnimationsComplete: AnimationBatchCompletionHandler
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
            animationBatchTimeoutJob = coroutineScope.launch {
                delay(timeMillis = calculateAnimationTimeoutMillis(animations).toLong())

                if (isActive && pendingAnimationsInCurrentBatch.isNotEmpty()) {
                    println("NovelAnimationService: Animation batch timed out for IDs: " +
                            "${pendingAnimationsInCurrentBatch.map { it.id }}")
                    val callback = currentBatchCompletionCallback
                    val completedAnimations = successfullyPlayedAnimations.toList()
                    clearAllAnimations()
                    callback?.invoke(completedAnimations, true)
                }
            }
        } else {
            onAllAnimationsComplete(emptyList(), false)
        }
    }

    override fun notifyAnimationComplete(animation: Animation) {
        if (!pendingAnimationsInCurrentBatch.contains(animation)) {
            if (currentBatchCompletionCallback == null && animationBatchTimeoutJob == null) {
                println("NovelAnimationService: Received late completion notification for " +
                        "animation: ${animation.id}")
            } else {
                println("NovelAnimationService: Received notification for unknown or " +
                        "already processed animation: ${animation.id}")
            }
            _activeAnimations.update { current -> current.filterNot { it.id == animation.id } }
            return
        }

        // Add the completed command to the list of successfully played commands
        // to allow the VisualNovelEngine to perform any necessary state updates
        successfullyPlayedAnimations.add(animation)
        pendingAnimationsInCurrentBatch.remove(animation)

        // Update the activeAnimations flow to remove the completed command.
        // This ensures the flow reflects commands that are still supposed to be animating.
        _activeAnimations.update { currentAnimations ->
            currentAnimations.filterNot { it.id == animation.id }
        }

        // All animations in the current batch are complete before the timeout
        if (pendingAnimationsInCurrentBatch.isEmpty() && currentBatchCompletionCallback != null) {
            val callback = currentBatchCompletionCallback
            val completedAnimations = successfullyPlayedAnimations.toList()
            cleanupCurrentBatchState(false)
            callback?.invoke(completedAnimations, false)
        }
    }

    override fun clearAllAnimations() {
        cleanupCurrentBatchState()
    }

    private fun cleanupCurrentBatchState(clearActiveAnimationsFlow: Boolean = true) {
        animationBatchTimeoutJob?.cancel()
        animationBatchTimeoutJob = null
        pendingAnimationsInCurrentBatch.clear()
        successfullyPlayedAnimations.clear()
        currentBatchCompletionCallback = null
        if (clearActiveAnimationsFlow) {
            _activeAnimations.value = emptyList()
        }
    }

    private fun calculateAnimationTimeoutMillis(animations: List<Animation>): Int {
        val timeoutDurationMillis = animations
            .maxOfOrNull {
                if (it is Animation.Text) {
                    // Text animation length depends on length of the text,
                    // so we need to calculate the timeout separately here
                    (it.delayMillis + (it.durationMillis * it.content.length)) * 2
                } else {
                    (it.delayMillis + it.durationMillis) * 2
                }
            }
            .let {
                it ?: DEFAULT_ANIMATION_BATCH_TIMEOUT_MILLIS
            }

        return timeoutDurationMillis
    }
}
