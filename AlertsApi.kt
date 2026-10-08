package com.ukrainealerts.map.data

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class AlertsApiException(message: String) : Exception(message)

/**
 * Тонкий клієнт над /v1/alerts/active.json. Навмисно без OkHttp/Retrofit —
 * HttpURLConnection + org.json вже є в Android SDK, тож жодних додаткових
 * залежностей для мережі не потрібно (той самий підхід, що й у CLI/вебі:
 * мінімум залежностей).
 */
class AlertsApi(private val token: String) {

    companion object {
        private const val BASE_URL = "https://api.alerts.in.ua/v1/alerts/active.json"
        private const val TIMEOUT_MS = 10_000
    }

    fun fetchActiveAlerts(): List<Alert> {
        if (token.isBlank()) {
            throw AlertsApiException("Не задано токен alerts.in.ua. Відкрийте Налаштування.")
        }

        val url = URL("$BASE_URL?token=${URLEncoder.encode(token, "UTF-8")}")
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = TIMEOUT_MS
        connection.readTimeout = TIMEOUT_MS
        connection.requestMethod = "GET"

        try {
            val code = connection.responseCode
            if (code == 401) throw AlertsApiException("Невірний токен alerts.in.ua (401).")
            if (code == 429) throw AlertsApiException("Перевищено ліміт запитів до API (429).")

            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }

            if (code !in 200..299) {
                throw AlertsApiException("Помилка API ($code): ${body.take(200)}")
            }

            val json = JSONObject(body)
            val rawAlerts = json.optJSONArray("alerts") ?: return emptyList()
            val result = mutableListOf<Alert>()
            for (i in 0 until rawAlerts.length()) {
                val item = rawAlerts.getJSONObject(i)
                val oblast = item.optString("location_oblast", "").ifBlank { null }
                result += Alert(
                    locationTitle = item.optString("location_title", "?"),
                    locationType = item.optString("location_type", "?"),
                    alertType = item.optString("alert_type", "?"),
                    startedAt = item.optString("started_at", "?"),
                    locationOblast = oblast,
                )
            }
            return result
        } catch (e: AlertsApiException) {
            throw e
        } catch (e: Exception) {
            throw AlertsApiException("Немає з'єднання з API: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }
}
