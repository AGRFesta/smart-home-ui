package org.agrfesta.sh.ui.navigation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import org.agrfesta.sh.ui.startup.StartupUiState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PikestaAppTest {

    @Test
    fun `should show home screen when startup state is TokenPresent`() = runComposeUiTest {
        // When
        setContent { PikestaApp(uiState = StartupUiState.TokenPresent) }

        // Then
        onNodeWithText("Pikesta").assertIsDisplayed()
    }

    @Test
    fun `should show auth screen when startup state is TokenAbsent`() = runComposeUiTest {
        // When
        setContent { PikestaApp(uiState = StartupUiState.TokenAbsent) }

        // Then
        onNodeWithText("Accesso").assertIsDisplayed()
    }
}
