package com.caiwuguan

import android.app.Application
import com.caiwuguan.ai.deepseek.ApiKeyManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class FinanceOfficerApp : Application() {

    @Inject
    lateinit var apiKeyManager: ApiKeyManager

    override fun onCreate() {
        super.onCreate()
        apiKeyManager.init(this)
    }
}
