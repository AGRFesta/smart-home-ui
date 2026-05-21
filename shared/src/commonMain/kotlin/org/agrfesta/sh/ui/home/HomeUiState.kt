package org.agrfesta.sh.ui.home

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data object Success : HomeUiState()
    data object Unauthorized : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
