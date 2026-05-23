package org.agrfesta.sh.ui.api

import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class KtorHomeApiClientTest {

    @Test
    fun `fetchHome sends Authorization Bearer token header`() = runTest {
        // Given
        val token = "my-secret-token"
        var capturedAuthHeader: String? = null
        val mockEngine = MockEngine { request ->
            capturedAuthHeader = request.headers[HttpHeaders.Authorization]
            respond(content = "", status = HttpStatusCode.Unauthorized)
        }
        val sut = testClient(mockEngine)

        // When
        sut.fetchHome(token)

        // Then
        capturedAuthHeader shouldBe "Bearer $token"
    }

    @Test
    fun `fetchHome throws RuntimeException when server responds with 500`() = runTest {
        // Given
        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.InternalServerError) }
        val sut = testClient(mockEngine)

        // When
        val exception = assertFailsWith<RuntimeException> { sut.fetchHome("any-token") }

        // Then
        exception.message shouldBe "Unexpected status: 500 Internal Server Error"
    }

    @Test
    fun `fetchHome returns Unauthorized when server responds with 401`() = runTest {
        // Given
        val mockEngine = MockEngine { respond(content = "", status = HttpStatusCode.Unauthorized) }
        val sut = testClient(mockEngine)

        // When
        val result = sut.fetchHome("invalid-token")

        // Then
        result shouldBe HomeApiResult.Unauthorized
    }

    @Test
    fun `fetchHome returns Success carrying the parsed HomeResponse when server responds with 200`() = runTest {
        // Given
        val responseBody = """
            {
              "globalState": {
                "heatingActive": { "type": "success", "value": true },
                "strategy":      { "type": "success", "value": "COMFORT" }
              },
              "areas": [
                {
                  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "name": "Living Room",
                  "measurements": {
                    "heating": {
                      "currentTemperature": { "type": "success", "value": 21.5 }
                    },
                    "humidity": {
                      "relative": { "type": "success", "value": 45.5 }
                    }
                  }
                }
              ]
            }
        """.trimIndent()
        val mockEngine = MockEngine {
            respond(
                content = responseBody,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val sut = testClient(mockEngine)

        // When
        val result = sut.fetchHome("any-token")

        // Then
        val success = assertIs<HomeApiResult.Success>(result)
        success.data shouldBe HomeResponse(
            globalState = GlobalState(
                heatingActive = FieldResult.Success(true),
                strategy = FieldResult.Success("COMFORT")
            ),
            areas = listOf(
                Area(
                    id = "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    name = "Living Room",
                    measurements = AreaMeasurements(
                        heating = HeatingMeasurements(
                            currentTemperature = FieldResult.Success(21.5)
                        ),
                        humidity = HumidityMeasurements(
                            relative = FieldResult.Success(45.5)
                        )
                    )
                )
            )
        )
    }
}

private fun testClient(engine: MockEngine) = KtorHomeApiClient(
    baseUrl = "http://test",
    httpClient = HttpClient(engine) {
        install(ContentNegotiation) { json() }
    }
)
