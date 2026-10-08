package com.ukrainealerts.map.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ukrainealerts.map.data.AlertStatus
import com.ukrainealerts.map.data.Regions
import com.ukrainealerts.map.ui.UiState
import com.ukrainealerts.map.ui.theme.StatusFull
import com.ukrainealerts.map.ui.theme.StatusNone
import com.ukrainealerts.map.ui.theme.StatusPartial

@Composable
fun MapScreen(uiState: UiState, onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
    ) {
        StatusHeader(uiState = uiState, onRefresh = onRefresh)
        Spacer(Modifier.height(10.dp))

        val byPosition = remember(uiState) { Regions.ALL.associateBy { it.row to it.col } }

        LazyVerticalGrid(
            columns = GridCells.Fixed(Regions.GRID_COLS),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(Regions.GRID_ROWS * Regions.GRID_COLS) { index ->
                val row = index / Regions.GRID_COLS
                val col = index % Regions.GRID_COLS
                val region = byPosition[row to col]

                if (region == null) {
                    Spacer(modifier = Modifier.height(46.dp))
                } else {
                    val status = uiState.statuses[region.code]?.status ?: AlertStatus.NONE
                    val color = statusColor(status)
                    Box(
                        modifier = Modifier
                            .height(46.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(color.copy(alpha = if (status == AlertStatus.NONE) 0.16f else 0.32f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(region.abbr, color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Legend(uiState = uiState)
    }
}

@Composable
private fun statusColor(status: AlertStatus): Color = when (status) {
    AlertStatus.NONE -> StatusNone
    AlertStatus.PARTIAL -> StatusPartial
    AlertStatus.FULL -> StatusFull
}

@Composable
private fun Legend(uiState: UiState) {
    val full = uiState.statuses.values.count { it.status == AlertStatus.FULL }
    val partial = uiState.statuses.values.count { it.status == AlertStatus.PARTIAL }
    val none = uiState.statuses.values.count { it.status == AlertStatus.NONE }
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        LegendItem(color = StatusFull, label = "тривога ($full)")
        LegendItem(color = StatusPartial, label = "частково ($partial)")
        LegendItem(color = StatusNone, label = "спокійно ($none)")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 13.sp)
    }
}
