package com.example.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.engine.LifeEngine
import com.example.model.NewsCategory
import com.example.ui.components.GlobalNewsFeedSheet
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GlobalNewsFeedSheetTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun globalNewsSheet_displaysEconomyClimateAndFiltersDispatchesByCategory() {
        val newsItems = listOf(
            LifeEngine.generateAnnualGlobalNews(2025, "Jakarta, Indonesia"),
            LifeEngine.generateAnnualGlobalNews(2024, "Jakarta, Indonesia"),
            LifeEngine.generateAnnualGlobalNews(2023, "Jakarta, Indonesia"),
            LifeEngine.generateAnnualGlobalNews(2022, "Jakarta, Indonesia")
        ).distinctBy { it.category }

        composeTestRule.setContent {
            MaterialTheme {
                GlobalNewsFeedSheet(
                    newsFeed = newsItems,
                    birthplace = "Jakarta, Indonesia",
                    currentYear = 2025,
                    economyMultiplier = 1.10f,
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Global News Wire").assertIsDisplayed()
        composeTestRule.onNodeWithText("Southeast Asia Edition · 2025").assertIsDisplayed()
        composeTestRule.onNodeWithText("Booming (+10%)").assertIsDisplayed()

        val sciHeadline = newsItems.first { it.category == NewsCategory.SCIENCE_TECH }.headline
        val warHeadline = newsItems.first { it.category == NewsCategory.WAR_GEOPOLITICS }.headline

        composeTestRule.onNodeWithText(sciHeadline).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText(warHeadline).performScrollTo().assertIsDisplayed()

        // Filter to War & Geopolitics
        composeTestRule.onNodeWithTag("news_filter_${NewsCategory.WAR_GEOPOLITICS.name}")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText(warHeadline).performScrollTo().assertIsDisplayed()
        assertTrue(composeTestRule.onAllNodesWithText(sciHeadline).fetchSemanticsNodes().isEmpty())
    }
}
