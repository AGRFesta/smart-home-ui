package org.agrfesta.sh.ui

import org.agrfesta.sh.ui.platform.TokenRepository

class FakeTokenRepository(private val token: String? = null) : TokenRepository {
    override fun hasToken(): Boolean = token != null
    override fun getToken(): String? = token
    override fun saveToken(token: String) {}
}
