package org.agrfesta.sh.ui.api

import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class KtorHomeApiClientTest {

    @Test
    fun `fetchHome sends Authorization Bearer token header`() = runTest {
        // Given
        val token = "my-secret-token"
        var capturedAuthHeader: String? = null
        val mockEngine = MockEngine { request ->
            capturedAuthHeader = request.headers[HttpHeaders.Authorization]
            respond(content = "", status = HttpStatusCode.OK)
        }
        val sut = KtorHomeApiClient(baseUrl = "http://test", httpClient = HttpClient(mockEngine))

        // When
        sut.fetchHome(token)

        // Then
        capturedAuthHeader shouldBe "Bearer $token"
    }

    @Test
    fun `fetchHome throws RuntimeException when server responds with 500`() = runTest {
        // Given
        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.InternalServerError) }
        val sut = KtorHomeApiClient(baseUrl = "http://test", httpClient = HttpClient(mockEngine))

        // When
        val exception = assertFailsWith<RuntimeException> { sut.fetchHome("any-token") }

        // Then
        exception.message shouldBe "Unexpected status: 500 Internal Server Error"
    }

    @Test
    fun `fetchHome returns Unauthorized when server responds with 401`() = runTest {
        // Given
        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.Unauthorized) }
        val sut = KtorHomeApiClient(baseUrl = "http://test", httpClient = HttpClient(mockEngine))

        // When
        val result = sut.fetchHome("invalid-token")

        // Then
        result shouldBe HomeApiResult.Unauthorized
    }

    @Test
    fun `fetchHome returns Success when server responds with 200`() = runTest {
        // Given
        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.OK) }
        val sut = KtorHomeApiClient(baseUrl = "http://test", httpClient = HttpClient(mockEngine))

        // When
        val result = sut.fetchHome("any-token")

        // Then
        result shouldBe HomeApiResult.Success
    }
}
