package org.agrfesta.sh.ui.api

import kotlinx.coroutines.flow.Flow

interface HomeStreamApiClient {
    fun streamHome(token: String): Flow<HomeStreamEvent>
}
