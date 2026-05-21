package org.agrfesta.sh.ui.startup

sealed class StartupUiState {
    data object Loading : StartupUiState()
    data object TokenPresent : StartupUiState()
    data object TokenAbsent : StartupUiState()
}
