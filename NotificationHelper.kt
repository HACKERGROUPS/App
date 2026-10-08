package com.ukrainealerts.map.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ukrainealerts.map.MainActivity
import com.ukrainealerts.map.R

object NotificationHelper {
    const val CHANNEL_ALERTS = "alerts_channel"
    const val CHANNEL_SERVICE = "service_channel"
    const val NOTIF_ID_SERVICE = 1

    private var nextAlertNotificationId = 100

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ALERTS,
                "Тривога / відбій",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { description = "Сповіщення про нову повітряну тривогу та відбій" }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_SERVICE,
                "Фонове стеження",
                NotificationManager.IMPORTANCE_LOW,
            ).apply { description = "Постійне сповіщення під час фонового моніторингу" }
        )
    }

    fun buildServiceNotification(context: Context, text: String) =
        NotificationCompat.Builder(context, CHANNEL_SERVICE)
            .setContentTitle("Карта тривог — стеження активне")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setContentIntent(mainActivityIntent(context))
            .build()

    fun showAlertNotification(context: Context, title: String, body: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(mainActivityIntent(context))
            .build()
        NotificationManagerCompat.from(context).notify(nextAlertNotificationId++, notification)
    }

    private fun mainActivityIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
