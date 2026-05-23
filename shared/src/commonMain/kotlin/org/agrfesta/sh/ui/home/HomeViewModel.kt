package org.agrfesta.sh.ui.home

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.agrfesta.sh.ui.api.HomeApiClient
import org.agrfesta.sh.ui.api.HomeApiResult
import org.agrfesta.sh.ui.platform.TokenRepository

class HomeViewModel(
    private val homeApiClient: HomeApiClient,
    private val tokenRepository: TokenRepository,
    private val scope: CoroutineScope,
) {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _unauthorizedEvent = MutableSharedFlow<Unit>()
    val unauthorizedEvent: SharedFlow<Unit> = _unauthorizedEvent.asSharedFlow()

    fun loadHome() {
        scope.launch {
            val token = tokenRepository.getToken() ?: run {
                _uiState.value = HomeUiState.Unauthorized
                _unauthorizedEvent.emit(Unit)
                return@launch
            }
            try {
                val fetchResult = homeApiClient.fetchHome(token)
                when (fetchResult) {
                    is HomeApiResult.Success -> _uiState.value = HomeUiState.Success(data = fetchResult.data)
                    HomeApiResult.Unauthorized -> {
                        _uiState.value = HomeUiState.Unauthorized
                        _unauthorizedEvent.emit(Unit)
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
