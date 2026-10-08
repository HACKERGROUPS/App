package com.ukrainealerts.map.data

/**
 * Перетворює список тривог з API на статус кожної з 27 областей
 * (аналог compute_region_statuses у status.py) і порівнює два стани,
 * щоб визначити, де щойно почалась тривога чи відбій (diff_statuses).
 */
object StatusCalculator {

    fun compute(alerts: List<Alert>): Map<String, RegionStatus> {
        val statuses = Regions.ALL.associate { it.code to RegionStatus(it) }.toMutableMap()

        for (alert in alerts) {
            if (alert.locationType == "oblast") {
                val region = Regions.BY_TITLE[alert.locationTitle] ?: continue
                statuses[region.code] = statuses.getValue(region.code).copy(
                    status = AlertStatus.FULL,
                    alertType = alert.alertType,
                    startedAt = alert.startedAt,
                )
            } else {
                val parentTitle = alert.locationOblast ?: continue
                val region = Regions.BY_TITLE[parentTitle] ?: continue
                val current = statuses.getValue(region.code)
                statuses[region.code] = if (current.status != AlertStatus.FULL) {
                    current.copy(
                        status = AlertStatus.PARTIAL,
                        alertType = current.alertType ?: alert.alertType,
                        startedAt = current.startedAt ?: alert.startedAt,
                        details = current.details + alert.locationTitle,
                    )
                } else {
                    current.copy(details = current.details + alert.locationTitle)
                }
            }
        }
        return statuses
    }

    /**
     * Повертає (newlyAlerted, newlyCleared) — назви областей, де щойно
     * почалась тривога (none -> partial/full) чи відбій (-> none).
     * previous == null (перший запуск) -> обидва списки порожні, щоб не
     * "вистрілити" звуком/сповіщенням одразу при старті.
     */
    fun diff(
        previous: Map<String, RegionStatus>?,
        current: Map<String, RegionStatus>,
    ): Pair<List<String>, List<String>> {
        if (previous == null) return emptyList<String>() to emptyList()

        val newlyAlerted = mutableListOf<String>()
        val newlyCleared = mutableListOf<String>()
        for ((code, cur) in current) {
            val prev = previous[code] ?: continue
            val wasAlert = prev.status != AlertStatus.NONE
            val isAlert = cur.status != AlertStatus.NONE
            if (!wasAlert && isAlert) newlyAlerted += cur.region.apiTitle
            else if (wasAlert && !isAlert) newlyCleared += cur.region.apiTitle
        }
        return newlyAlerted to newlyCleared
    }
}
