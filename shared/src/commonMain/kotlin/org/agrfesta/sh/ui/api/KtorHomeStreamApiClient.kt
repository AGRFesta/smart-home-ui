package org.agrfesta.sh.ui.api

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
class KtorHomeStreamApiClient(
    private val baseUrl: String,
    private val httpClient: HttpClient = defaultStreamingHttpClient(),
) : HomeStreamApiClient {
    override fun streamHome(token: String): Flow<HomeStreamEvent> = channelFlow {
        httpClient.prepareGet("${baseUrl.removeSuffix("/")}/home/stream") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.Accept, "text/event-stream")
        }.execute { response ->
            when (response.status) {
                HttpStatusCode.Unauthorized -> send(HomeStreamEvent.Unauthorized)
                HttpStatusCode.OK -> {
                    val channel = response.bodyAsChannel()
                    while (true) {
                        val line = channel.readUTF8Line() ?: break
                        if (line.startsWith("data:")) {
                            send(HomeStreamEvent.Data(lenientJson.decodeFromString(line.removePrefix("data:").removePrefix(" "))))
                        }
                    }
                }
                else -> throw RuntimeException("Unexpected status: ${response.status}")
            }
        }
    }
}
