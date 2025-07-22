import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import model.assets.Animation
import service.NovelAnimationServiceImpl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NovelAnimationServiceImplTest {
    private val scope = TestScope()
    private val underTest = NovelAnimationServiceImpl(scope)

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
        underTest.playAnimationBatch(
            animations = listOf(),
            onAllAnimationsComplete = { animations, timedOut ->
                onAllAnimationsCompleteCalled = true
            }
        )
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
        val animations = listOf(
            Animation.Text("First", "Text"),
            Animation.Text("Second", "Text")
        )
        var onAllAnimationsCompleteCalled = false

        // Act & Assert

        // Before notifying about animation completion, they should be active
        underTest.playAnimationBatch(animations, onAllAnimationsComplete = { animations, timedOut ->
            onAllAnimationsCompleteCalled = true
        })
        var activeAnimations = underTest.activeAnimations.value
        assertTrue(activeAnimations.isNotEmpty())
        assertEquals(2, activeAnimations.size)
        assertFalse(onAllAnimationsCompleteCalled)

        // After notifying service about the animations completing, they should be inactive
        animations.forEach { underTest.notifyAnimationComplete(it) }
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
            Animation.Text("First", "Text"),
            Animation.Text("Second", "Text")
        )
        val secondBatch = listOf(
            Animation.Text("Third", "Text"),
            Animation.Text("Fourth", "Text")
        )
        var onFirstBatchCompletedCalled = false
        var onSecondBatchCompletedCalled = false

        // Act & Assert

        // Queue first batch, check that it's active
        underTest.playAnimationBatch(
            animations = firstBatch,
            onAllAnimationsComplete = { animations, timedOut ->
                onFirstBatchCompletedCalled = true
            }
        )
        assertEquals(firstBatch, underTest.activeAnimations.value)

        // Queue second batch before completing the first one
        underTest.playAnimationBatch(
            animations = secondBatch,
            onAllAnimationsComplete = { animations, timedOut ->
                onSecondBatchCompletedCalled = true
            }
        )
        assertEquals(secondBatch, underTest.activeAnimations.value)
        secondBatch.forEach { underTest.notifyAnimationComplete(it) }
        assertTrue(onSecondBatchCompletedCalled)
        assertFalse(onFirstBatchCompletedCalled)
        assertTrue(underTest.activeAnimations.value.isEmpty())
    }

    @Test
    fun `playAnimationBatch with commands having duplicate IDs`() {
        // Verify how the system handles a batch with commands that have the same commandId.

        // Arrange
        val animations = listOf(
            Animation.Text("First", "Text"),
            Animation.Text("First", "Text"),
            Animation.Text("Second", "Text")
        )

        // Act & Assert
        assertFailsWith<IllegalArgumentException> {
            underTest.playAnimationBatch(
                animations = animations,
                onAllAnimationsComplete = { animations, timedOut -> }
            )
        }
    }

    @Test
    fun `notifyAnimationComplete for a valid command`() {
        // Verify that notifying completion of a command removes it from activeAnimations.

        // Arrange
        val animations = listOf(
            Animation.Text("First", "Text"),
            Animation.Text("Second", "Text")
        )
        var onAllAnimationsCompleteCalled = false

        // Act & Assert

        // Before notifying about animation completion, they should all be active
        underTest.playAnimationBatch(
            animations = animations,
            onAllAnimationsComplete = { animations, timedOut ->
                onAllAnimationsCompleteCalled = true
            }
        )
        assertEquals(animations.toList(), underTest.activeAnimations.value)

        // Notify completion of the first command
        underTest.notifyAnimationComplete(animations.first())
        assertEquals(listOf(animations.last()), underTest.activeAnimations.value)
        assertFalse(onAllAnimationsCompleteCalled)
    }

    @Test
    fun `notifyAnimationComplete for a command not in the current batch`() {
        // Verify that notifying completion of a commandId not in pendingCommandsInCurrentBatch does
        // not invoke the completion callback and handles gracefully.

        // Arrange
        val animations = listOf(Animation.Text("First", "Text"))
        val nonBatchAnimation = Animation.Text("Second", "Text")
        var onAllAnimationsCompleteCalled = false
        underTest.playAnimationBatch(
            animations = animations,
            onAllAnimationsComplete = { animations, timedOut ->
                onAllAnimationsCompleteCalled = true
            }
        )

        // Act
        underTest.notifyAnimationComplete(nonBatchAnimation)

        // Assert
        // activeAnimations state should remain unchanged
        assertEquals(animations.first(), underTest.activeAnimations.value.first())
        assertFalse(onAllAnimationsCompleteCalled)
    }

    @Test
    fun `notifyAnimationComplete for a command already completed`() {
        // Verify that calling notifyAnimationComplete multiple times for the same commandId behaves
        // correctly (e.g., doesn't invoke callback multiple times).

        // Arrange
        val animation = Animation.Text("First", "Text")
        var onAllAnimationsCompleteCalled = false
        underTest.playAnimationBatch(
            animations = listOf(animation),
            onAllAnimationsComplete = { animations, timedOut ->
                onAllAnimationsCompleteCalled = true
            }
        )

        // Act & Assert

        // Notify completion of the first command, should complete the batch
        underTest.notifyAnimationComplete(animation)
        assertTrue(onAllAnimationsCompleteCalled)

        // Notify with the same commandId again, should fail with an exception
        // and callback should not be invoked again
        onAllAnimationsCompleteCalled = false
        underTest.notifyAnimationComplete(animation)
        assertFalse(onAllAnimationsCompleteCalled)
    }

    @Test
    fun `notifyAnimationComplete when no batch is active`() {
        // Verify that calling notifyAnimationComplete when currentBatchCompletionCallback is null
        // (no active batch) handles gracefully.

        // Arrange
        val animation = Animation.Text("First", "Text")
        assertTrue(underTest.activeAnimations.value.isEmpty())

        // Act
        underTest.notifyAnimationComplete(animation)

        // Assert
        assertTrue(underTest.activeAnimations.value.isEmpty())
    }

    @Test
    fun `notifyAnimationComplete sequence and activeAnimations updates`() {
        // Play a batch with multiple commands. Notify completion for them one by one and verify
        // activeAnimations flow emits the correct list of remaining animations after each
        // notification.

        // Arrange
        val animations = listOf(
            Animation.Text("First", "Text"),
            Animation.Text("Second", "Text"),
            Animation.Text("Third", "Text")
        )
        var onAllAnimationsCompleteCalled = false
        underTest.playAnimationBatch(
            animations = animations,
            onAllAnimationsComplete = { animations, timedOut ->
                onAllAnimationsCompleteCalled = true
            }
        )

        // Act & Assert

        // Before notifying about animation completion, they should all be active
        assertFalse(onAllAnimationsCompleteCalled)
        assertEquals(animations.toList(), underTest.activeAnimations.value)

        // After each notify call, the active animations list should reflect the remaining commands
        animations.forEachIndexed { index, animation ->
            underTest.notifyAnimationComplete(animation)
            val remainingActiveAnimation = animations.subList(index + 1, animations.size)
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
        val animations = listOf(
            Animation.Text("First", "Text"),
            Animation.Text("Second", "Text"),
            Animation.Text("Third", "Text")
        )
        var onAllAnimationsCompleteCalled = false
        underTest.playAnimationBatch(
            animations = animations,
            onAllAnimationsComplete = { animations, timedOut ->
                onAllAnimationsCompleteCalled = true
            }
        )

        // Act & Assert

        // Before clearing all animations, they should all be active
        assertEquals(animations, underTest.activeAnimations.value)

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
        val animations = listOf(
            Animation.Text("First", "Text"),
            Animation.Text("Second", "Text"),
            Animation.Text("Third", "Text")
        )
        var onAllAnimationsCompleteCalled = false
        underTest.playAnimationBatch(
            animations = animations,
            onAllAnimationsComplete = { animations, timedOut ->
                onAllAnimationsCompleteCalled = true
            }
        )

        // Act & Assert
        underTest.notifyAnimationComplete(animations.first())
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
            Animation.Text("First", "Text"),
            Animation.Text("Second", "Text"),
            Animation.Text("Third", "Text")
        )
        val secondBatch = listOf(
            Animation.Text("Fourth", "Text"),
            Animation.Text("Fifth", "Text")
        )
        var onAllAnimationsCompleteCalled = false
        underTest.playAnimationBatch(
            animations = firstBatch,
            onAllAnimationsComplete = { animations, timedOut ->
                onAllAnimationsCompleteCalled = true
            }
        )

        // Act & Assert
        underTest.notifyAnimationComplete(firstBatch.first())
        assertEquals(2, underTest.activeAnimations.value.size)
        assertFalse(onAllAnimationsCompleteCalled)

        underTest.playAnimationBatch(
            animations = secondBatch,
            onAllAnimationsComplete = { animations, timedOut ->
                onAllAnimationsCompleteCalled = true
            }
        )
        assertEquals(secondBatch, underTest.activeAnimations.value)
        assertFalse(onAllAnimationsCompleteCalled)

        secondBatch.forEach { underTest.notifyAnimationComplete(it) }
        assertTrue(onAllAnimationsCompleteCalled)
    }

    @Test
    fun `Interaction  playAnimationBatch  clearAllAnimations  playAnimationBatch again`() {
        // Play a batch, clear all, then play another batch. Verify the state is correctly set for
        // the new batch and the cleared batch's callback is not invoked.

        val firstBatch = listOf(
            Animation.Text("First", "Text"),
            Animation.Text("Second", "Text"),
            Animation.Text("Third", "Text")
        )
        val secondBatch = listOf(
            Animation.Text("Fourth", "Text"),
            Animation.Text("Fifth", "Text")
        )
        var onAllAnimationsCompleteCalled = false

        // Act & Assert
        underTest.playAnimationBatch(
            animations = firstBatch,
            onAllAnimationsComplete = { animations, timedOut ->
                onAllAnimationsCompleteCalled = true
            }
        )
        assertEquals(firstBatch, underTest.activeAnimations.value)
        assertFalse(onAllAnimationsCompleteCalled)

        underTest.clearAllAnimations()
        assertTrue(underTest.activeAnimations.value.isEmpty())
        assertFalse(onAllAnimationsCompleteCalled)

        underTest.playAnimationBatch(
            animations = secondBatch,
            onAllAnimationsComplete = { animations, timedOut ->
                onAllAnimationsCompleteCalled = true
            }
        )
        assertEquals(secondBatch, underTest.activeAnimations.value)
        assertFalse(onAllAnimationsCompleteCalled)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `activeAnimations flow emission on playAnimationBatch`() = runTest {
        // Verify that collectors of the activeAnimations flow receive an update when
        // playAnimationBatch is called with new commands.

        // Arrange
        // set up collecting state flow updates
        val collectedUpdates = mutableListOf<List<Animation>>()
        val collectJob = launch {
            underTest.activeAnimations.collect(collectedUpdates::add)
        }
        advanceUntilIdle()
        assertEquals(1, collectedUpdates.size)
        assertTrue(collectedUpdates.first().isEmpty())

        // set up command batch
        val animations = listOf(
            Animation.Text("First", "Text"),
            Animation.Text("Second", "Text"),
            Animation.Text("Third", "Text")
        )

        // Act & Assert
        underTest.playAnimationBatch(
            animations = animations,
            onAllAnimationsComplete = { animations, timedOut -> }
        )
        advanceUntilIdle()
        assertEquals(2, collectedUpdates.size)
        assertEquals(animations, collectedUpdates.last())

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
        val collectedUpdates = mutableListOf<List<Animation>>()
        val collectJob = launch {
            underTest.activeAnimations.collect(collectedUpdates::add)
        }
        advanceUntilIdle()
        assertEquals(1, collectedUpdates.size)
        assertTrue(collectedUpdates.last().isEmpty())

        // set up command batch
        val animations = listOf(
            Animation.Text("First", "Text"),
            Animation.Text("Second", "Text"),
            Animation.Text("Third", "Text")
        )
        var onAllAnimationsCompleteCalled = false

        // Act & Assert
        // After initial play, the activeAnimations should be updated
        underTest.playAnimationBatch(
            animations = animations,
            onAllAnimationsComplete = { animations, timedOut ->
                onAllAnimationsCompleteCalled = true
            }
        )
        advanceUntilIdle()
        assertEquals(2, collectedUpdates.size)
        assertEquals(animations.toList(), collectedUpdates.last())

        // After each notify call, we should get a list of remaining commands
        animations.forEachIndexed { index, animation ->
            underTest.notifyAnimationComplete(animation)
            advanceUntilIdle()
            val remainingCommands = animations.subList(index + 1, animations.size)
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
        val collectedUpdates = mutableListOf<List<Animation>>()
        val collectJob = launch {
            underTest.activeAnimations.collect(collectedUpdates::add)
        }
        advanceUntilIdle()
        assertEquals(1, collectedUpdates.size)
        assertTrue(collectedUpdates.last().isEmpty())

        // set up command batch
        val animations = listOf(
            Animation.Text("First", "Text"),
            Animation.Text("Second", "Text"),
            Animation.Text("Third", "Text")
        )

        // after initial play, the activeAnimations should be updated
        underTest.playAnimationBatch(
            animations = animations,
            onAllAnimationsComplete = { animations, timedOut -> }
        )
        advanceUntilIdle()
        assertEquals(2, collectedUpdates.size)
        assertEquals(animations, collectedUpdates.last())

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
        val animation = Animation.Text("First", "Text")
        val faultyCallbackException = IllegalStateException("Test exception from onAllAnimationsComplete")
        var onAllAnimationsCompleteCalled = false
        val onAllAnimationsComplete = {
            onAllAnimationsCompleteCalled = true
            throw faultyCallbackException
        }
        underTest.playAnimationBatch(
            animations = listOf(animation),
            onAllAnimationsComplete = { animations, timedOut -> onAllAnimationsComplete() }
        )
        assertEquals(
            animation,
            underTest.activeAnimations.value.first(),
            "Animations should be active before completion."
        )

        // Act
        // complete batch and verify exception is propagated
        val thrownException = assertFailsWith<IllegalStateException> {
            underTest.notifyAnimationComplete(animation)
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
        val nextAnimation = Animation.Text("Second", "Text")
        var nextCallbackInvoked = false
        underTest.playAnimationBatch(
            animations = listOf(nextAnimation),
            onAllAnimationsComplete = { animations, timedOut ->
                nextCallbackInvoked = true
            }
        )

        assertEquals(
            nextAnimation,
            underTest.activeAnimations.value.first(),
            "Service should accept a new batch."
        )
        assertFalse(
            nextCallbackInvoked,
            "New batch callback should not be invoked yet."
        )

        // complete the new batch
        underTest.notifyAnimationComplete(nextAnimation)
        assertTrue(
            nextCallbackInvoked,
            "New batch callback should be invokable."
        )
        assertTrue(
            underTest.activeAnimations.value.isEmpty(),
            "Active animations should be empty after the new batch completes."
        )
    }

    @Test
    fun `playAnimationBatch - onAllAnimationsComplete should contain successful animations`() {
        // Arrange
        val animations = listOf(
            Animation.Text("First", "Text"),
            Animation.Text("Second", "Text"),
            Animation.Text("Third", "Text")
        )
        var succeededAnimations = listOf<Animation>()
        val onAllAnimationsComplete = { playedAnimations: List<Animation>, timedOut: Boolean ->
            succeededAnimations = playedAnimations
        }

        // Act
        underTest.playAnimationBatch(animations, onAllAnimationsComplete)
        animations.forEach(underTest::notifyAnimationComplete)

        // Assert
        assertEquals(
            animations,
            succeededAnimations,
            "onAllAnimationsComplete should contain all successfully played animations."
        )
    }

    @Test
    fun `playAnimationBatch - onAllAnimationsComplete with empty batch should return empty list`() {
        // Arrange
        var succeededAnimations: List<Animation>? = null
        val onAllAnimationsComplete = { playedAnimations: List<Animation>, timedOut: Boolean ->
            succeededAnimations = playedAnimations
        }

        // Act
        underTest.playAnimationBatch(listOf(), onAllAnimationsComplete)

        // Assert
        assertNotNull(
            succeededAnimations,
            "succeededAnimations should not be null."
        )
        assertTrue(
            succeededAnimations.isEmpty(),
            "onAllAnimationsComplete should return an empty list for an empty batch."
        )
    }

    @Test
    fun `playAnimationBatch - onAllAnimationsComplete should not contain failed animations`() {
        // Arrange
        val successfulAnimations = listOf(
            Animation.Text("First", "Text"),
            Animation.Text("Second", "Text"),
            Animation.Text("Third", "Text")
        )
        val failedAnimation = Animation.Text("Fourth", "Text")
        var callbackResult = listOf<Animation>()
        val onAllAnimationsComplete = { playedAnimations: List<Animation>, timedOut: Boolean ->
            callbackResult = playedAnimations
        }

        // Act
        underTest.playAnimationBatch(successfulAnimations, onAllAnimationsComplete)
        underTest.notifyAnimationComplete(failedAnimation)
        successfulAnimations.forEach(underTest::notifyAnimationComplete)

        // Assert
        assertFalse(
            callbackResult.contains(failedAnimation),
            "onAllAnimationsComplete should not contain failed animations."
        )
        assertEquals(
            successfulAnimations,
            callbackResult,
            "onAllAnimationsComplete should contain all successfully played animations."
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `playAnimationBatch - timeout should be called after the timeout period`() = runTest {
        // Arrange
        val underTest = NovelAnimationServiceImpl(this)
        val animation = Animation.SpriteTransition(
            baseName = "SomeCharacter",
            name = "Dancing",
            durationMillis = 50,
            fromSpriteId = "SomeSpriteId",
            toSpriteId = "SomeOtherSpriteId"
        )
        var successfulAnimations = listOf<Animation>()
        var batchTimedOut = false
        val onAllAnimationsComplete = { playedAnimations: List<Animation>, timedOut: Boolean ->
            successfulAnimations = playedAnimations
            batchTimedOut = timedOut
        }

        // Act
        underTest.playAnimationBatch(listOf(animation), onAllAnimationsComplete)
        advanceTimeBy(200)

        // Assert
        assertTrue(batchTimedOut)
        assertTrue(successfulAnimations.isEmpty())
    }
}