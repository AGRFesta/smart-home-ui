package org.agrfesta.sh.ui.platform

import java.nio.file.Path

class DesktopTokenRepository(private val storageDir: Path) : TokenRepository {

    private val tokenFile = storageDir.resolve("token").toFile()

    override fun hasToken(): Boolean = tokenFile.exists()

    override fun getToken(): String? = if (hasToken()) tokenFile.readText() else null

    override fun saveToken(token: String) {
        storageDir.toFile().mkdirs()
        tokenFile.writeText(token)
    }
}
