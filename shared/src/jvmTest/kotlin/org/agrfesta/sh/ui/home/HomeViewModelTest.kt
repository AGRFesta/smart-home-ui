package org.agrfesta.sh.ui.home

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.agrfesta.sh.ui.api.HomeApiClient
import org.agrfesta.sh.ui.api.HomeApiResult
import org.agrfesta.sh.ui.platform.TokenRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val mockApiClient = mockk<HomeApiClient>()
    private val mockTokenRepository = mockk<TokenRepository>()
    private val token = "test-token"
    private lateinit var viewModel: HomeViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { mockTokenRepository.getToken() } returns token
        viewModel = HomeViewModel(mockApiClient, mockTokenRepository, testScope)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() {
        // Then
        viewModel.uiState.value shouldBe HomeUiState.Loading
    }

    @Test
    fun `loadHome emits Success when client returns success`() {
        // Given
        coEvery { mockApiClient.fetchHome(token) } returns HomeApiResult.Success

        // When
        viewModel.loadHome()
        testScope.advanceUntilIdle()

        // Then
        viewModel.uiState.value shouldBe HomeUiState.Success
    }

    @Test
    fun `loadHome emits Unauthorized when client returns unauthorized`() {
        // Given
        coEvery { mockApiClient.fetchHome(token) } returns HomeApiResult.Unauthorized

        // When
        viewModel.loadHome()
        testScope.advanceUntilIdle()

        // Then
        viewModel.uiState.value shouldBe HomeUiState.Unauthorized
    }

    @Test
    fun `loadHome emits Error when client throws an exception`() {
        // Given
        coEvery { mockApiClient.fetchHome(token) } throws RuntimeException("Network error")

        // When
        viewModel.loadHome()
        testScope.advanceUntilIdle()

        // Then
        val state = assertIs<HomeUiState.Error>(viewModel.uiState.value)
        state.message shouldBe "Network error"
    }

    @Test
    fun `loadHome emits navigation event when client returns unauthorized`() {
        // Given
        coEvery { mockApiClient.fetchHome(token) } returns HomeApiResult.Unauthorized
        val events = mutableListOf<Unit>()
        val collectJob = testScope.launch { viewModel.unauthorizedEvent.collect { events.add(it) } }

        // When
        viewModel.loadHome()
        testScope.advanceUntilIdle()

        // Then
        withClue("Expected exactly one unauthorized navigation event to be emitted") {
            events.size shouldBe 1
        }
        collectJob.cancel()
    }

    @Test
    fun `loadHome does not emit navigation event when client throws an exception`() {
        // Given
        coEvery { mockApiClient.fetchHome(token) } throws RuntimeException("Network error")
        val events = mutableListOf<Unit>()
        val collectJob = testScope.launch { viewModel.unauthorizedEvent.collect { events.add(it) } }

        // When
        viewModel.loadHome()
        testScope.advanceUntilIdle()

        // Then
        withClue("Expected no unauthorized navigation event to be emitted on error") {
            events.size shouldBe 0
        }
        collectJob.cancel()
    }
}
