package org.agrfesta.sh.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppHeaderTest {

    @Test
    fun `should display the application logo`() = runComposeUiTest {
        // When
        setContent { AppHeader() }

        // Then
        onNodeWithTag("app_header_logo").assertIsDisplayed()
    }
}
