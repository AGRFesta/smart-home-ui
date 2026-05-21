package org.agrfesta.sh.ui.platform

interface TokenRepository {
    fun hasToken(): Boolean
    fun getToken(): String?
    fun saveToken(token: String)
}
