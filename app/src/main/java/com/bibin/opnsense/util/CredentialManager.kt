package com.bibin.opnsense.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CredentialManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "opnsense_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    var firewallUrl: String
        get() = prefs.getString(KEY_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_URL, value).apply()

    var apiKey: String
        get() = prefs.getString(KEY_API_KEY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_KEY, value).apply()

    var apiSecret: String
        get() = prefs.getString(KEY_API_SECRET, "") ?: ""
        set(value) = prefs.edit().putString(KEY_API_SECRET, value).apply()

    val isKeySet: Boolean
        get() = apiKey.isNotBlank()

    val isConfigured: Boolean
        get() = firewallUrl.isNotBlank() && apiKey.isNotBlank() && apiSecret.isNotBlank()

    fun saveAll(url: String, key: String, secret: String) {
        firewallUrl = url.trimEnd('/')
        apiKey = key.trim()
        apiSecret = secret.trim()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_URL = "firewall_url"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_API_SECRET = "api_secret"
    }
}
