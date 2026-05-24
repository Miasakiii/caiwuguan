package com.caiwuguan.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat

class KeepAliveService : Service() {

    companion object {
        private const val TAG = "KeepAliveService"
        private const val ONGOING_NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "keep_alive_channel"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "KeepAliveService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createForegroundNotification()
        ServiceCompat.startForeground(
            this,
            ONGOING_NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
        )
        Log.d(TAG, "KeepAliveService started")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "KeepAliveService destroyed")
    }

    private fun createForegroundNotification(): Notification {
        val builder = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("财务官")
            .setContentText("正在自动监听支付通知")
            .setSmallIcon(android.R.drawable.ic_menu_send)

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "后台保活服务",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "财务官后台保活通知"
            }
            val manager = getSystemService(android.app.NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }
}
