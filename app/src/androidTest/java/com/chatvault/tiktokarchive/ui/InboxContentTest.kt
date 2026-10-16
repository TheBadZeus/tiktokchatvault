package com.chatvault.tiktokarchive.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.chatvault.tiktokarchive.data.dao.InboxRow
import com.chatvault.tiktokarchive.domain.model.ThemeMode
import com.chatvault.tiktokarchive.ui.inbox.InboxContent
import com.chatvault.tiktokarchive.ui.inbox.InboxState
import com.chatvault.tiktokarchive.ui.theme.ChatVaultTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class InboxContentTest {
    @get:Rule val rule = createComposeRule()

    private fun show(state: InboxState, onOpen: (String) -> Unit = {}, onDemo: () -> Unit = {}) {
        rule.setContent {
            ChatVaultTheme(ThemeMode.LIGHT) {
                InboxContent(state, "", {}, onOpen, {}, {}, onDemo)
            }
        }
    }

    @Test
    fun rowIsShownAndClickable() {
        var opened: String? = null
        val row = InboxRow("c1", "maya.demo", "hey there", 1_700_000_000_000, 2, isDemo = true, hasStarred = false)
        show(InboxState.Content(listOf(row)), onOpen = { opened = it })
        rule.onNodeWithText("maya.demo").assertIsDisplayed()
        rule.onNodeWithText("hey there").assertIsDisplayed()
        rule.onNodeWithText("maya.demo").performClick()
        assertEquals("c1", opened)
    }

    @Test
    fun emptyStateExplainsNothingIsCapturedAutomatically() {
        show(InboxState.Content(emptyList()))
        rule.onNodeWithText("No archived conversations yet").assertIsDisplayed()
    }

    @Test
    fun errorStateOffersRetry() {
        show(InboxState.Error("disk problem"))
        rule.onNodeWithText("Try again").assertIsDisplayed()
    }
}
