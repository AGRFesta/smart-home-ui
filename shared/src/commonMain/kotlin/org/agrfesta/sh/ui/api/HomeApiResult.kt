package org.agrfesta.sh.ui.api

sealed class HomeApiResult {
    data class Success(val data: HomeResponse) : HomeApiResult()
    data object Unauthorized : HomeApiResult()
}
