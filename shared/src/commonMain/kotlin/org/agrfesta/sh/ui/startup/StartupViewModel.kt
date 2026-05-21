package org.agrfesta.sh.ui.startup

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.agrfesta.sh.ui.platform.TokenRepository

class StartupViewModel(
    private val tokenRepository: TokenRepository,
    private val scope: CoroutineScope,
) {
    private val _uiState = MutableStateFlow<StartupUiState>(StartupUiState.Loading)
    val uiState: StateFlow<StartupUiState> = _uiState.asStateFlow()

    fun checkToken() {
        scope.launch {
            _uiState.value = if (tokenRepository.hasToken()) {
                StartupUiState.TokenPresent
            } else {
                StartupUiState.TokenAbsent
            }
        }
    }
}
