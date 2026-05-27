package com.caiwuguan.ui.settings

import androidx.lifecycle.ViewModel
import com.caiwuguan.ai.deepseek.ApiKeyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ApiKeyViewModel @Inject constructor(
    private val apiKeyManager: ApiKeyManager
) : ViewModel() {

    fun getApiKey(): String? = apiKeyManager.getApiKey()

    fun saveApiKey(apiKey: String) {
        apiKeyManager.saveApiKey(apiKey)
    }

    fun clearApiKey() {
        apiKeyManager.clearApiKey()
    }

    fun hasApiKey(): Boolean = apiKeyManager.hasApiKey()
}
