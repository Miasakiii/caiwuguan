package com.caiwuguan.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.caiwuguan.BuildConfig
import com.caiwuguan.data.parser.ParseResult
import com.caiwuguan.data.parser.ParserRegistry
import com.caiwuguan.domain.usecase.AddBillUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NotificationListenerServiceImpl : NotificationListenerService() {

    companion object {
        private const val TAG = "NLSImpl"
        private const val CHANNEL_ID = "notification_listener_channel"
    }

    @Inject lateinit var parserRegistry: ParserRegistry
    @Inject lateinit var addBillUseCase: AddBillUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var isInitialized = false

    override fun onCreate() {
        super.onCreate()
        if (!isInitialized) {
            isInitialized = true
            createNotificationChannel()
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Service initialized with Hilt injection")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        try {
            val notification = sbn?.notification ?: return
            val packageName = sbn.packageName
            val text = extractNotificationText(notification) ?: return

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Notification from $packageName: $text")
            }

            val result = parserRegistry.parse(text, packageName)

            if (result is ParseResult.Success) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Parsed as ${result.type} - ${result.amount} - ${result.merchant}")
                }

                serviceScope.launch {
                    addBillUseCase.executeFromParse(result, text, packageName)
                }
            } else if (BuildConfig.DEBUG) {
                Log.d(TAG, "Parse result: $result")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error processing notification", e)
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
    }

    private fun extractNotificationText(notification: Notification): String? {
        return notification.extras?.getCharSequence(Notification.EXTRA_TEXT)
            ?.toString()
            ?: notification.extras?.getCharSequence(Notification.EXTRA_TITLE)
            ?.toString()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "通知监听服务",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "用于监听支付通知并自动记录账单"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = super.onBind(intent)
    override fun onUnbind(intent: Intent?): Boolean = super.onUnbind(intent)
}