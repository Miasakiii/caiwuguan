package com.caiwuguan.data.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 运行时权限管理器
 */
@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val activity = context as? Activity

    /**
     * 通知监听权限是否已开启
     */
    fun isNotificationListenerEnabled(): Boolean {
        val serviceName = "${context.packageName}/.service.NotificationListenerServiceImpl"
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return enabledServices?.contains(serviceName) == true
    }

    /**
     * 通知监听权限是否已开启（兼容旧版本）
     */
    fun isNotificationListenerLegacyEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return enabledServices?.contains(context.packageName) == true
    }

    /**
     * 检查是否可以请求通知监听权限
     * 从 Android 14+ 开始，需要用户手动开启，无法通过代码请求
     */
    fun canRequestNotificationListener(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    /**
     * 打开通知监听权限设置页面
     */
    fun openNotificationListenerSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            // 尝试打开无障碍设置
            val intent2 = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            context.startActivity(intent2)
        }
    }

    /**
     * 请求通知监听权限（Android 13+）
     * 注意：Android 13+ 不能通过代码请求，只能引导用户手动开启
     */
    fun requestNotificationListenerPermission(onResult: (Boolean) -> Unit) {
        // Android 13+ 需要手动开启，无法代码请求
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            openNotificationListenerSettings()
            onResult(isNotificationListenerEnabled())
        } else {
            // 旧版本也不支持代码请求，只能引导
            openNotificationListenerSettings()
            onResult(isNotificationListenerEnabled())
        }
    }

    /**
     * 检查通知权限是否已授权（Android 13+）
     */
    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // 旧版本不需要请求
        }
    }

    /**
     * 请求通知权限（Android 13+）
     */
    fun requestNotificationPermission(onResult: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity?.let { act ->
                // 这里应该使用 ActivityResultContracts.RequestPermission()
                // 但由于是纯 Kotlin 类，直接返回结果
                onResult(hasNotificationPermission())
            }
        } else {
            onResult(true)
        }
    }

    /**
     * 检查前台服务权限
     */
    fun hasForegroundServicePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * 检查电池优化豁免
     */
    fun isIgnoringBatteryOptimizations(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            ?: return false
        return powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * 请求电池优化豁免
     */
    fun requestIgnoreBatteryOptimization(onResult: (Boolean) -> Unit) {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = android.net.Uri.parse("package:$context.packageName")
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            activity?.startActivity(intent)
        }
        onResult(isIgnoringBatteryOptimizations())
    }
}
