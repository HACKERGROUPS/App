package com.ukrainealerts.map.ui

import android.app.Application
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ukrainealerts.map.data.AlertsApi
import com.ukrainealerts.map.data.AlertsApiException
import com.ukrainealerts.map.data.AppSettings
import com.ukrainealerts.map.data.RegionStatus
import com.ukrainealerts.map.data.SettingsRepository
import com.ukrainealerts.map.data.StatusCalculator
import com.ukrainealerts.map.data.ViewMode
import com.ukrainealerts.map.service.AlertsMonitorService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class UiState(
    val statuses: Map<String, RegionStatus> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastUpdated: String = "—",
    val viewMode: ViewMode = ViewMode.MAP,
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private var pollingJob: Job? = null
    private var hasAppliedDefaultView = false

    init {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { newSettings ->
                val intervalChanged = newSettings.intervalSeconds != _settings.value.intervalSeconds
                val tokenChanged = newSettings.token != _settings.value.token
                _settings.value = newSettings

                if (!hasAppliedDefaultView) {
                    _uiState.value = _uiState.value.copy(viewMode = newSettings.defaultView)
                    hasAppliedDefaultView = true
                }

                if (pollingJob == null || intervalChanged || tokenChanged) {
                    restartPolling()
                }
            }
        }
    }

    fun setViewMode(mode: ViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = mode)
        viewModelScope.launch { settingsRepository.setDefaultView(mode) }
    }

    fun refreshNow() {
        viewModelScope.launch { refreshOnce() }
    }

    fun saveToken(value: String) = viewModelScope.launch { settingsRepository.setToken(value) }
    fun saveInterval(seconds: Int) = viewModelScope.launch { settingsRepository.setInterval(seconds) }
    fun saveSound(enabled: Boolean) = viewModelScope.launch { settingsRepository.setSound(enabled) }
    fun saveNotifications(enabled: Boolean) =
        viewModelScope.launch { settingsRepository.setNotifications(enabled) }

    fun saveBackgroundMonitoring(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setBackgroundMonitoring(enabled) }
        val context = getApplication<Application>()
        val intent = Intent(context, AlertsMonitorService::class.java)
        if (enabled) {
            ContextCompat.startForegroundService(context, intent)
        } else {
            context.stopService(intent)
        }
    }

    fun testSound() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
                toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
                delay(600)
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
            }
        }
    }

    private fun restartPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                refreshOnce()
                delay(_settings.value.intervalSeconds * 1000L)
            }
        }
    }

    private suspend fun refreshOnce() {
        val token = _settings.value.token
        _uiState.value = _uiState.value.copy(isLoading = true)
        try {
            val alerts = withContext(Dispatchers.IO) { AlertsApi(token).fetchActiveAlerts() }
            val current = StatusCalculator.compute(alerts)
            _uiState.value = _uiState.value.copy(
                statuses = current,
                isLoading = false,
                error = null,
                lastUpdated = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            )
        } catch (e: AlertsApiException) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
        }
    }
}
