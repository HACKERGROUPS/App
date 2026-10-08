package com.ukrainealerts.map.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ukrainealerts.map.data.ALERT_TYPE_UK
import com.ukrainealerts.map.data.AlertStatus
import com.ukrainealerts.map.data.RegionStatus
import com.ukrainealerts.map.ui.UiState
import com.ukrainealerts.map.ui.theme.StatusFull
import com.ukrainealerts.map.ui.theme.StatusNone
import com.ukrainealerts.map.ui.theme.StatusPartial

@Composable
fun TableScreen(uiState: UiState, onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
    ) {
        StatusHeader(uiState = uiState, onRefresh = onRefresh)
        Spacer(Modifier.height(10.dp))

        val sorted = uiState.statuses.values.sortedWith(
            compareBy(
                { statusOrder(it.status) },
                { it.region.apiTitle },
            )
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(sorted) { rs -> RegionRow(rs) }
        }
    }
}

private fun statusOrder(status: AlertStatus): Int = when (status) {
    AlertStatus.FULL -> 0
    AlertStatus.PARTIAL -> 1
    AlertStatus.NONE -> 2
}

@Composable
private fun RegionRow(rs: RegionStatus) {
    val color = when (rs.status) {
        AlertStatus.NONE -> StatusNone
        AlertStatus.PARTIAL -> StatusPartial
        AlertStatus.FULL -> StatusFull
    }
    val label = when (rs.status) {
        AlertStatus.NONE -> "спокійно"
        AlertStatus.PARTIAL -> "частково"
        AlertStatus.FULL -> "тривога"
    }

    Card {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(rs.region.apiTitle, fontWeight = FontWeight.SemiBold)
                Text(label, color = color, fontWeight = FontWeight.Bold)
            }
            if (rs.status != AlertStatus.NONE) {
                Text(
                    ALERT_TYPE_UK[rs.alertType] ?: rs.alertType ?: "—",
                    style = MaterialTheme.typography.bodySmall,
                )
                rs.startedAt?.let {
                    Text("Триває з: $it", style = MaterialTheme.typography.bodySmall)
                }
                if (rs.details.isNotEmpty()) {
                    val tail = if (rs.details.size > 3) "…" else ""
                    Text(
                        "Деталі: " + rs.details.take(3).joinToString(", ") + tail,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
