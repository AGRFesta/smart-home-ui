package org.agrfesta.sh.ui.auth

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.agrfesta.sh.ui.platform.TokenRepository

class AuthViewModel(
    private val tokenRepository: TokenRepository,
    private val scope: CoroutineScope,
) {
    private val _navigationEvent = MutableSharedFlow<Unit>()
    val navigationEvent: SharedFlow<Unit> = _navigationEvent.asSharedFlow()

    fun saveToken(token: String) {
        scope.launch {
            tokenRepository.saveToken(token)
            _navigationEvent.emit(Unit)
        }
    }
}
