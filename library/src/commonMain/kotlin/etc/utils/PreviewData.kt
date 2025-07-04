package etc.utils

import data.model.StoryPassageNovelEvent
import fk.story.engine.main.utils.StoryPassagePlayResult

internal object PreviewData {
    private val novelEvents = listOf(
        StoryPassageNovelEvent.InformationalText(
            value = "This is a test scene to demonstrate the visual novel engine."
        ),
        StoryPassageNovelEvent.CharacterAction(
            characterName = "banker_left",
            expression = "Smiling",
            text = "Hello Player! I'm happy to be here, but I do not have much to say."
        ),
        StoryPassageNovelEvent.PlayerText(
            value = "Hi Banker! Nice to see you."
        ),
        StoryPassageNovelEvent.CharacterAction(
            characterName = "banker_center",
            expression = "Smiling",
            text = "I'm the main banker, so you should pay some respect to me."
        ),
        StoryPassageNovelEvent.Link(
            linkText = "Uhh... okay?",
            targetPassageName = "NextPassage"
        ),
        StoryPassageNovelEvent.Link(
            linkText = "Okay, but I won't.",
            targetPassageName = "NextPassage"
        ),
        StoryPassageNovelEvent.Link(
            linkText = "Fine.",
            targetPassageName = "NextPassage"
        ),
    )

    val passageData = StoryPassagePlayResult.DataReady(
        passageEvents = novelEvents,
        storyPlaythroughRecord = emptyList()
    )
}