package org.agrfesta.sh.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.kotest.matchers.shouldBe
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
@OptIn(ExperimentalTestApi::class)
class MainActivityStartupTest {

    private val fakeApiClient = FakeHomeStreamApiClient()

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @After
    fun tearDown() {
        MainActivity.dependencyFactory = null
    }

    @Test
    fun `should not crash on startup when no token is stored`() {
        // Given
        MainActivity.dependencyFactory = {
            AppDependencies(
                tokenRepository = FakeTokenRepository(token = null),
                homeStreamApiClient = fakeApiClient,
            )
        }

        // When / Then
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.state shouldBe Lifecycle.State.RESUMED
        }
    }

    @Test
    fun `should display auth screen when no token is stored`() {
        // Given
        MainActivity.dependencyFactory = {
            AppDependencies(
                tokenRepository = FakeTokenRepository(token = null),
                homeStreamApiClient = fakeApiClient,
            )
        }

        // When / Then
        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithTag("auth_request_permission_button")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithTag("auth_request_permission_button").assertIsDisplayed()
        }
    }

    @Test
    fun `should display home screen when a token is stored`() {
        // Given
        MainActivity.dependencyFactory = {
            AppDependencies(
                tokenRepository = FakeTokenRepository(token = "test-token"),
                homeStreamApiClient = fakeApiClient,
            )
        }

        // When / Then
        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.waitUntil(timeoutMillis = 5_000) {
                composeRule.onAllNodesWithTag("home_loading_indicator")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithTag("home_loading_indicator").assertIsDisplayed()
        }
    }
}
