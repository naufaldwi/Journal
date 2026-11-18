package com.example.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import com.example.engine.LifeEngine
import com.example.model.AvatarAppearance
import com.example.model.CareerTrack
import com.example.model.CharacterGender
import com.example.model.LifeStats
import com.example.model.MoodSnapshot
import com.example.model.TemperamentTrait
import com.example.ui.components.MoodTrendsChartCard
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MoodTrendsChartCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun moodTrendsChartCard_displaysTenYearChartCareerLoadAndRandomEncounterImpact() {
        val history = (0..9).map { idx ->
            MoodSnapshot(
                age = 20 + idx,
                year = 2016 + idx,
                happiness = (78 - idx * 2).coerceIn(20, 100),
                stress = (24 + idx * 4).coerceIn(0, 100),
                anxiety = (18 + idx * 3).coerceIn(0, 100),
                careerStressLoad = if (idx >= 5) 14 else 9,
                hadRandomEncounter = idx == 4 || idx == 9,
                encounterLabel = when (idx) {
                    4 -> "Random Encounter: Office Breakroom"
                    9 -> "Sudden Burnout & Health Scare"
                    else -> ""
                }
            )
        }
        val life = LifeEngine.createNewLife(
            characterName = "Dimas Nugroho",
            birthplace = "Jakarta, Indonesia",
            birthYear = 1996,
            pronouns = "he/him",
            trait = TemperamentTrait.AMBITIOUS,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(happiness = 60, stress = 60, anxiety = 45),
            gender = CharacterGender.MALE
        ).updatedLife.copy(
            currentAge = 29,
            careerTrack = CareerTrack.MEDICINE,
            moodHistory = history
        )

        composeTestRule.setContent {
            MaterialTheme {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    MoodTrendsChartCard(life = life)
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("mood_trends_chart_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("10-Year Mood Trends").assertIsDisplayed()
        composeTestRule.onNodeWithText("Last 10 yrs").assertIsDisplayed()
        composeTestRule.onNodeWithTag("mood_trends_canvas").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("mood_year_inspector").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Encounter: Sudden Burnout & Health Scare").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("2 yr(s)").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun moodTrendsChartCard_displaysBaselinePromptAtAgeZero() {
        val infant = LifeEngine.createNewLife(
            characterName = "Ayu Lestari",
            birthplace = "Yogyakarta, Indonesia",
            birthYear = 2025,
            pronouns = "she/her",
            trait = TemperamentTrait.CURIOUS,
            appearance = AvatarAppearance(),
            startingStats = LifeStats(),
            gender = CharacterGender.FEMALE
        ).updatedLife

        composeTestRule.setContent {
            MaterialTheme {
                MoodTrendsChartCard(life = infant)
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Baseline").assertIsDisplayed()
        composeTestRule.onNodeWithText("Year 2025 Baseline Recorded").assertIsDisplayed()
    }
}
