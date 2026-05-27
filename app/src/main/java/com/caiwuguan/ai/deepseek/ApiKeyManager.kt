package com.caiwuguan.ai.deepseek

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyManager @Inject constructor() {

    companion object {
        private const val PREFS_FILE_NAME = "encrypted_api_prefs"
        private const val KEY_API_KEY = "deepseek_api_key"
    }

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveApiKey(apiKey: String) {
        prefs?.edit()?.putString(KEY_API_KEY, apiKey)?.apply()
    }

    fun getApiKey(): String? {
        return prefs?.getString(KEY_API_KEY, null)
    }

    fun clearApiKey() {
        prefs?.edit()?.remove(KEY_API_KEY)?.apply()
    }

    fun hasApiKey(): Boolean {
        return !getApiKey().isNullOrBlank()
    }
}
