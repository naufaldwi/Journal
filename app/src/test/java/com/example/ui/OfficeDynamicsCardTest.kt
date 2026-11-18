package com.example.ui

import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.engine.LifeEngine
import com.example.model.AvatarAppearance
import com.example.model.BondCategory
import com.example.model.CareerTrack
import com.example.model.CharacterGender
import com.example.model.LifeStats
import com.example.model.Relationship
import com.example.model.TemperamentTrait
import com.example.ui.components.OfficeProfessionalDynamicCard
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class OfficeDynamicsCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun officeDynamicCard_displaysProfessionalBondsAndLifeMiserableActiveIndicator() {
        var lastInteraction: Pair<String, String>? = null
        var meetCalled = false

        val life = LifeEngine.createNewLife(
            characterName = "Dimas Nugroho",
            birthplace = "Jakarta, Indonesia",
            birthYear = 1998,
            pronouns = "he/him",
            trait = TemperamentTrait.AMBITIOUS,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(),
            gender = CharacterGender.MALE
        ).updatedLife.copy(
            currentAge = 27,
            careerTrack = CareerTrack.LAW,
            careerRung = 1,
            relationships = listOf(
                Relationship(
                    id = "col_1",
                    name = "Raka Putra",
                    role = "Office Colleague",
                    age = 28,
                    closeness = 14,
                    gender = CharacterGender.MALE,
                    category = BondCategory.WORK
                ),
                Relationship(
                    id = "mgr_1",
                    name = "Sari Dewi",
                    role = "Team Manager",
                    age = 36,
                    closeness = 19,
                    gender = CharacterGender.FEMALE,
                    category = BondCategory.WORK
                ),
                Relationship(
                    id = "tch_1",
                    name = "Budi Santoso",
                    role = "Professor",
                    age = 48,
                    closeness = 21,
                    gender = CharacterGender.MALE,
                    category = BondCategory.SCHOOL
                ),
                Relationship(
                    id = "col_2",
                    name = "Putri Ayu",
                    role = "Senior Producer",
                    age = 32,
                    closeness = 74,
                    gender = CharacterGender.FEMALE,
                    category = BondCategory.WORK
                )
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                androidx.compose.foundation.layout.Column(
                    modifier = androidx.compose.ui.Modifier.verticalScroll(
                        androidx.compose.foundation.rememberScrollState()
                    )
                ) {
                    OfficeProfessionalDynamicCard(
                        life = life,
                        onInteractRelationship = { id, action -> lastInteraction = id to action },
                        onMeetProfessionalContact = { meetCalled = true }
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Office / Professional Dynamic").assertIsDisplayed()
        composeTestRule.onNodeWithTag("life_miserable_indicator").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Life Miserable Status: Active").assertIsDisplayed()
        composeTestRule.onNodeWithText("3/3 Hostile").assertIsDisplayed()

        composeTestRule.onNodeWithText("Raka Putra").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Sari Dewi").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Budi Santoso").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Putri Ayu").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithTag("office_amends_col_1").performScrollTo().performClick()
        assertEquals("col_1" to "apologize", lastInteraction)

        composeTestRule.onNodeWithTag("office_checkin_col_2").performScrollTo().performClick()
        assertEquals("col_2" to "time", lastInteraction)

        composeTestRule.onNodeWithTag("meet_professional_contact_button").performScrollTo().performClick()
        assertEquals(true, meetCalled)
    }

    @Test
    fun officeDynamicCard_displaysClearStatusWhenNoHostileBonds() {
        val life = LifeEngine.createNewLife(
            characterName = "Putri Lestari",
            birthplace = "Bandung, Indonesia",
            birthYear = 2001,
            pronouns = "she/her",
            trait = TemperamentTrait.ARTISTIC,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(),
            gender = CharacterGender.FEMALE
        ).updatedLife.copy(
            currentAge = 24,
            careerTrack = CareerTrack.BAND_GUITARIST,
            relationships = listOf(
                Relationship(
                    id = "mgr_ok",
                    name = "Laras Kusuma",
                    role = "Studio Producer",
                    age = 35,
                    closeness = 80,
                    gender = CharacterGender.FEMALE,
                    category = BondCategory.WORK
                )
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                OfficeProfessionalDynamicCard(
                    life = life,
                    onInteractRelationship = { _, _ -> },
                    onMeetProfessionalContact = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Life Miserable Status: Clear").assertIsDisplayed()
        composeTestRule.onNodeWithText("0/3 Hostile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Promotion: +65%").assertIsDisplayed()
    }
}
