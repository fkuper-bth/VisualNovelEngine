package etc.utils.preview

import data.model.StoryPassageNovelEvent
import fk.story.engine.main.utils.StoryPassagePlayResult

internal object PreviewData {
    private val novelEvents = listOf(
        StoryPassageNovelEvent.InformationalText(
            value = "This is a test scene to demonstrate the visual novel engine."
        ),
        StoryPassageNovelEvent.CharacterAction(
            characterName = "banker_secondary",
            expression = "smiling",
            text = "Hello Player! I'm happy to be here, but I do not have much to say."
        ),
        StoryPassageNovelEvent.PlayerText(
            value = "Hi Banker! Nice to see you."
        ),
        StoryPassageNovelEvent.CharacterAction(
            characterName = "banker_main",
            expression = "smiling",
            text = "I'm the main banker, so you should pay some respect to me."
        ),
        StoryPassageNovelEvent.PlayerText(
            value = "Alright good to know."
        ),
        StoryPassageNovelEvent.CharacterAction(
            characterName = "banker_secondary",
            expression = "angry",
            text = "Yea kid. Don't get it twisted."
        ),
        StoryPassageNovelEvent.CharacterAction(
            characterName = "banker_main",
            expression = "angry",
            text = "Do you get it?"
        ),
        StoryPassageNovelEvent.Link(
            linkText = "No... not really?",
            targetPassageName = "NextPassage"
        ),
        StoryPassageNovelEvent.Link(
            linkText = "I think I do.",
            targetPassageName = "NextPassage"
        ),
        StoryPassageNovelEvent.Link(
            linkText = "Time will tell.",
            targetPassageName = "NextPassage"
        ),
    )

    val passageData = StoryPassagePlayResult.DataReady(
        passageEvents = novelEvents,
        storyPlaythroughRecord = emptyList()
    )
}