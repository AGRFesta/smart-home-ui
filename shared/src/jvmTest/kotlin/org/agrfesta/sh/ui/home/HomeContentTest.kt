package org.agrfesta.sh.ui.home

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class HomeContentTest {

    @Test
    fun `should display app title`() = runComposeUiTest {
        // When
        setContent { HomeContent() }

        // Then
        onNodeWithText("Pikesta").assertIsDisplayed()
    }

    @Test
    fun `should display welcome message`() = runComposeUiTest {
        // When
        setContent { HomeContent() }

        // Then
        onNodeWithText("Benvenuto a casa").assertIsDisplayed()
    }
}
