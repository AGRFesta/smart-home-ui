package org.agrfesta.sh.ui.startup

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.agrfesta.sh.ui.platform.TokenRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class StartupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val tokenRepository = mockk<TokenRepository>()
    private lateinit var viewModel: StartupViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = StartupViewModel(tokenRepository, testScope)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is loading`() {
        // Then
        viewModel.uiState.value shouldBe StartupUiState.Loading
    }

    @Test
    fun `checkToken emits TokenPresent when repository has token`() {
        // Given
        every { tokenRepository.hasToken() } returns true

        // When
        viewModel.checkToken()
        testScope.advanceUntilIdle()

        // Then
        viewModel.uiState.value shouldBe StartupUiState.TokenPresent
    }

    @Test
    fun `checkToken emits TokenAbsent when repository has no token`() {
        // Given
        every { tokenRepository.hasToken() } returns false

        // When
        viewModel.checkToken()
        testScope.advanceUntilIdle()

        // Then
        viewModel.uiState.value shouldBe StartupUiState.TokenAbsent
    }
}
