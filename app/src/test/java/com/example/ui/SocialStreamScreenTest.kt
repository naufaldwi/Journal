package com.example.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import com.example.engine.LifeEngine
import com.example.model.AvatarAppearance
import com.example.model.CareerTrack
import com.example.model.CharacterGender
import com.example.model.LifeStats
import com.example.model.SocialChannel
import com.example.model.SocialPostTopic
import com.example.model.SocialStreamComment
import com.example.model.SocialStreamPost
import com.example.model.TemperamentTrait
import com.example.ui.screens.SocialStreamScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SocialStreamScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun socialStreamScreen_allowsPublishingTopicsAndDisplaysLikesNpcCommentsAndFilters() {
        val life = LifeEngine.createNewLife(
            characterName = "Nadia Putri",
            birthplace = "Bandung, Indonesia",
            birthYear = 1999,
            pronouns = "she/her",
            trait = TemperamentTrait.ARTISTIC,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(happiness = 82),
            gender = CharacterGender.FEMALE
        ).updatedLife.copy(
            currentAge = 26,
            careerTrack = CareerTrack.DESIGN,
            fame = 64,
            socialChannel = SocialChannel(handle = "@nadia_studio", followers = 14_200, reputation = 84),
            socialFeed = listOf(
                SocialStreamPost(
                    id = "p_career",
                    authorName = "Nadia Putri",
                    authorHandle = "@nadia_studio",
                    topic = SocialPostTopic.CAREER,
                    age = 26,
                    year = 2025,
                    content = "Wrapped the studio architecture sprint on time!",
                    likes = 945,
                    comments = listOf(
                        SocialStreamComment(
                            id = "c1",
                            authorName = "Budi Santoso",
                            authorRelation = "Team Manager",
                            body = "Respect the hustle — always good collaborating with you!",
                            isSupportive = true
                        ),
                        SocialStreamComment(
                            id = "c2",
                            authorName = "Rina Kusuma",
                            authorRelation = "Office Colleague",
                            body = "Funny how everything looks curated online vs. in person...",
                            isSupportive = false
                        )
                    )
                ),
                SocialStreamPost(
                    id = "p_rel",
                    authorName = "Nadia Putri",
                    authorHandle = "@nadia_studio",
                    topic = SocialPostTopic.RELATIONSHIP,
                    age = 26,
                    year = 2025,
                    content = "Sunday tea date in Braga Street.",
                    likes = 1120,
                    comments = emptyList()
                )
            )
        )

        var capturedTopic: SocialPostTopic? = null
        var capturedMsg: String? = null

        composeTestRule.setContent {
            MaterialTheme {
                SocialStreamScreen(
                    life = life,
                    onPostUpdate = { topic, msg ->
                        capturedTopic = topic
                        capturedMsg = msg
                    }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("social_popularity_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Viral Voice").assertIsDisplayed()

        // Select Career topic, type update, and publish
        composeTestRule.onNodeWithTag("social_stream_list").performScrollToNode(hasAnyDescendant(hasTestTag("social_topic_career")))
        composeTestRule.onNodeWithTag("social_topic_career").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("social_post_input").performScrollTo().performTextInput("Launching a clean new portfolio")
        composeTestRule.onNodeWithTag("publish_social_post_button").performScrollTo().performClick()

        assertEquals(SocialPostTopic.CAREER, capturedTopic)
        assertEquals("Launching a clean new portfolio", capturedMsg)

        // Verify feed post, likes, and NPC comments render
        composeTestRule.onNodeWithTag("social_stream_list").performScrollToNode(hasTestTag("social_post_card_p_career"))
        composeTestRule.onNodeWithTag("social_post_card_p_career").assertIsDisplayed()
        composeTestRule.onNodeWithText("945 likes").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Budi Santoso").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Rina Kusuma").performScrollTo().assertIsDisplayed()

        // Filter to Relationship only -> hides Career post and displays Relationship post
        composeTestRule.onNodeWithTag("social_stream_list").performScrollToNode(hasTestTag("social_filter_row"))
        composeTestRule.onNodeWithTag("social_filter_relationship").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("social_stream_list").performScrollToNode(hasTestTag("social_post_card_p_rel"))
        composeTestRule.onNodeWithTag("social_post_card_p_rel").assertIsDisplayed()
        composeTestRule.onNodeWithText("1,120 likes").performScrollTo().assertIsDisplayed()
        assertTrue(composeTestRule.onAllNodesWithText("Wrapped the studio architecture sprint on time!").fetchSemanticsNodes().isEmpty())
    }
}
