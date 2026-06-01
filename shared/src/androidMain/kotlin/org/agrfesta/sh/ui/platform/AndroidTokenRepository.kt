package org.agrfesta.sh.ui.platform

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AndroidTokenRepository(context: Context) : TokenRepository {

    private val appContext = context.applicationContext

    private val prefs = try {
        createPrefs(appContext)
    } catch (e: Exception) {
        // Keyset or MasterKey incompatible/corrupted (e.g. tink upgrade, keystore invalidation).
        // Wipe prefs and evict the MasterKey entry, then recreate — user will need to re-authenticate.
        appContext.deleteSharedPreferences(PREFS_NAME)
        try {
            java.security.KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
                deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            }
        } catch (_: Exception) {}
        createPrefs(appContext)
    }

    override fun hasToken(): Boolean = prefs.contains(KEY_TOKEN)

    override fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    override fun saveToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()

    companion object {
        private const val PREFS_NAME = "pikesta_secure_prefs"
        private const val KEY_TOKEN = "auth_token"

        private fun createPrefs(appContext: Context) = EncryptedSharedPreferences.create(
            appContext,
            PREFS_NAME,
            MasterKey.Builder(appContext).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
