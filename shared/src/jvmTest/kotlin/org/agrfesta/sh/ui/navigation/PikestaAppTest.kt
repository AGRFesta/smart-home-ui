package org.agrfesta.sh.ui.navigation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import io.mockk.mockk
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.agrfesta.sh.ui.auth.AuthViewModel
import org.agrfesta.sh.ui.platform.TokenRepository
import org.agrfesta.sh.ui.startup.StartupUiState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PikestaAppTest {

    private val authViewModel = AuthViewModel(
        mockk<TokenRepository>(relaxed = true),
        TestScope(UnconfinedTestDispatcher())
    )

    @Test
    fun `should show home screen when startup state is TokenPresent`() = runComposeUiTest {
        // When
        setContent { PikestaApp(uiState = StartupUiState.TokenPresent, authViewModel = authViewModel) }

        // Then
        onNodeWithText("Pikesta").assertIsDisplayed()
    }

    @Test
    fun `should show auth screen when startup state is TokenAbsent`() = runComposeUiTest {
        // When
        setContent { PikestaApp(uiState = StartupUiState.TokenAbsent, authViewModel = authViewModel) }

        // Then
        onNodeWithTag("auth_screen").assertIsDisplayed()
    }

    @Test
    fun `should show loading indicator when startup state is Loading`() = runComposeUiTest {
        // When
        setContent { PikestaApp(uiState = StartupUiState.Loading, authViewModel = authViewModel) }

        // Then
        onNodeWithTag("loading_indicator").assertIsDisplayed()
    }
}
