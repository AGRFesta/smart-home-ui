package org.agrfesta.sh.ui.navigation

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.agrfesta.sh.ui.api.HomeStreamApiClient
import org.agrfesta.sh.ui.api.HomeStreamEvent
import org.agrfesta.sh.ui.auth.AuthViewModel
import org.agrfesta.sh.ui.home.HomeViewModel
import org.agrfesta.sh.ui.platform.TokenRepository
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AppNavGraphTest {

    private val authViewModel = AuthViewModel(
        mockk<TokenRepository>(relaxed = true),
        TestScope(UnconfinedTestDispatcher())
    )
    private val homeViewModel = HomeViewModel(
        mockk<HomeStreamApiClient>().also { every { it.streamHome(any()) } returns flow { awaitCancellation() } },
        mockk<TokenRepository>().also { every { it.getToken() } returns "test-token" },
        TestScope(UnconfinedTestDispatcher())
    )

    @Test
    fun `should show home screen when starting on home route`() = runComposeUiTest {
        // When
        setContent { AppNavGraph(authViewModel = authViewModel, homeViewModel = homeViewModel) }

        // Then
        onNodeWithTag("home_loading_indicator").assertIsDisplayed()
    }

    @Test
    fun `should show auth screen when starting on auth route`() = runComposeUiTest {
        // When
        setContent { AppNavGraph(startDestination = Routes.AUTH, authViewModel = authViewModel, homeViewModel = homeViewModel) }

        // Then
        onNodeWithTag("auth_screen").assertIsDisplayed()
    }

    @Test
    fun `should navigate to auth screen when home view model emits unauthorized event`() = runComposeUiTest {
        // Given
        val unauthorizedStreamApiClient = mockk<HomeStreamApiClient>()
        every { unauthorizedStreamApiClient.streamHome(any()) } returns flowOf(HomeStreamEvent.Unauthorized)
        val unauthorizedHomeViewModel = HomeViewModel(
            unauthorizedStreamApiClient,
            mockk<TokenRepository>().also { every { it.getToken() } returns "test-token" },
            TestScope(UnconfinedTestDispatcher())
        )
        setContent { AppNavGraph(authViewModel = authViewModel, homeViewModel = unauthorizedHomeViewModel) }

        // When
        unauthorizedHomeViewModel.connectStream()

        // Then
        onNodeWithTag("auth_screen").assertIsDisplayed()
    }
}
