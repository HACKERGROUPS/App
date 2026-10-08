package com.ukrainealerts.map.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "alerts_settings")

enum class ViewMode { MAP, TABLE }

data class AppSettings(
    val token: String = "",
    val intervalSeconds: Int = 20,
    val soundEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val backgroundMonitoring: Boolean = false,
    val defaultView: ViewMode = ViewMode.MAP,
)

/**
 * Зберігає налаштування в DataStore — приватному сховищі застосунку
 * (недоступному іншим застосункам без root). Токен зберігається як є,
 * без додаткового шифрування: для суто особистого використання цього
 * достатньо; за бажання можна пізніше перейти на EncryptedSharedPreferences.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val INTERVAL = intPreferencesKey("interval_seconds")
        val SOUND = booleanPreferencesKey("sound_enabled")
        val NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val BACKGROUND = booleanPreferencesKey("background_monitoring")
        val VIEW = stringPreferencesKey("default_view")
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            token = prefs[Keys.TOKEN] ?: "",
            intervalSeconds = prefs[Keys.INTERVAL] ?: 20,
            soundEnabled = prefs[Keys.SOUND] ?: true,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: true,
            backgroundMonitoring = prefs[Keys.BACKGROUND] ?: false,
            defaultView = (prefs[Keys.VIEW] ?: ViewMode.MAP.name).let { stored ->
                runCatching { ViewMode.valueOf(stored) }.getOrDefault(ViewMode.MAP)
            },
        )
    }

    suspend fun setToken(value: String) = context.dataStore.edit { it[Keys.TOKEN] = value }
    suspend fun setInterval(seconds: Int) = context.dataStore.edit { it[Keys.INTERVAL] = seconds }
    suspend fun setSound(enabled: Boolean) = context.dataStore.edit { it[Keys.SOUND] = enabled }
    suspend fun setNotifications(enabled: Boolean) =
        context.dataStore.edit { it[Keys.NOTIFICATIONS] = enabled }
    suspend fun setBackgroundMonitoring(enabled: Boolean) =
        context.dataStore.edit { it[Keys.BACKGROUND] = enabled }
    suspend fun setDefaultView(view: ViewMode) = context.dataStore.edit { it[Keys.VIEW] = view.name }
}
