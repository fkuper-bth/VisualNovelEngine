package api

import animation.AnimationCommand
import animation.NovelAnimationService
import data.model.StoryPassageNovelEvent
import fk.story.engine.main.contract.toNovelEventPlayHistory
import fk.story.engine.main.utils.StoryPassagePlayResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import model.SceneRenderState
import model.toRenderedText
import sound.SoundEngine

interface VisualNovelEngine {
    fun handleStoryPassagePlay(passageData: StoryPassagePlayResult.DataReady)

    /**
     * Clears the current scene and resets internal state.
     */
    fun reset()

    /**
     * The current visual scene state, used by the UI for rendering.
     */
    val sceneState: StateFlow<SceneRenderState>

    /**
     * Whether the engine is currently executing a visual transition (e.g. fade, move).
     * Useful to block user input during transitions.
     */
    val isBusy: StateFlow<Boolean>

    fun loadVisualNovelSceneState(state: SceneRenderState)
}

class VisualNovelEngineImpl(
    private val animationService: NovelAnimationService,
    private val soundEngine: SoundEngine? = null
) : VisualNovelEngine {
    private val _sceneState = MutableStateFlow(SceneRenderState())
    override val sceneState get() = _sceneState.asStateFlow()

    private val _isBusy = MutableStateFlow(false)
    override val isBusy get() = _isBusy.asStateFlow()

    private var currentPassageEventIndex: Int = 0
    private var currentPassageEvents = emptyList<StoryPassageNovelEvent>()
    private var currentPassageAccumulatedLinks = mutableListOf<StoryPassageNovelEvent.Link>()

    override fun loadVisualNovelSceneState(state: SceneRenderState) {
        _sceneState.value = state
    }

    override fun reset() {
        _sceneState.value = SceneRenderState()
        _isBusy.value = false
        currentPassageEventIndex = 0
        currentPassageEvents = emptyList()
        currentPassageAccumulatedLinks.clear()
    }

    override fun handleStoryPassagePlay(passageData: StoryPassagePlayResult.DataReady) {
        // Prepare state for the next passage
        val textBoxes = passageData.storyPlaythroughRecord
            .toNovelEventPlayHistory()
            .mapNotNull { it.toRenderedText() }
        _sceneState.value = _sceneState.value.copy(textBoxes = textBoxes)

        // Start processing events of the next passage
        currentPassageEvents = passageData.passageEvents
        currentPassageAccumulatedLinks.clear()
        if (currentPassageEvents.isNotEmpty()) {
            processNextEvent()
        }
    }

    private fun processNextEvent() {
        // All events in the current passage have been processed
        if (currentPassageEventIndex >= currentPassageEvents.size) {
            _sceneState.value = _sceneState.value.copy(
                textBoxes = _sceneState.value.textBoxes.toMutableList().apply {
                    currentPassageAccumulatedLinks.forEach {
                        it.toRenderedText()?.let(::add)
                    }
                }
            )
            return
        }

        val currentEvent = currentPassageEvents[currentPassageEventIndex]
        when (currentEvent) {
            is StoryPassageNovelEvent.CharacterAction -> {
                // Highlight & animate speaking character
                // Render & animate character's text
                _isBusy.value = true

                val animateTextCommand = AnimationCommand
                    .AnimateText(currentEvent.identifier, currentEvent.text)
                animationService.playAnimationBatch(animateTextCommand) {
                    currentPassageEventIndex++
                    processNextEvent()
                    _isBusy.value = false
                }

                _sceneState.value = _sceneState.value.copy(
                    activeCharacter = _sceneState.value.characters.firstOrNull {
                        it.name == currentEvent.characterName
                    },
                    textBoxes = _sceneState.value.textBoxes.toMutableList().apply {
                        currentEvent.toRenderedText()?.let(::add)
                    }
                )
            }
            is StoryPassageNovelEvent.InformationalText -> {
                _isBusy.value = true

                val animateTextCommand = AnimationCommand
                    .AnimateText(currentEvent.identifier, currentEvent.value)
                animationService.playAnimationBatch(animateTextCommand) {
                    currentPassageEventIndex++
                    processNextEvent()
                    _isBusy.value = false
                }

                _sceneState.value = _sceneState.value.copy(
                    textBoxes = _sceneState.value.textBoxes.toMutableList().apply {
                        currentEvent.toRenderedText()?.let(::add)
                    }
                )
            }
            is StoryPassageNovelEvent.PlayerText -> {
                _isBusy.value = true

                val animateTextCommand = AnimationCommand
                    .AnimateText(currentEvent.identifier, currentEvent.value)
                animationService.playAnimationBatch(animateTextCommand) {
                    currentPassageEventIndex++
                    processNextEvent()
                    _isBusy.value = false
                }

                _sceneState.value = _sceneState.value.copy(
                    textBoxes = _sceneState.value.textBoxes.toMutableList().apply {
                        currentEvent.toRenderedText()?.let(::add)
                    }
                )
            }
            is StoryPassageNovelEvent.Link -> {
                currentPassageAccumulatedLinks.add(currentEvent)
                currentPassageEventIndex++
                processNextEvent()
            }
            is StoryPassageNovelEvent.Sound -> TODO()
            else -> {
                currentPassageEventIndex++
                processNextEvent()
            }
        }
    }
}