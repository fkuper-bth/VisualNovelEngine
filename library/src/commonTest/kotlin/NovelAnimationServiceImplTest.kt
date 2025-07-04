import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import animation.AnimationCommand
import animation.AnimationCommandIdentifier
import animation.NovelAnimationServiceImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.uuid.Uuid

class NovelAnimationServiceImplTest {
    private val underTest = NovelAnimationServiceImpl()

    @Test
    fun `getActiveAnimations initial state`() {
        // Verify that activeAnimations initially emits an empty list.

        // Act
        val activeAnimations = underTest.activeAnimations.value

        // Assert
        assertTrue(activeAnimations.isEmpty())
    }

    @Test
    fun `playAnimationBatch with empty command list`() {
        // Verify that playing an empty batch immediately invokes onAllAnimationsComplete and
        // activeAnimations remains empty.

        // Arrange
        var onAllAnimationsCompleteCalled = false

        // Act
        underTest.playAnimationBatch(onAllAnimationsComplete = {
            onAllAnimationsCompleteCalled = true
        })
        val activeAnimations = underTest.activeAnimations.value

        // Assert
        assertTrue(onAllAnimationsCompleteCalled)
        assertTrue(activeAnimations.isEmpty())
    }

    @Test
    fun `playAnimationBatch with non empty command list`() {
        // Verify that playing a non-empty batch updates activeAnimations with the commands and
        // registers onAllAnimationsComplete.

        // Arrange
        val commands = listOf(
            AnimationCommand.AnimateText(Uuid.random(), "First Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Second Text To Animate")
        ).toTypedArray()
        var onAllAnimationsCompleteCalled = false

        // Act & Assert

        // Before notifying about animation completion, they should be active
        underTest.playAnimationBatch(*commands, onAllAnimationsComplete = {
            onAllAnimationsCompleteCalled = true
        })
        var activeAnimations = underTest.activeAnimations.value
        assertTrue(activeAnimations.isNotEmpty())
        assertEquals(2, activeAnimations.size)
        assertFalse(onAllAnimationsCompleteCalled)

        // After notifying service about the animations completing, they should be inactive
        commands.forEach { underTest.notifyAnimationComplete(it.commandId) }
        activeAnimations = underTest.activeAnimations.value
        assertTrue(activeAnimations.isEmpty())
        assertTrue(onAllAnimationsCompleteCalled)
    }

    @Test
    fun `playAnimationBatch replaces existing batch`() {
        // Verify that calling playAnimationBatch a second time replaces the commands and completion
        // callback from the first batch.

        // Arrange
        val firstBatch = listOf(
            AnimationCommand.AnimateText(Uuid.random(), "First Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Second Text To Animate")
        ).toTypedArray()
        val secondBatch = listOf(
            AnimationCommand.AnimateText(Uuid.random(), "Third Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Fourth Text To Animate")
        ).toTypedArray()
        var onFirstBatchCompletedCalled = false
        var onSecondBatchCompletedCalled = false

        // Act & Assert

        // Queue first batch, check that it's active
        underTest.playAnimationBatch(*firstBatch, onAllAnimationsComplete = {
          onFirstBatchCompletedCalled = true
        })
        assertEquals(firstBatch.toList(), underTest.activeAnimations.value)

        // Queue second batch before completing the first one
        underTest.playAnimationBatch(*secondBatch, onAllAnimationsComplete = {
            onSecondBatchCompletedCalled = true
        })
        assertEquals(secondBatch.toList(), underTest.activeAnimations.value)
        secondBatch.forEach { underTest.notifyAnimationComplete(it.commandId) }
        assertTrue(onSecondBatchCompletedCalled)
        assertFalse(onFirstBatchCompletedCalled)
        assertTrue(underTest.activeAnimations.value.isEmpty())
    }

    @Test
    fun `playAnimationBatch with commands having duplicate IDs`() {
        // Verify how the system handles a batch with commands that have the same commandId.

        // Arrange
        val duplicateId = Uuid.random()
        val commands = listOf(
            AnimationCommand.AnimateText(duplicateId, "First Text To Animate"),
            AnimationCommand.AnimateText(duplicateId, "Second Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Third Text To Animate")
        ).toTypedArray()

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            underTest.playAnimationBatch(*commands, onAllAnimationsComplete = {})
        }
    }

    @Test
    fun `notifyAnimationComplete for a valid command`() {
        // Verify that notifying completion of a command removes it from activeAnimations.

        // Arrange
        val commands = listOf(
            AnimationCommand.AnimateText(Uuid.random(), "First Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Second Text To Animate")
        ).toTypedArray()
        var onAllAnimationsCompleteCalled = false

        // Act & Assert

        // Before notifying about animation completion, they should all be active
        underTest.playAnimationBatch(*commands, onAllAnimationsComplete = {
            onAllAnimationsCompleteCalled = true
        })
        assertEquals(commands.toList(), underTest.activeAnimations.value)

        // Notify completion of the first command
        underTest.notifyAnimationComplete(commands.first().commandId)
        assertEquals(listOf(commands.last()), underTest.activeAnimations.value)
        assertFalse(onAllAnimationsCompleteCalled)
    }

    @Test
    fun `notifyAnimationComplete for a command not in the current batch`() {
        // Verify that notifying completion of a commandId not in pendingCommandsInCurrentBatch does
        // not invoke the completion callback and handles gracefully.

        // Arrange
        val command = AnimationCommand.AnimateText(Uuid.random(), "First Text To Animate")
        val invalidCommandId = AnimationCommandIdentifier.Text(Uuid.random())
        var onAllAnimationsCompleteCalled = false
        underTest.playAnimationBatch(command, onAllAnimationsComplete = {
            onAllAnimationsCompleteCalled = true
        })

        // Act & Assert

        // notifyAnimationComplete should fail with an exception
        assertFailsWith<IllegalArgumentException> {
            underTest.notifyAnimationComplete(invalidCommandId)
        }

        // activeAnimations state should remain unchanged
        assertEquals(command, underTest.activeAnimations.value.first())
        assertFalse(onAllAnimationsCompleteCalled)
    }

    @Test
    fun `notifyAnimationComplete for a command already completed`() {
        // Verify that calling notifyAnimationComplete multiple times for the same commandId behaves
        // correctly (e.g., doesn't invoke callback multiple times).

        // Arrange
        val command = AnimationCommand.AnimateText(Uuid.random(), "First Text To Animate")
        var onAllAnimationsCompleteCalled = false
        underTest.playAnimationBatch(command, onAllAnimationsComplete = {
            onAllAnimationsCompleteCalled = true
        })

        // Act & Assert

        // Notify completion of the first command, should complete the batch
        underTest.notifyAnimationComplete(command.commandId)
        assertTrue(onAllAnimationsCompleteCalled)

        // Notify with the same commandId again, should fail with an exception
        // and callback should not be invoked again
        onAllAnimationsCompleteCalled = false
        assertFailsWith<IllegalArgumentException> {
            underTest.notifyAnimationComplete(command.commandId)
        }
        assertFalse(onAllAnimationsCompleteCalled)
    }

    @Test
    fun `notifyAnimationComplete when no batch is active`() {
        // Verify that calling notifyAnimationComplete when currentBatchCompletionCallback is null
        // (no active batch) handles gracefully.

        // Arrange
        val commandId = AnimationCommandIdentifier.Text(Uuid.random())
        assertTrue(underTest.activeAnimations.value.isEmpty())

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            underTest.notifyAnimationComplete(commandId)
        }
    }

    @Test
    fun `notifyAnimationComplete sequence and activeAnimations updates`() {
        // Play a batch with multiple commands. Notify completion for them one by one and verify
        // activeAnimations flow emits the correct list of remaining animations after each
        // notification.

        // Arrange
        val commands = listOf(
            AnimationCommand.AnimateText(Uuid.random(), "First Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Second Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Third Text To Animate")
        ).toTypedArray()
        var onAllAnimationsCompleteCalled = false
        underTest.playAnimationBatch(*commands, onAllAnimationsComplete = {
            onAllAnimationsCompleteCalled = true
        })

        // Act & Assert

        // Before notifying about animation completion, they should all be active
        assertFalse(onAllAnimationsCompleteCalled)
        assertEquals(commands.toList(), underTest.activeAnimations.value)

        // After each notify call, the active animations list should reflect the remaining commands
        commands.forEachIndexed { index, command ->
            underTest.notifyAnimationComplete(command.commandId)
            val remainingActiveAnimation = commands.toList().subList(index + 1, commands.size)
            assertEquals(remainingActiveAnimation, underTest.activeAnimations.value)
        }

        // After notifying about all animations completing, the callback should be invoked
        assertTrue(onAllAnimationsCompleteCalled)
    }

    @Test
    fun `clearAllAnimations when animations are active`() {
        // Verify that clearAllAnimations clears activeAnimations,
        // and currentBatchCompletionCallback without invoking the callback.

        // Arrange
        val commands = listOf(
            AnimationCommand.AnimateText(Uuid.random(), "First Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Second Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Third Text To Animate")
        ).toTypedArray()
        var onAllAnimationsCompleteCalled = false
        underTest.playAnimationBatch(*commands, onAllAnimationsComplete = {
            onAllAnimationsCompleteCalled = true
        })

        // Act & Assert

        // Before clearing all animations, they should all be active
        assertEquals(commands.toList(), underTest.activeAnimations.value)

        // Clear animations
        underTest.clearAllAnimations()
        assertTrue(underTest.activeAnimations.value.isEmpty())
        assertFalse(onAllAnimationsCompleteCalled)
    }

    @Test
    fun `clearAllAnimations when no animations are active`() {
        // Verify that clearAllAnimations behaves correctly when there are no active animations or
        // pending commands.

        // Arrange
        assertTrue(underTest.activeAnimations.value.isEmpty())

        // Act
        underTest.clearAllAnimations()

        // Assert
        assertTrue(underTest.activeAnimations.value.isEmpty())
    }

    @Test
    fun `clearAllAnimations after a batch is played but before completion`() {
        // Play a batch, then call clearAllAnimations. Verify that the completion callback is not
        // invoked and state is reset.

        // Arrange
        val commands = listOf(
            AnimationCommand.AnimateText(Uuid.random(), "First Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Second Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Third Text To Animate")
        ).toTypedArray()
        var onAllAnimationsCompleteCalled = false
        underTest.playAnimationBatch(*commands, onAllAnimationsComplete = {
            onAllAnimationsCompleteCalled = true
        })

        // Act & Assert
        underTest.notifyAnimationComplete(commands.first().commandId)
        assertEquals(2, underTest.activeAnimations.value.size)

        underTest.clearAllAnimations()
        assertTrue(underTest.activeAnimations.value.isEmpty())
        assertFalse(onAllAnimationsCompleteCalled)
    }

    @Test
    fun `Interaction  playAnimationBatch  notifyAnimationComplete  playAnimationBatch again`() {
        // Play a batch, notify some completions, then play a new batch. Verify the state
        // (activeAnimations, pending, callback) is correctly set for the new batch.

        // Arrange
        val firstBatch = listOf(
            AnimationCommand.AnimateText(Uuid.random(), "First Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Second Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Third Text To Animate")
        ).toTypedArray()
        val secondBatch = listOf(
            AnimationCommand.AnimateText(Uuid.random(), "Fourth Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Fifth Text To Animate")
        ).toTypedArray()
        var onAllAnimationsCompleteCalled = false
        underTest.playAnimationBatch(*firstBatch, onAllAnimationsComplete = {
            onAllAnimationsCompleteCalled = true
        })

        // Act & Assert
        underTest.notifyAnimationComplete(firstBatch.first().commandId)
        assertEquals(2, underTest.activeAnimations.value.size)
        assertFalse(onAllAnimationsCompleteCalled)

        underTest.playAnimationBatch(*secondBatch, onAllAnimationsComplete = {
            onAllAnimationsCompleteCalled = true
        })
        assertEquals(secondBatch.toList(), underTest.activeAnimations.value)
        assertFalse(onAllAnimationsCompleteCalled)

        secondBatch.forEach { underTest.notifyAnimationComplete(it.commandId) }
        assertTrue(onAllAnimationsCompleteCalled)
    }

    @Test
    fun `Interaction  playAnimationBatch  clearAllAnimations  playAnimationBatch again`() {
        // Play a batch, clear all, then play another batch. Verify the state is correctly set for
        // the new batch and the cleared batch's callback is not invoked.

        val firstBatch = listOf(
            AnimationCommand.AnimateText(Uuid.random(), "First Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Second Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Third Text To Animate")
        ).toTypedArray()
        val secondBatch = listOf(
            AnimationCommand.AnimateText(Uuid.random(), "Fourth Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Fifth Text To Animate")
        ).toTypedArray()
        var onAllAnimationsCompleteCalled = false

        // Act & Assert
        underTest.playAnimationBatch(*firstBatch, onAllAnimationsComplete = {
            onAllAnimationsCompleteCalled = true
        })
        assertEquals(firstBatch.toList(), underTest.activeAnimations.value)
        assertFalse(onAllAnimationsCompleteCalled)

        underTest.clearAllAnimations()
        assertTrue(underTest.activeAnimations.value.isEmpty())
        assertFalse(onAllAnimationsCompleteCalled)

        underTest.playAnimationBatch(*secondBatch, onAllAnimationsComplete = {
            onAllAnimationsCompleteCalled = true
        })
        assertEquals(secondBatch.toList(), underTest.activeAnimations.value)
        assertFalse(onAllAnimationsCompleteCalled)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `activeAnimations flow emission on playAnimationBatch`() = runTest {
        // Verify that collectors of the activeAnimations flow receive an update when
        // playAnimationBatch is called with new commands.

        // Arrange
        // set up collecting state flow updates
        val collectedUpdates = mutableListOf<List<AnimationCommand>>()
        val collectJob = launch {
            underTest.activeAnimations.collect(collectedUpdates::add)
        }
        advanceUntilIdle()
        assertEquals(1, collectedUpdates.size)
        assertTrue(collectedUpdates.first().isEmpty())

        // set up command batch
        val commands = listOf(
            AnimationCommand.AnimateText(Uuid.random(), "First Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Second Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Third Text To Animate")
        ).toTypedArray()

        // Act & Assert
        underTest.playAnimationBatch(*commands, onAllAnimationsComplete = {})
        advanceUntilIdle()
        assertEquals(2, collectedUpdates.size)
        assertEquals(commands.toList(), collectedUpdates.last())

        // Clean up: cancel collection job
        collectJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `activeAnimations flow emission on notifyAnimationComplete`() = runTest {
        // Verify that collectors of the activeAnimations flow receive an update when
        // notifyAnimationComplete removes a command.

        // Arrange
        // set up collecting state flow updates
        val collectedUpdates = mutableListOf<List<AnimationCommand>>()
        val collectJob = launch {
            underTest.activeAnimations.collect(collectedUpdates::add)
        }
        advanceUntilIdle()
        assertEquals(1, collectedUpdates.size)
        assertTrue(collectedUpdates.last().isEmpty())

        // set up command batch
        val commands = listOf(
            AnimationCommand.AnimateText(Uuid.random(), "First Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Second Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Third Text To Animate")
        ).toTypedArray()
        var onAllAnimationsCompleteCalled = false

        // Act & Assert
        // After initial play, the activeAnimations should be updated
        underTest.playAnimationBatch(*commands, onAllAnimationsComplete = {
            onAllAnimationsCompleteCalled = true
        })
        advanceUntilIdle()
        assertEquals(2, collectedUpdates.size)
        assertEquals(commands.toList(), collectedUpdates.last())

        // After each notify call, we should get a list of remaining commands
        commands.forEachIndexed { index, command ->
            underTest.notifyAnimationComplete(command.commandId)
            advanceUntilIdle()
            val remainingCommands = commands.toList().subList(index + 1, commands.size)
            assertEquals(remainingCommands, collectedUpdates.last())
        }

        // After all animations completing, the callback should have been invoked
        assertTrue(onAllAnimationsCompleteCalled)

        // Clean up: cancel collection job
        collectJob.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `activeAnimations flow emission on clearAllAnimations`() = runTest {
        // Verify that collectors of the activeAnimations flow receive an empty list when
        // clearAllAnimations is called.

        // Arrange
        // set up collecting state flow updates
        val collectedUpdates = mutableListOf<List<AnimationCommand>>()
        val collectJob = launch {
            underTest.activeAnimations.collect(collectedUpdates::add)
        }
        advanceUntilIdle()
        assertEquals(1, collectedUpdates.size)
        assertTrue(collectedUpdates.last().isEmpty())

        // set up command batch
        val commands = listOf(
            AnimationCommand.AnimateText(Uuid.random(), "First Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Second Text To Animate"),
            AnimationCommand.AnimateText(Uuid.random(), "Third Text To Animate")
        ).toTypedArray()

        // after initial play, the activeAnimations should be updated
        underTest.playAnimationBatch(*commands, onAllAnimationsComplete = {})
        advanceUntilIdle()
        assertEquals(2, collectedUpdates.size)
        assertEquals(commands.toList(), collectedUpdates.last())

        // Act
        underTest.clearAllAnimations()
        advanceUntilIdle()

        // Assert
        assertTrue(collectedUpdates.last().isEmpty())

        // Clean up: cancel collection job
        collectJob.cancel()
    }

    @Test
    fun `Behavior when onAllAnimationsComplete throws an exception`() {
        // Test the service's behavior if the provided onAllAnimationsComplete callback throws an
        // exception. Ensure the service itself doesn't crash and internal state is managed.

        // Arrange
        val command = AnimationCommand.AnimateText(Uuid.random(), "First Text To Animate")
        val faultyCallbackException = IllegalStateException("Test exception from onAllAnimationsComplete")
        var onAllAnimationsCompleteCalled = false
        val onAllAnimationsComplete = {
            onAllAnimationsCompleteCalled = true
            throw faultyCallbackException
        }
        underTest.playAnimationBatch(command, onAllAnimationsComplete = onAllAnimationsComplete)
        assertEquals(
            command,
            underTest.activeAnimations.value.first(),
            "Animations should be active before completion."
        )

        // Act
        // complete batch and verify exception is propagated
        val thrownException = assertFailsWith<IllegalStateException> {
            underTest.notifyAnimationComplete(command.commandId)
        }
        assertEquals(
            faultyCallbackException,
            thrownException,
            "Exception from the callback is propagated."
        )
        assertTrue(
            onAllAnimationsCompleteCalled,
            "Callback was invoked."
        )
        assertTrue(
            underTest.activeAnimations.value.isEmpty(),
            "Active animations should be empty after the batch completion attempt, even if callback failed."
        )

        // test playing a new batch to ensure the service is still functional
        val nextBatchCommand = AnimationCommand.AnimateText(Uuid.random(), "Next batch after faulty callback")
        var nextCallbackInvoked = false
        underTest.playAnimationBatch(nextBatchCommand, onAllAnimationsComplete = {
            nextCallbackInvoked = true
        })

        assertEquals(
            nextBatchCommand,
            underTest.activeAnimations.value.first(),
            "Service should accept a new batch."
        )
        assertFalse(
            nextCallbackInvoked,
            "New batch callback should not be invoked yet."
        )

        // complete the new batch
        underTest.notifyAnimationComplete(nextBatchCommand.commandId)
        assertTrue(
            nextCallbackInvoked,
            "New batch callback should be invokable."
        )
        assertTrue(
            underTest.activeAnimations.value.isEmpty(),
            "Active animations should be empty after the new batch completes."
        )
    }
}