package org.agrfesta.sh.ui.api

sealed class HomeApiResult {
    data object Success : HomeApiResult()
    data object Unauthorized : HomeApiResult()
}
