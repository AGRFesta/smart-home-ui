package org.agrfesta.sh.ui.navigation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppNavGraphTest {

    @Test
    fun `should show home screen when starting on home route`() = runComposeUiTest {
        // When
        setContent { AppNavGraph() }

        // Then
        onNodeWithText("Pikesta").assertIsDisplayed()
    }

    @Test
    fun `should show auth screen when starting on auth route`() = runComposeUiTest {
        // When
        setContent { AppNavGraph(startDestination = Routes.AUTH) }

        // Then
        onNodeWithText("Accesso").assertIsDisplayed()
    }
}
