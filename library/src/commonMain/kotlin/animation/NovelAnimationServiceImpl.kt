package animation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class NovelAnimationServiceImpl : NovelAnimationService {
    private val _activeAnimations = MutableStateFlow<List<AnimationCommand>>(emptyList())
    override val activeAnimations: StateFlow<List<AnimationCommand>> = _activeAnimations.asStateFlow()

    private val pendingCommandsInCurrentBatch = mutableSetOf<AnimationCommandIdentifier>()
    private var currentBatchCompletionCallback: (() -> Unit)? = null

    override fun playAnimationBatch(
        vararg commands: AnimationCommand,
        onAllAnimationsComplete: () -> Unit
    ) {
        // Check if command batch contains duplicate IDs
        val distinctIds = commands.map(AnimationCommand::commandId).toSet()
        if (distinctIds.size < commands.size) {
            val duplicateIds = commands
                .groupingBy(AnimationCommand::commandId)
                .eachCount()
                .filter { it.value > 1 }.keys
            val message = "Animation batch cannot contain duplicate command IDs. Found duplicates for IDs: $duplicateIds"
            throw IllegalArgumentException(message)
        }

        // This implementation assumes one batch is processed at a time for completion tracking.
        // If a new batch is played, it replaces the current one.
        _activeAnimations.value = commands.toList()

        pendingCommandsInCurrentBatch.clear()
        currentBatchCompletionCallback = null // Clear previous callback

        if (commands.isNotEmpty()) {
            pendingCommandsInCurrentBatch.addAll(commands.map(AnimationCommand::commandId))
            currentBatchCompletionCallback = onAllAnimationsComplete
        } else {
            // If the batch is empty, call completion immediately.
            onAllAnimationsComplete()
        }
    }

    override fun notifyAnimationComplete(commandId: AnimationCommandIdentifier) {
        val removed = pendingCommandsInCurrentBatch.remove(commandId)
        if (!removed) {
            throw IllegalArgumentException("Received notification for unknown command ID: $commandId")
        }

        // Update the activeAnimations flow to remove the completed command.
        // This ensures the flow reflects commands that are still supposed to be animating.
        _activeAnimations.update { currentAnimations ->
            currentAnimations.filterNot { it.commandId == commandId }
        }

        if (pendingCommandsInCurrentBatch.isEmpty()) {
            // All commands in the current batch are complete
            val callback = currentBatchCompletionCallback
            currentBatchCompletionCallback = null // Reset for the next batch
            callback?.invoke()
        }
    }

    override fun clearAllAnimations() {
        _activeAnimations.value = emptyList()
        pendingCommandsInCurrentBatch.clear()
        // Resetting pending completion callback as per interface description, without invoking it.
        currentBatchCompletionCallback = null
    }
}
