package com.caiwuguan.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "app_prefs")

@Singleton
class UserPrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATION_LISTENER_ENABLED = booleanPreferencesKey("notification_listener_enabled")
        val AUTO_RECORD_ENABLED = booleanPreferencesKey("auto_record_enabled")
        val FIRST_LAUNCH = booleanPreferencesKey("first_launch")
    }

    val themeMode: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "system"
    }

    val notificationListenerEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[NOTIFICATION_LISTENER_ENABLED] ?: false
    }

    val autoRecordEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_RECORD_ENABLED] ?: true
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[FIRST_LAUNCH] ?: true
    }

    suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { preferences ->
            preferences[FIRST_LAUNCH] = false
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    suspend fun setAutoRecordEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_RECORD_ENABLED] = enabled
        }
    }
}
