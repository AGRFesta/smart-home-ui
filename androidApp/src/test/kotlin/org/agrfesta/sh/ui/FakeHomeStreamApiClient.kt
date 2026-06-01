package org.agrfesta.sh.ui

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.agrfesta.sh.ui.api.HomeStreamApiClient
import org.agrfesta.sh.ui.api.HomeStreamEvent

class FakeHomeStreamApiClient : HomeStreamApiClient {
    override fun streamHome(token: String): Flow<HomeStreamEvent> = emptyFlow()
}
