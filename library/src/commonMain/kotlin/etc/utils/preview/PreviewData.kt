package etc.utils.preview

import data.model.Story
import data.model.StoryPassage
import data.model.StoryPassageNovelEvent
import kotlinx.serialization.json.Json

internal object PreviewData {
    val storyJsonContent get() = Json.encodeToString<Story>(story)

    private val story get() = Story.createTestInstance(
        name = "TestStory",
        startNode = "1",
        passages = passages.map {
            StoryPassageNovelEvent.Link(targetPassageName = it.key) to it.value
        }.toMap()
    )

    private val passages get() = mapOf(
        PASSAGE_ONE_NAME to StoryPassage.createTestInstance(
            id = "1",
            name = PASSAGE_ONE_NAME,
            novelEvents = passageOneNovelEvents
        ),
        PASSAGE_TWO_NAME to StoryPassage.createTestInstance(
            id = "2",
            name = PASSAGE_TWO_NAME,
            novelEvents = passageTwoNovelEvents
        )
    )

    private const val PASSAGE_ONE_NAME = "PassageOne"
    private const val PASSAGE_TWO_NAME = "PassageTwo"
    private const val BANKER_MAIN_NAME = "banker_main"
    private const val BANKER_SECONDARY_NAME = "banker_secondary"

    private val passageOneNovelEvents = listOf(
        StoryPassageNovelEvent.InformationalText(
            value = "This is a test scene to demonstrate the visual novel engine."
        ),
        StoryPassageNovelEvent.CharacterAction(
            characterName = BANKER_SECONDARY_NAME,
            expression = "smiling",
            text = "Hello Player! I'm happy to be here, but I do not have much to say."
        ),
        StoryPassageNovelEvent.PlayerText(
            value = "Hi Banker! Nice to see you."
        ),
        StoryPassageNovelEvent.CharacterAction(
            characterName = BANKER_MAIN_NAME,
            expression = "smiling",
            text = "I'm the main banker, so you should pay some respect to me."
        ),
        StoryPassageNovelEvent.PlayerText(
            value = "Alright good to know."
        ),
        StoryPassageNovelEvent.CharacterAction(
            characterName = BANKER_SECONDARY_NAME,
            expression = "angry",
            text = "Yea kid. Don't get it twisted."
        ),
        StoryPassageNovelEvent.CharacterAction(
            characterName = BANKER_MAIN_NAME,
            expression = "angry",
            text = "Do you get it?"
        ),
        StoryPassageNovelEvent.Link(
            linkText = "No... not really?",
            targetPassageName = PASSAGE_TWO_NAME
        ),
        StoryPassageNovelEvent.Link(
            linkText = "I think I do.",
            targetPassageName = PASSAGE_TWO_NAME
        ),
        StoryPassageNovelEvent.Link(
            linkText = "Time will tell.",
            targetPassageName = PASSAGE_TWO_NAME
        ),
        StoryPassageNovelEvent.Link(
            linkText = "This will cause a playback error!",
            targetPassageName = "NonExistentPassage"
        )
    )

    private val passageTwoNovelEvents = listOf(
        StoryPassageNovelEvent.InformationalText(
            value = "You notice that there is nothing left to do and decide to call it a day."
        ),
        StoryPassageNovelEvent.PlayerText(
            value = "Looks like we are done here..."
        ),
        StoryPassageNovelEvent.StoryEnded()
    )
}
