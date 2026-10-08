package com.ukrainealerts.map.data

/** Одна тривога, як її повертає alerts.in.ua. */
data class Alert(
    val locationTitle: String,
    val locationType: String,       // "oblast" | "raion" | "city" | "hromada" ...
    val alertType: String,          // "air_raid" | "artillery_shelling" | ...
    val startedAt: String,
    val locationOblast: String? = null,
)

enum class AlertStatus { NONE, PARTIAL, FULL }

/** Область зі становищем у сітці для карти (аналог regions.py). */
data class Region(
    val code: String,
    val apiTitle: String,
    val abbr: String,
    val row: Int,
    val col: Int,
)

data class RegionStatus(
    val region: Region,
    val status: AlertStatus = AlertStatus.NONE,
    val alertType: String? = null,
    val startedAt: String? = null,
    val details: List<String> = emptyList(),
)

val ALERT_TYPE_UK: Map<String, String> = mapOf(
    "air_raid" to "повітряна тривога",
    "artillery_shelling" to "загроза артобстрілу",
    "urban_fights" to "вуличні бої",
    "chemical" to "хімічна загроза",
    "nuclear" to "радіаційна загроза",
)
