package org.agrfesta.sh.ui.api

interface HomeApiClient {
    suspend fun fetchHome(token: String): HomeApiResult
}
