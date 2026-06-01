package org.agrfesta.sh.ui.home

import org.agrfesta.sh.ui.api.HomeResponse

enum class ConnectionState { Connected, Reconnecting }

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(
        val data: HomeResponse,
        val connectionState: ConnectionState = ConnectionState.Connected,
    ) : HomeUiState()
    data object Unauthorized : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
