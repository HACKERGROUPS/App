package com.ukrainealerts.map.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ukrainealerts.map.ui.UiState

@Composable
fun StatusHeader(uiState: UiState, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("Оновлено: ${uiState.lastUpdated}", style = MaterialTheme.typography.bodySmall)
            uiState.error?.let {
                Text(
                    "⚠ $it",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        TextButton(onClick = onRefresh) {
            Text(if (uiState.isLoading) "Оновлюю…" else "Оновити")
        }
    }
}
