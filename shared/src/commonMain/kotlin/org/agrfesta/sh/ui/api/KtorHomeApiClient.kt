package org.agrfesta.sh.ui.api

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json

internal fun defaultHttpClient() = HttpClient {
    install(ContentNegotiation) {
        json()
    }
}

class KtorHomeApiClient(
    private val baseUrl: String,
    private val httpClient: HttpClient = defaultHttpClient(),
) : HomeApiClient {
    override suspend fun fetchHome(token: String): HomeApiResult {
        return httpClient.prepareGet("${baseUrl.removeSuffix("/")}/home") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.execute { response ->
            when (response.status) {
                HttpStatusCode.OK -> HomeApiResult.Success
                HttpStatusCode.Unauthorized -> HomeApiResult.Unauthorized
                else -> throw RuntimeException("Unexpected status: ${response.status}")
            }
        }
    }
}
