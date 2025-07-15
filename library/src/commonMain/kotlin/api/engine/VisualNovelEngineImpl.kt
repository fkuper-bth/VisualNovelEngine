package api.engine

import service.NovelAnimationService
import service.AssetStore
import service.getNow
import model.assets.Animation
import model.assets.Asset
import service.SceneRenderController
import model.scene.SceneRenderStateIds
import model.assets.Sprite
import data.model.StoryPassageNovelEvent
import model.assets.Text
import model.assets.toText
import fk.story.engine.main.contract.toNovelEventPlayHistory
import fk.story.engine.main.utils.StoryPassagePlayResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import service.SoundEngine

internal class VisualNovelEngineImpl(
    private val assetStore: AssetStore,
    private val animationService: NovelAnimationService,
    private val sceneRenderController: SceneRenderController,
    private val soundEngine: SoundEngine? = null
) : VisualNovelEngine {
    private val sceneIds = sceneRenderController.sceneIds
    override val sceneState get() = sceneRenderController.renderState

    private val _isBusy = MutableStateFlow(false)
    override val isBusy get() = _isBusy.asStateFlow()

    private var currentPassageEventIndex: Int = 0
    private var currentPassageEvents = emptyList<StoryPassageNovelEvent>()
    private var currentPassageAccumulatedLinks = mutableListOf<Text.Link>()

    override fun loadAssets(assets: List<Asset>) {
        assetStore.addOrUpdateAssets(assets)
    }

    override fun loadVisualNovelSceneState(state: SceneRenderStateIds) {
        // Clear all pending animations from the animation service
        animationService.clearAllAnimations()

        // Update the scene state
        sceneRenderController.setScene(state)
    }

    override fun reset() {
        _isBusy.value = false
        currentPassageEventIndex = 0
        currentPassageEvents = emptyList()
        currentPassageAccumulatedLinks.clear()
        sceneRenderController.setScene()
    }

    override fun handleStoryPassagePlay(passageData: StoryPassagePlayResult.DataReady) {
        // Prepare state for the next passage
        val textBoxes = passageData.storyPlaythroughRecord
            .toNovelEventPlayHistory()
            .mapNotNull { it.toText() }
        assetStore.addOrUpdateAssets(textBoxes)
        assetStore.addOrUpdateAssets(textBoxes.map { it.animationProps })
        sceneRenderController.setScene(
            sceneIds.value.copy(textBoxIds = textBoxes.map(Text::id))
        )

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
            sceneRenderController.setScene(
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

                // FIXME: where to define text animation properties?
                // this is not a nice solution, since it does not allow text animations to be
                // customized on an event basis
                val characterText = currentEvent.toText() as Text.Character
                assetStore.addOrUpdateAsset(characterText)
                val characterAnimationId = Animation.createIdentifier(
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

                val character = assetStore.getNow<Sprite.Character>(currentEvent.characterName)
                sceneRenderController.setScene(
                    sceneIds.value.copy(
                        activeCharacterId = character?.id,
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
                sceneRenderController.setScene(
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
                sceneRenderController.setScene(
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