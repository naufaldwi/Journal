package com.example.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.example.engine.LifeEngine
import com.example.model.AvatarAppearance
import com.example.model.BondCategory
import com.example.model.CharacterGender
import com.example.model.LifeStats
import com.example.model.Relationship
import com.example.model.RelationshipPriority
import com.example.model.TemperamentTrait
import com.example.ui.screens.RelationshipSettingsScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RelationshipSettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun relationshipSettings_togglesNpcPrioritiesFiltersAndTriggersBoostAndQuickActions() {
        val life = LifeEngine.createNewLife(
            characterName = "Nadia Putri",
            birthplace = "Bandung, Indonesia",
            birthYear = 1999,
            pronouns = "she/her",
            trait = TemperamentTrait.EMPATHETIC,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(happiness = 76),
            gender = CharacterGender.FEMALE
        ).updatedLife.copy(
            currentAge = 26,
            netWorth = 1200,
            relationships = listOf(
                Relationship(
                    id = "rel_partner",
                    name = "Raka Putra",
                    role = "Partner",
                    age = 27,
                    closeness = 84,
                    isPartner = true,
                    priority = RelationshipPriority.GIFTING,
                    gender = CharacterGender.MALE,
                    category = BondCategory.ROMANCE
                ),
                Relationship(
                    id = "rel_colleague",
                    name = "Dimas Wijaya",
                    role = "Office Colleague",
                    age = 29,
                    closeness = 48,
                    yearsNeglected = 1,
                    priority = RelationshipPriority.STANDARD,
                    gender = CharacterGender.MALE,
                    category = BondCategory.WORK
                )
            )
        )

        var lastPriorityRelId: String? = null
        var lastPriority: RelationshipPriority? = null
        var boostClicks = 0
        var lastQuickRelId: String? = null
        var lastQuickAction: String? = null

        composeTestRule.setContent {
            MaterialTheme {
                RelationshipSettingsScreen(
                    life = life,
                    onSetPriority = { relId, prio ->
                        lastPriorityRelId = relId
                        lastPriority = prio
                    },
                    onRunPriorityBoost = { boostClicks++ },
                    onQuickInteract = { relId, action ->
                        lastQuickRelId = relId
                        lastQuickAction = action
                    }
                )
            }
        }
        composeTestRule.waitForIdle()

        // Summary card & primary boost button render with prioritized counts
        composeTestRule.onNodeWithTag("relationship_settings_summary_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active Social Tie Priorities").assertIsDisplayed()
        composeTestRule.onNodeWithTag("run_priority_boost_button").performClick()
        assertEquals(1, boostClicks)

        // Toggle Dimas Wijaya from Standard -> Interact
        composeTestRule.onNodeWithTag("relationship_settings_list")
            .performScrollToNode(hasTestTag("rel_setting_card_rel_colleague"))
        composeTestRule.onNodeWithTag("rel_priority_rel_colleague_interaction").performScrollTo().performClick()
        assertEquals("rel_colleague", lastPriorityRelId)
        assertEquals(RelationshipPriority.INTERACTION, lastPriority)

        // Trigger quick gift on Dimas Wijaya
        composeTestRule.onNodeWithTag("rel_quick_gift_rel_colleague").performScrollTo().performClick()
        assertEquals("rel_colleague", lastQuickRelId)
        assertEquals("gift", lastQuickAction)

        // Trigger quick interact on Raka Putra
        composeTestRule.onNodeWithTag("relationship_settings_list")
            .performScrollToNode(hasTestTag("rel_setting_card_rel_partner"))
        composeTestRule.onNodeWithTag("rel_quick_interact_rel_partner").performScrollTo().performClick()
        assertEquals("rel_partner", lastQuickRelId)
        assertEquals("time", lastQuickAction)

        // Filter to Prioritized only -> keeps rel_partner and hides STANDARD rel_colleague
        composeTestRule.onNodeWithTag("relationship_settings_list")
            .performScrollToNode(hasAnyDescendant(hasTestTag("rel_settings_filter_prioritized")))
        composeTestRule.onNodeWithTag("rel_settings_filter_prioritized").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("relationship_settings_list")
            .performScrollToNode(hasTestTag("rel_setting_card_rel_partner"))
        composeTestRule.onNodeWithTag("rel_setting_card_rel_partner").assertIsDisplayed()
        assertTrue(
            composeTestRule.onAllNodesWithTag("rel_setting_card_rel_colleague").fetchSemanticsNodes().isEmpty()
        )
    }
}
