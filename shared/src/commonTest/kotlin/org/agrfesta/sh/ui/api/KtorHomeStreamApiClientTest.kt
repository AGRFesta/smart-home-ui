package org.agrfesta.sh.ui.api

import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class KtorHomeStreamApiClientTest {

    @Test
    fun `streamHome sends Authorization Bearer token header`() = runTest {
        // Given
        val token = "my-secret-token"
        var capturedAuthHeader: String? = null
        val mockEngine = MockEngine { request ->
            capturedAuthHeader = request.headers[HttpHeaders.Authorization]
            respond(
                content = "",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/event-stream")
            )
        }
        val sut = testClient(mockEngine)

        // When
        sut.streamHome(token).collect {}

        // Then
        capturedAuthHeader shouldBe "Bearer $token"
    }

    @Test
    fun `streamHome sends Accept text_event-stream header`() = runTest {
        // Given
        var capturedAcceptHeader: String? = null
        val mockEngine = MockEngine { request ->
            capturedAcceptHeader = request.headers[HttpHeaders.Accept]
            respond(
                content = "",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/event-stream")
            )
        }
        val sut = testClient(mockEngine)

        // When
        sut.streamHome("any-token").collect {}

        // Then
        capturedAcceptHeader shouldBe "text/event-stream"
    }

    @Test
    fun `streamHome emits Data event with parsed HomeResponse for a received SSE data event`() = runTest {
        // Given
        val responseJson = """{"globalState":{"heatingActive":{"type":"success","value":true},"strategy":{"type":"success","value":"COMFORT"}},"areas":[]}"""
        val sseBody = "data:$responseJson\n\n"
        val mockEngine = MockEngine { _ ->
            respond(
                content = sseBody,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/event-stream")
            )
        }
        val sut = testClient(mockEngine)

        // When
        val events = mutableListOf<HomeStreamEvent>()
        sut.streamHome("any-token").collect { events.add(it) }

        // Then
        withClue("Expected exactly one HomeStreamEvent.Data to be emitted") {
            events.size shouldBe 1
        }
        val event = assertIs<HomeStreamEvent.Data>(events.first())
        event.homeResponse shouldBe HomeResponse(
            globalState = GlobalState(
                heatingActive = FieldResult.Success(true),
                strategy = FieldResult.Success("COMFORT")
            ),
            areas = emptyList()
        )
    }

    @Test
    fun `streamHome emits Data event for an area without sensors whose measurement keys are omitted`() = runTest {
        // Given
        val responseJson =
            """{"globalState":{"heatingActive":{"type":"success","value":true},"strategy":{"type":"success","value":"COMFORT"}},""" +
            """"areas":[{"id":"area-no-sensors","name":"Storage Room","measurements":{}}]}"""
        val sseBody = "data:$responseJson\n\n"
        val mockEngine = MockEngine { _ ->
            respond(
                content = sseBody,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/event-stream")
            )
        }
        val sut = testClient(mockEngine)

        // When
        val events = mutableListOf<HomeStreamEvent>()
        sut.streamHome("any-token").collect { events.add(it) }

        // Then
        withClue("Expected exactly one HomeStreamEvent.Data to be emitted") {
            events.size shouldBe 1
        }
        val event = assertIs<HomeStreamEvent.Data>(events.first())
        event.homeResponse.areas shouldBe listOf(
            Area(
                id = "area-no-sensors",
                name = "Storage Room",
                measurements = AreaMeasurements(heating = null, humidity = null)
            )
        )
    }

    @Test
    fun `streamHome emits multiple Data events for multiple consecutive SSE events`() = runTest {
        // Given
        val json1 = """{"globalState":{"heatingActive":{"type":"success","value":true},"strategy":{"type":"success","value":"COMFORT"}},"areas":[]}"""
        val json2 = """{"globalState":{"heatingActive":{"type":"success","value":false},"strategy":{"type":"success","value":"ECO"}},"areas":[]}"""
        val sseBody = "data:$json1\n\ndata:$json2\n\n"
        val mockEngine = MockEngine { _ ->
            respond(
                content = sseBody,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "text/event-stream")
            )
        }
        val sut = testClient(mockEngine)

        // When
        val events = mutableListOf<HomeStreamEvent>()
        sut.streamHome("any-token").collect { events.add(it) }

        // Then
        withClue("Expected exactly two HomeStreamEvent.Data events to be emitted") {
            events.size shouldBe 2
        }
        assertIs<HomeStreamEvent.Data>(events[0])
        assertIs<HomeStreamEvent.Data>(events[1])
    }

    @Test
    fun `streamHome throws RuntimeException when server responds with unexpected status`() = runTest {
        // Given
        val mockEngine = MockEngine { _ ->
            respond(content = "", status = HttpStatusCode.InternalServerError)
        }
        val sut = testClient(mockEngine)

        // When
        val exception = assertFailsWith<RuntimeException> {
            sut.streamHome("any-token").collect {}
        }

        // Then
        exception.message shouldBe "Unexpected status: 500 Internal Server Error"
    }

    @Test
    fun `streamHome emits Unauthorized event when server responds with 401`() = runTest {
        // Given
        val mockEngine = MockEngine { _ ->
            respond(content = "", status = HttpStatusCode.Unauthorized)
        }
        val sut = testClient(mockEngine)

        // When
        val events = mutableListOf<HomeStreamEvent>()
        sut.streamHome("invalid-token").collect { events.add(it) }

        // Then
        withClue("Expected exactly one HomeStreamEvent.Unauthorized to be emitted") {
            events.size shouldBe 1
        }
        assertIs<HomeStreamEvent.Unauthorized>(events.first())
    }
}

private fun testClient(engine: MockEngine) = KtorHomeStreamApiClient(
    baseUrl = "http://test",
    httpClient = HttpClient(engine)
)
