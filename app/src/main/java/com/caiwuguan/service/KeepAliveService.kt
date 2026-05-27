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
import com.caiwuguan.data.db.dao.BillDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class KeepAliveService : Service() {

    companion object {
        private const val TAG = "KeepAliveService"
        private const val ONGOING_NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "keep_alive_channel"
    }

    @Inject lateinit var billDao: BillDao

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        updateNotificationWithTodayExpense()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
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

    private fun updateNotificationWithTodayExpense() {
        serviceScope.launch {
            try {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val dayStart = calendar.timeInMillis
                val dayEnd = dayStart + 24 * 60 * 60 * 1000L

                val totalExpense = billDao.getTotalExpense(dayStart, dayEnd).first() ?: 0L
                val totalIncome = billDao.getTotalIncome(dayStart, dayEnd).first() ?: 0L

                val expenseText = "%.2f".format(totalExpense / 100.0)
                val incomeText = "%.2f".format(totalIncome / 100.0)

                val notification = Notification.Builder(this@KeepAliveService, CHANNEL_ID)
                    .setContentTitle("今日：支出 ¥$expenseText | 收入 ¥$incomeText")
                    .setContentText("正在自动监听支付通知")
                    .setSmallIcon(android.R.drawable.ic_menu_send)
                    .build()

                val manager = getSystemService(NotificationManager::class.java)
                manager?.notify(ONGOING_NOTIFICATION_ID, notification)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update notification", e)
            }
        }
    }
}
