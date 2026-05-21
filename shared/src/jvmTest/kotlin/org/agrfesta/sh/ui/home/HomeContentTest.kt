package org.agrfesta.sh.ui.home

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class HomeContentTest {

    @Test
    fun `should display app title`() = runComposeUiTest {
        // When
        setContent { HomeContent(uiState = HomeUiState.Success) }

        // Then
        onNodeWithText("Pikesta").assertIsDisplayed()
    }

    @Test
    fun `should display welcome message`() = runComposeUiTest {
        // When
        setContent { HomeContent(uiState = HomeUiState.Success) }

        // Then
        onNodeWithText("Benvenuto a casa").assertIsDisplayed()
    }

    @Test
    fun `should display loading indicator when state is Loading`() = runComposeUiTest {
        // When
        setContent { HomeContent(uiState = HomeUiState.Loading) }

        // Then
        onNodeWithTag("home_loading_indicator").assertIsDisplayed()
    }

    @Test
    fun `should display error message when state is Error`() = runComposeUiTest {
        // Given
        val errorMessage = "Network error"

        // When
        setContent { HomeContent(uiState = HomeUiState.Error(errorMessage)) }

        // Then
        onNodeWithText(errorMessage).assertIsDisplayed()
    }
}
