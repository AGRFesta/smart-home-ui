package org.agrfesta.sh.ui.api

sealed class HomeStreamEvent {
    data class Data(val homeResponse: HomeResponse) : HomeStreamEvent()
    data object Unauthorized : HomeStreamEvent()
}
