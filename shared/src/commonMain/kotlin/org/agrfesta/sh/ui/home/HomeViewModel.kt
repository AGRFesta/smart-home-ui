package org.agrfesta.sh.ui.home

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.agrfesta.sh.ui.api.HomeStreamApiClient
import org.agrfesta.sh.ui.api.HomeStreamEvent
import org.agrfesta.sh.ui.platform.TokenRepository

class HomeViewModel(
    private val homeStreamApiClient: HomeStreamApiClient,
    private val tokenRepository: TokenRepository,
    private val scope: CoroutineScope,
) {
    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _unauthorizedEvent = MutableSharedFlow<Unit>()
    val unauthorizedEvent: SharedFlow<Unit> = _unauthorizedEvent.asSharedFlow()

    fun connectStream() {
        scope.launch {
            val token = tokenRepository.getToken() ?: run {
                emitUnauthorized()
                return@launch
            }
            var active = true
            while (active) {
                try {
                    homeStreamApiClient.streamHome(token).collect { event ->
                        when (event) {
                            is HomeStreamEvent.Data -> _uiState.value = HomeUiState.Success(
                                data = event.homeResponse,
                                connectionState = ConnectionState.Connected
                            )
                            is HomeStreamEvent.Unauthorized -> {
                                emitUnauthorized()
                                active = false
                            }
                        }
                    }
                    if (active) transitionToReconnecting()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    if (_uiState.value is HomeUiState.Success) {
                        transitionToReconnecting()
                    } else {
                        _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
                        active = false
                    }
                }
            }
        }
    }

    companion object {
        private const val RETRY_DELAY_MS = 5_000L
    }

    private suspend fun transitionToReconnecting() {
        val current = _uiState.value
        if (current is HomeUiState.Success) {
            _uiState.value = current.copy(connectionState = ConnectionState.Reconnecting)
        }
        delay(RETRY_DELAY_MS)
    }

    private suspend fun emitUnauthorized() {
        _uiState.value = HomeUiState.Unauthorized
        _unauthorizedEvent.emit(Unit)
    }
}
