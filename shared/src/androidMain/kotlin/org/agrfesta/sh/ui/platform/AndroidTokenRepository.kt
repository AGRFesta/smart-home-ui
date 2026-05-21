package org.agrfesta.sh.ui.platform

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AndroidTokenRepository(context: Context) : TokenRepository {

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "pikesta_secure_prefs",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun hasToken(): Boolean = prefs.contains(KEY_TOKEN)

    override fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    override fun saveToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()

    companion object {
        private const val KEY_TOKEN = "auth_token"
    }
}
