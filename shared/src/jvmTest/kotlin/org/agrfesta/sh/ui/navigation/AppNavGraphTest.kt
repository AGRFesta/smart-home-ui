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
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppNavGraphTest {

    private val authViewModel = AuthViewModel(
        mockk<TokenRepository>(relaxed = true),
        TestScope(UnconfinedTestDispatcher())
    )

    @Test
    fun `should show home screen when starting on home route`() = runComposeUiTest {
        // When
        setContent { AppNavGraph(authViewModel = authViewModel) }

        // Then
        onNodeWithText("Pikesta").assertIsDisplayed()
    }

    @Test
    fun `should show auth screen when starting on auth route`() = runComposeUiTest {
        // When
        setContent { AppNavGraph(startDestination = Routes.AUTH, authViewModel = authViewModel) }

        // Then
        onNodeWithTag("auth_screen").assertIsDisplayed()
    }
}
