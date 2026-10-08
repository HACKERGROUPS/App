package com.ukrainealerts.map.service

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.ukrainealerts.map.data.AlertStatus
import com.ukrainealerts.map.data.AlertsApi
import com.ukrainealerts.map.data.AlertsApiException
import com.ukrainealerts.map.data.RegionStatus
import com.ukrainealerts.map.data.SettingsRepository
import com.ukrainealerts.map.data.StatusCalculator
import com.ukrainealerts.map.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Foreground-служба: поки увімкнена (перемикач "Фонове стеження" в
 * Налаштуваннях), опитує alerts.in.ua з заданим інтервалом і показує
 * сповіщення/звук при новій тривозі чи відбої — навіть коли застосунок
 * закрито. Постійне сповіщення (CHANNEL_SERVICE) обов'язкове для
 * foreground-служб в Android.
 */
class AlertsMonitorService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null
    private lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        settingsRepository = SettingsRepository(applicationContext)
        NotificationHelper.ensureChannels(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundCompat()
        if (job?.isActive != true) {
            job = scope.launch { monitorLoop() }
        }
        return START_STICKY
    }

    private fun startForegroundCompat() {
        val notification = NotificationHelper.buildServiceNotification(this, "Очікую перше оновлення…")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceCompat.startForeground(
                this,
                NotificationHelper.NOTIF_ID_SERVICE,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            startForeground(NotificationHelper.NOTIF_ID_SERVICE, notification)
        }
    }

    private suspend fun monitorLoop() {
        var previous: Map<String, RegionStatus>? = null
        while (true) {
            val settings = settingsRepository.settingsFlow.first()
            try {
                val alerts = AlertsApi(settings.token).fetchActiveAlerts()
                val current = StatusCalculator.compute(alerts)

                val activeCount = current.values.count { it.status != AlertStatus.NONE }
                updateServiceNotification(
                    if (activeCount == 0) "Спокійно по всіх областях"
                    else "Активних тривог: $activeCount"
                )

                val (newlyAlerted, newlyCleared) = StatusCalculator.diff(previous, current)
                if (newlyAlerted.isNotEmpty()) {
                    if (settings.notificationsEnabled) {
                        NotificationHelper.showAlertNotification(
                            this, "🚨 Повітряна тривога", newlyAlerted.joinToString(", ")
                        )
                    }
                    if (settings.soundEnabled) playTone(alert = true)
                } else if (newlyCleared.isNotEmpty()) {
                    if (settings.notificationsEnabled) {
                        NotificationHelper.showAlertNotification(
                            this, "✅ Відбій тривоги", newlyCleared.joinToString(", ")
                        )
                    }
                    if (settings.soundEnabled) playTone(alert = false)
                }
                previous = current
            } catch (e: AlertsApiException) {
                updateServiceNotification("⚠ ${e.message}")
            }

            delay(settings.intervalSeconds * 1000L)
        }
    }

    private fun updateServiceNotification(text: String) {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        manager.notify(NotificationHelper.NOTIF_ID_SERVICE, NotificationHelper.buildServiceNotification(this, text))
    }

    private fun playTone(alert: Boolean) {
        runCatching {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
            if (alert) {
                toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
            } else {
                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
