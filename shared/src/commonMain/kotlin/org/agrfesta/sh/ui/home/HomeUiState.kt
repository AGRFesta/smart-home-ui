package org.agrfesta.sh.ui.home

import org.agrfesta.sh.ui.api.HomeResponse

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Success(val data: HomeResponse) : HomeUiState()
    data object Unauthorized : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
