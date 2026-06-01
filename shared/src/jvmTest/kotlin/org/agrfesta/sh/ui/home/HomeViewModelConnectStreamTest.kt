package org.agrfesta.sh.ui.home

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.agrfesta.sh.ui.api.HomeStreamApiClient
import org.agrfesta.sh.ui.api.HomeStreamEvent
import org.agrfesta.sh.ui.platform.TokenRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

class HomeViewModelConnectStreamTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val mockStreamApiClient = mockk<HomeStreamApiClient>()
    private val mockTokenRepository = mockk<TokenRepository>()
    private lateinit var viewModel: HomeViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { mockTokenRepository.getToken() } returns "test-token"
        viewModel = HomeViewModel(mockStreamApiClient, mockTokenRepository, testScope)
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
    fun `connectStream emits Unauthorized and navigation event when token is missing`() {
        // Given
        every { mockTokenRepository.getToken() } returns null
        val events = mutableListOf<Unit>()
        val collectJob = testScope.launch { viewModel.unauthorizedEvent.collect { events.add(it) } }

        // When
        viewModel.connectStream()
        testScope.advanceUntilIdle()

        // Then
        viewModel.uiState.value shouldBe HomeUiState.Unauthorized
        withClue("Expected exactly one unauthorized navigation event") {
            events.size shouldBe 1
        }
        collectJob.cancel()
    }

    @Test
    fun `connectStream emits Unauthorized and navigation event when stream emits Unauthorized`() {
        // Given
        every { mockStreamApiClient.streamHome("test-token") } returns flowOf(HomeStreamEvent.Unauthorized)
        val events = mutableListOf<Unit>()
        val collectJob = testScope.launch { viewModel.unauthorizedEvent.collect { events.add(it) } }

        // When
        viewModel.connectStream()
        testScope.advanceUntilIdle()

        // Then
        viewModel.uiState.value shouldBe HomeUiState.Unauthorized
        withClue("Expected exactly one unauthorized navigation event") {
            events.size shouldBe 1
        }
        collectJob.cancel()
    }

    @Test
    fun `connectStream emits Error state when stream throws before any successful data`() {
        // Given
        every { mockStreamApiClient.streamHome("test-token") } returns flow {
            throw RuntimeException("Network error")
        }

        // When
        viewModel.connectStream()
        testScope.advanceUntilIdle()

        // Then
        val state = assertIs<HomeUiState.Error>(viewModel.uiState.value)
        state.message shouldBe "Network error"
    }

    @Test
    fun `connectStream stops retrying when stream emits Unauthorized after previous success`() {
        // Given
        val homeResponse = aHomeResponse()
        every { mockStreamApiClient.streamHome("test-token") } returns flow {
            emit(HomeStreamEvent.Data(homeResponse))
            emit(HomeStreamEvent.Unauthorized)
        }
        val events = mutableListOf<Unit>()
        val collectJob = testScope.launch { viewModel.unauthorizedEvent.collect { events.add(it) } }

        // When
        viewModel.connectStream()
        testScope.advanceUntilIdle()

        // Then
        viewModel.uiState.value shouldBe HomeUiState.Unauthorized
        withClue("Expected exactly one unauthorized navigation event — no retry after Unauthorized") {
            events.size shouldBe 1
        }
        collectJob.cancel()
    }

    @Test
    fun `connectStream resets connectionState to Connected on successful reconnection after drop`() {
        // Given
        val homeResponse = aHomeResponse()
        var callCount = 0
        every { mockStreamApiClient.streamHome("test-token") } answers {
            callCount++
            when (callCount) {
                1 -> flow {
                    emit(HomeStreamEvent.Data(homeResponse))
                    throw RuntimeException("Connection dropped")
                }
                else -> flow {
                    emit(HomeStreamEvent.Data(homeResponse))
                    awaitCancellation()
                }
            }
        }

        // When
        viewModel.connectStream()
        testScope.advanceTimeBy(5_000)
        testScope.advanceUntilIdle()

        // Then
        val state = assertIs<HomeUiState.Success>(viewModel.uiState.value)
        state.connectionState shouldBe ConnectionState.Connected
    }

    @Test
    fun `connectStream transitions to Reconnecting when stream completes normally after success`() {
        // Given
        val homeResponse = aHomeResponse()
        var callCount = 0
        every { mockStreamApiClient.streamHome("test-token") } answers {
            callCount++
            if (callCount == 1) flow { emit(HomeStreamEvent.Data(homeResponse)) }
            else flow { awaitCancellation() }
        }

        // When
        viewModel.connectStream()
        testScope.advanceUntilIdle()

        // Then
        val state = assertIs<HomeUiState.Success>(viewModel.uiState.value)
        state.connectionState shouldBe ConnectionState.Reconnecting
    }

    @Test
    fun `connectStream does not retry immediately when stream completes normally`() {
        // Given
        val homeResponse = aHomeResponse()
        var callCount = 0
        every { mockStreamApiClient.streamHome("test-token") } answers {
            callCount++
            if (callCount == 1) flow { emit(HomeStreamEvent.Data(homeResponse)) }
            else flow { awaitCancellation() }
        }

        // When
        viewModel.connectStream()
        testScope.advanceTimeBy(4_999)

        // Then
        withClue("Expected streamHome NOT to be retried before delay elapses after normal completion") {
            callCount shouldBe 1
        }
    }

    @Test
    fun `connectStream does not retry before delay elapses after connection drop`() {
        // Given
        val homeResponse = aHomeResponse()
        var callCount = 0
        every { mockStreamApiClient.streamHome("test-token") } answers {
            callCount++
            if (callCount == 1) flow {
                emit(HomeStreamEvent.Data(homeResponse))
                throw RuntimeException("Connection dropped")
            } else flow { awaitCancellation() }
        }

        // When
        viewModel.connectStream()
        testScope.advanceTimeBy(4_999)

        // Then
        withClue("Expected streamHome NOT to be retried before delay elapses") {
            callCount shouldBe 1
        }
    }

    @Test
    fun `connectStream retries stream after connection drop`() {
        // Given
        val homeResponse = aHomeResponse()
        var callCount = 0
        every { mockStreamApiClient.streamHome("test-token") } answers {
            callCount++
            if (callCount == 1) flow {
                emit(HomeStreamEvent.Data(homeResponse))
                throw RuntimeException("Connection dropped")
            } else flow { awaitCancellation() }
        }

        // When
        viewModel.connectStream()
        testScope.advanceTimeBy(5_000)
        testScope.advanceUntilIdle()

        // Then
        withClue("Expected streamHome to be retried after connection drop") {
            callCount shouldBe 2
        }
    }

    @Test
    fun `connectStream keeps last data with Reconnecting connectionState when stream drops after success`() {
        // Given
        val homeResponse = aHomeResponse()
        var callCount = 0
        every { mockStreamApiClient.streamHome("test-token") } answers {
            callCount++
            if (callCount == 1) flow {
                emit(HomeStreamEvent.Data(homeResponse))
                throw RuntimeException("Connection dropped")
            } else flow { awaitCancellation() }
        }

        // When
        viewModel.connectStream()
        testScope.advanceUntilIdle()

        // Then
        val state = assertIs<HomeUiState.Success>(viewModel.uiState.value)
        state.data shouldBe homeResponse
        state.connectionState shouldBe ConnectionState.Reconnecting
    }

    @Test
    fun `connectStream emits Success with Connected connectionState when first stream event arrives`() {
        // Given
        val homeResponse = aHomeResponse()
        every { mockStreamApiClient.streamHome("test-token") } returns flow {
            emit(HomeStreamEvent.Data(homeResponse))
            awaitCancellation()
        }

        // When
        viewModel.connectStream()
        testScope.advanceUntilIdle()

        // Then
        val state = assertIs<HomeUiState.Success>(viewModel.uiState.value)
        state.data shouldBe homeResponse
        state.connectionState shouldBe ConnectionState.Connected
    }
}
