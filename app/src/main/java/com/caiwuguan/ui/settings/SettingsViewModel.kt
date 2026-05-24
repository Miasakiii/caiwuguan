package com.caiwuguan.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caiwuguan.data.permission.PermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) : ViewModel() {

    private val _isNotificationListenerEnabled = MutableStateFlow(false)
    val isNotificationListenerEnabled: StateFlow<Boolean> = _isNotificationListenerEnabled.asStateFlow()

    private val _hasNotificationPermission = MutableStateFlow(false)
    val hasNotificationPermission: StateFlow<Boolean> = _hasNotificationPermission.asStateFlow()

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        viewModelScope.launch {
            _isNotificationListenerEnabled.value = permissionManager.isNotificationListenerEnabled()
            _hasNotificationPermission.value = permissionManager.hasNotificationPermission()
        }
    }

    fun openNotificationListenerSettings(onResult: () -> Unit = {}) {
        permissionManager.openNotificationListenerSettings()
        onResult()
    }

    fun requestNotificationPermission(onResult: (Boolean) -> Unit) {
        permissionManager.requestNotificationPermission(onResult)
    }

    fun requestIgnoreBatteryOptimization(onResult: (Boolean) -> Unit) {
        permissionManager.requestIgnoreBatteryOptimization(onResult)
    }
}
