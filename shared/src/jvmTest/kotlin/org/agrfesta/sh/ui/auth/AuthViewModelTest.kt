package org.agrfesta.sh.ui.auth

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.agrfesta.sh.ui.platform.TokenRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val tokenRepository = mockk<TokenRepository>()
    private lateinit var viewModel: AuthViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AuthViewModel(tokenRepository, testScope)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveToken calls repository saveToken with given token`() {
        // Given
        val token = "my-secret-token"
        every { tokenRepository.saveToken(token) } just runs

        // When
        viewModel.saveToken(token)
        testScope.advanceUntilIdle()

        // Then
        verify { tokenRepository.saveToken(token) }
    }

    @Test
    fun `saveToken emits navigation event after saving`() {
        // Given
        val token = "my-secret-token"
        every { tokenRepository.saveToken(any()) } just runs
        var eventEmitted = false
        val job = testScope.launch { viewModel.navigationEvent.collect { eventEmitted = true } }

        // When
        viewModel.saveToken(token)
        testScope.advanceUntilIdle()

        // Then
        withClue("navigationEvent should be emitted after saveToken") {
            eventEmitted shouldBe true
        }
        job.cancel()
    }
}
