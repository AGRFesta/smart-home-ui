package org.agrfesta.sh.ui.auth

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AuthContentTokenInvalidTest {

    @Test
    fun `should display token invalid banner when tokenInvalid is true`() = runComposeUiTest {
        // Given
        val tokenInvalid = true

        // When
        setContent { AuthContent(onTokenSaved = {}, tokenInvalid = tokenInvalid) }

        // Then
        onNodeWithTag("auth_token_invalid_banner").assertIsDisplayed()
    }

    @Test
    fun `should not display token invalid banner when tokenInvalid is false`() = runComposeUiTest {
        // When
        setContent { AuthContent(onTokenSaved = {}, tokenInvalid = false) }

        // Then
        onNodeWithTag("auth_token_invalid_banner").assertDoesNotExist()
    }
}

@OptIn(ExperimentalTestApi::class)
class AuthContentTest {

    @Test
    fun `should display token text field`() = runComposeUiTest {
        // When
        setContent { AuthContent(onTokenSaved = {}) }

        // Then
        onNodeWithTag("auth_token_field").assertIsDisplayed()
    }

    @Test
    fun `should display save button`() = runComposeUiTest {
        // When
        setContent { AuthContent(onTokenSaved = {}) }

        // Then
        onNodeWithTag("auth_save_button").assertIsDisplayed()
    }

    @Test
    fun `should call onTokenSaved with token when save button clicked with non-empty token`() = runComposeUiTest {
        // Given
        var savedToken: String? = null
        setContent { AuthContent(onTokenSaved = { savedToken = it }) }

        // When
        onNodeWithTag("auth_token_field").performTextInput("my-token")
        onNodeWithTag("auth_save_button").performClick()

        // Then
        savedToken shouldBe "my-token"
    }

    @Test
    fun `should call onTokenSaved with trimmed token when input has surrounding whitespace`() = runComposeUiTest {
        // Given
        var savedToken: String? = null
        setContent { AuthContent(onTokenSaved = { savedToken = it }) }

        // When
        onNodeWithTag("auth_token_field").performTextInput("  my-token  ")
        onNodeWithTag("auth_save_button").performClick()

        // Then
        savedToken shouldBe "my-token"
    }

    @Test
    fun `should not call onTokenSaved when save button clicked with empty token`() = runComposeUiTest {
        // Given
        var onTokenSavedCalled = false
        setContent { AuthContent(onTokenSaved = { onTokenSavedCalled = true }) }

        // When
        onNodeWithTag("auth_save_button").performClick()

        // Then
        withClue("onTokenSaved should not be called when token is empty") {
            onTokenSavedCalled shouldBe false
        }
    }
}
