package com.example.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.model.DailyEventCategory
import com.example.model.DailyEventChoice
import com.example.model.DailyRandomEvent
import com.example.model.TemperamentTrait
import com.example.ui.components.DailyRandomEventDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DailyRandomEventDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dailyRandomEventDialog_displaysTraitCategoryNarrativeAndDispatchesChoiceOrDismiss() {
        val event = DailyRandomEvent(
            id = "daily_2026-11-17_cur_book",
            dateKey = "2026-11-17",
            trait = TemperamentTrait.CURIOUS,
            category = DailyEventCategory.MONEY_FIND,
            title = "Folded Bills Inside a Used Book",
            narrative = "While browsing a corner bookstall in Jakarta, you uncovered $140 tucked inside a classic paperback.",
            isPositiveTone = true,
            choices = listOf(
                DailyEventChoice(
                    id = "keep_book_cash",
                    label = "Pocket the Windfall & Read",
                    effectHint = "+$140 Cash, +6 Happiness, +4 Intellect",
                    outcomeSummary = "Kept the tucked-away $140 and spent an inspiring afternoon reading.",
                    cashDelta = 140,
                    happinessDelta = 6,
                    intellectDelta = 4
                ),
                DailyEventChoice(
                    id = "leave_at_counter",
                    label = "Hand It to the Shopkeeper",
                    effectHint = "+8 Karma, +5 Happiness, +3 Intellect",
                    outcomeSummary = "Handed the $140 to the stall owner.",
                    happinessDelta = 5,
                    intellectDelta = 3,
                    karmaDelta = 8
                )
            )
        )

        var selectedChoiceId: String? = null
        var dismissed = false

        composeTestRule.setContent {
            MaterialTheme {
                DailyRandomEventDialog(
                    event = event,
                    onSelectChoice = { selectedChoiceId = it.id },
                    onDismiss = { dismissed = true }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("daily_random_event_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("DAILY RANDOM EVENT").assertIsDisplayed()
        composeTestRule.onNodeWithText("Curious Trait").assertIsDisplayed()
        composeTestRule.onNodeWithText("Money Find").assertIsDisplayed()
        composeTestRule.onNodeWithText("Folded Bills Inside a Used Book").assertIsDisplayed()

        composeTestRule.onNodeWithTag("daily_event_choice_keep_book_cash").performClick()
        assertEquals("keep_book_cash", selectedChoiceId)

        composeTestRule.onNodeWithTag("daily_event_dismiss_button").performClick()
        assertTrue(dismissed)
    }
}
