package api.engine

import data.model.StoryPassageNovelEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import main.contract.StoryEngine
import main.contract.StoryPlayer
import main.contract.toNovelEventPlayHistory
import main.utils.StoryPassagePlayResult
import model.assets.Animation
import model.assets.Sprite
import model.assets.Story
import model.assets.Text
import model.assets.toText
import model.scene.SceneRenderStateIds
import service.AssetStore
import service.NovelAnimationService
import service.SoundEngine
import service.StoryRenderController
import service.getNow

internal class VisualNovelStoryPlayerImpl(
    private val assetStore: AssetStore,
    private val animationService: NovelAnimationService,
    private val storyRenderController: StoryRenderController,
    private val storyEngine: StoryEngine,
    private val soundEngine: SoundEngine? = null,
    private val coroutineScope: CoroutineScope
) : VisualNovelStoryPlayer {
    override val uiState = storyRenderController.storyRenderState
    private val _isBusy = MutableStateFlow(false)
    override val isBusy get() = _isBusy.asStateFlow()

    private val sceneIds = storyRenderController.requestedSceneIds
    private var currentPassageEventIndex: Int = 0
    private var currentPassageEvents = emptyList<StoryPassageNovelEvent>()
    private var currentPassageAccumulatedLinks = mutableListOf<Text.Link>()
    private var storyPlayer: StoryPlayer? = null

    override fun playStory(storyId: String) {
        val storyAsset = assetStore.getNow<Story>(storyId)
        if (storyAsset == null) {
            storyRenderController.setErrorState("Story not found.")
            return
        }

        storyPlayer = storyEngine.startPlaying(storyAsset.content)
        storyPlayer?.playPassage()

        coroutineScope.launch {
            storyPlayer?.currentPlayResult?.collect { result ->
                when (result) {
                    is StoryPassagePlayResult.DataReady -> {
                        handleStoryPassagePlay(result)
                    }
                    is StoryPassagePlayResult.Error -> {
                        storyRenderController.setErrorState(result.message)
                    }
                    null -> { /* Initial state or waiting for data */ }
                }
            }
        }
    }

    override fun loadVisualNovelSceneState(state: SceneRenderStateIds) {
        // Clear all pending animations from the animation service
        animationService.clearAllAnimations()

        // Update the scene state
        storyRenderController.setScene(state)
    }

    override fun chooseStoryPassage(link: StoryPassageNovelEvent.Link) {
        storyPlayer?.playPassage(link)
    }

    override fun reset() {
        _isBusy.value = false
        storyPlayer = null
        currentPassageEventIndex = 0
        currentPassageEvents = emptyList()
        currentPassageAccumulatedLinks.clear()
        storyRenderController.setScene()
    }

    private fun handleStoryPassagePlay(passageData: StoryPassagePlayResult.DataReady) {
        // Update link assets to reflect whether they have been chosen
        fun transformNovelEvent(event: StoryPassageNovelEvent): Text? {
            val textAsset = event.toText()
            return when (textAsset) {
                is Text.Link -> {
                    if (textAsset.link.linkText == null) {
                        // Discard links with no link text since they shouldn't to be displayed
                        return null
                    }
                    textAsset.copy(wasChosen = true)
                }
                else -> textAsset
            }
        }
        val textBoxes = passageData.storyPlaythroughRecord
            .toNovelEventPlayHistory()
            .mapNotNull(::transformNovelEvent)

        // Update the asset store with the new text assets
        assetStore.addOrUpdateAssets(textBoxes)
        assetStore.addOrUpdateAssets(textBoxes.map(Text::animationProps))

        // Remove any link assets from scene state that are not supposed to be displayed anymore
        assetStore.getNow<Text.Link>(
            ids = currentPassageAccumulatedLinks.map(Text.Link::id)
        )
            ?.filterNot(Text.Link::wasChosen)
            ?.map(Text.Link::id)
            ?.let { linkIdsToRemoveFromSceneState ->
                storyRenderController.setScene(
                    sceneIds.value.copy(textBoxIds = sceneIds.value.textBoxIds
                        .toMutableList()
                        .filterNot { it in linkIdsToRemoveFromSceneState }
                    )
                )
            }

        // Prepare internal state for the next passage
        currentPassageEventIndex = 0
        currentPassageEvents = passageData.passageEvents
        currentPassageAccumulatedLinks.clear()

        // Start processing events of the next passage
        if (currentPassageEvents.isNotEmpty()) {
            processNextEvent()
        }
    }

    private fun processNextEvent() {
        if (currentPassageEventIndex >= currentPassageEvents.size) {
            // All events in the current passage have been processed
            storyRenderController.setScene(
                sceneIds.value.copy(textBoxIds = sceneIds.value.textBoxIds.toMutableList().apply {
                    currentPassageAccumulatedLinks.forEach { add(it.id) }
                })
            )
            return
        }

        val currentEvent = currentPassageEvents[currentPassageEventIndex]
        when (currentEvent) {
            is StoryPassageNovelEvent.CharacterAction -> {
                _isBusy.value = true

                val characterSprite = assetStore.getNow<Sprite.Character>(
                    id = currentEvent.characterName
                )
                val characterText = currentEvent.toText() as Text.Character
                assetStore.addOrUpdateAsset(characterText)
                val characterAnimationId = Animation.Companion.createIdentifier(
                    baseName = currentEvent.characterName,
                    animationName = currentEvent.expression
                )
                val animationsToPlay = mutableListOf<Animation>(characterText.animationProps)
                val characterAnimation = assetStore.getNow<Animation>(characterAnimationId)
                if (characterAnimation != null) {
                    animationsToPlay.add(characterAnimation)
                } else {
                    println("Failed to fetch animation with ID: $characterAnimationId")
                }

                animationService.playAnimationBatch(
                    animations = animationsToPlay,
                    onAllAnimationsComplete = { onAnimationBatchCompleted(it) }
                )
                storyRenderController.setScene(
                    sceneIds.value.copy(
                        activeCharacterId = characterSprite?.id,
                        textBoxIds = sceneIds.value.textBoxIds + characterText.id
                    )
                )
            }

            is StoryPassageNovelEvent.InformationalText -> {
                _isBusy.value = true

                val infoText = currentEvent.toText() as Text.Info
                assetStore.addOrUpdateAsset(infoText)

                animationService.playAnimationBatch(
                    animations = listOf(infoText.animationProps),
                    onAllAnimationsComplete = { onAnimationBatchCompleted(it) }
                )
                storyRenderController.setScene(
                    sceneIds.value.copy(
                        textBoxIds = sceneIds.value.textBoxIds + infoText.id
                    )
                )
            }

            is StoryPassageNovelEvent.PlayerText -> {
                _isBusy.value = true

                val playerText = currentEvent.toText() as Text.Player
                assetStore.addOrUpdateAsset(playerText)

                animationService.playAnimationBatch(
                    animations = listOf(playerText.animationProps),
                    onAllAnimationsComplete = { onAnimationBatchCompleted(it) }
                )
                storyRenderController.setScene(
                    sceneIds.value.copy(
                        textBoxIds = sceneIds.value.textBoxIds + playerText.id
                    )
                )
            }

            is StoryPassageNovelEvent.Link -> {
                val linkText = currentEvent.toText() as Text.Link
                assetStore.addOrUpdateAsset(linkText)
                currentPassageAccumulatedLinks.add(linkText)
                currentPassageEventIndex++
                processNextEvent()
            }

            is StoryPassageNovelEvent.Sound -> {
                // TODO: implement this properly, this is just a placeholder implementation
                soundEngine?.playSoundEffect(currentEvent.name)
                currentPassageEventIndex++
                processNextEvent()
            }

            is StoryPassageNovelEvent.StoryEnded -> {
                storyRenderController.setStoryEnded(currentEvent.playthroughRecord)
            }

            is StoryPassageNovelEvent.Custom -> {
                // TODO: add callback for custom events here
                currentPassageEventIndex++
                processNextEvent()
            }

            else -> {
                currentPassageEventIndex++
                processNextEvent()
            }
        }
    }

    private fun onAnimationBatchCompleted(completedAnimations: List<Animation>) {
        // Perform any necessary updates to the scene state resulting from the completed animations
        completedAnimations.forEach { animation ->
            when (animation) {
                is Animation.SpriteTransition -> {
                    val toSprite = assetStore.getNow<Sprite>(animation.toSpriteId)
                    if (toSprite == null) {
                        println("Failed to fetch animation with ID: ${animation.toSpriteId}")
                        return@forEach
                    }
                    assetStore.addOrUpdateAsset(animation.fromSpriteId, toSprite)
                }
                is Animation.SpriteSheet,
                is Animation.Text -> {
                    // No need to update asset store for these animations
                }
            }
        }

        // Update the current passage index and process the next event
        currentPassageEventIndex++
        processNextEvent()
        _isBusy.value = false
    }
}