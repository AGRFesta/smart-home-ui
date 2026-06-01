package org.agrfesta.sh.ui

import org.agrfesta.sh.ui.api.HomeStreamApiClient
import org.agrfesta.sh.ui.platform.TokenRepository

data class AppDependencies(
    val tokenRepository: TokenRepository,
    val homeStreamApiClient: HomeStreamApiClient,
)
