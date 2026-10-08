package com.ukrainealerts.map.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.ukrainealerts.map.data.AppSettings
import com.ukrainealerts.map.ui.MainViewModel

@Composable
fun SettingsScreen(settings: AppSettings, viewModel: MainViewModel) {
    var token by remember(settings.token) { mutableStateOf(settings.token) }
    var tokenVisible by remember { mutableStateOf(false) }
    var interval by remember(settings.intervalSeconds) {
        mutableFloatStateOf(settings.intervalSeconds.toFloat())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Токен alerts.in.ua", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("API-токен") },
            singleLine = true,
            visualTransformation = if (tokenVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                TextButton(onClick = { tokenVisible = !tokenVisible }) {
                    Text(if (tokenVisible) "Сховати" else "Показати")
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { viewModel.saveToken(token) },
            enabled = token != settings.token,
        ) {
            Text("Зберегти токен")
        }

        Divider()

        Text("Інтервал оновлення: ${interval.toInt()} с", style = MaterialTheme.typography.titleMedium)
        Slider(
            value = interval,
            onValueChange = { interval = it },
            onValueChangeFinished = { viewModel.saveInterval(interval.toInt()) },
            valueRange = 5f..120f,
            steps = 22,
        )

        Divider()

        SettingSwitchRow(
            title = "Звук при тривозі/відбої",
            checked = settings.soundEnabled,
            onCheckedChange = { viewModel.saveSound(it) },
        )
        OutlinedButton(onClick = { viewModel.testSound() }) {
            Text("🔊 Перевірити звук")
        }

        Spacer(Modifier.height(2.dp))

        SettingSwitchRow(
            title = "Push-сповіщення",
            checked = settings.notificationsEnabled,
            onCheckedChange = { viewModel.saveNotifications(it) },
        )

        Spacer(Modifier.height(2.dp))

        SettingSwitchRow(
            title = "Фонове стеження (постійне сповіщення; працює, навіть коли застосунок закрито)",
            checked = settings.backgroundMonitoring,
            onCheckedChange = { viewModel.saveBackgroundMonitoring(it) },
        )

        Spacer(Modifier.height(8.dp))
        Text(
            "Токен зберігається локально на пристрої (приватне сховище застосунку) " +
                "і нікуди, крім alerts.in.ua, не надсилається.\n\n" +
                "Якщо фонове стеження зупиняється саме по собі — додайте застосунок " +
                "у винятки енергозбереження/оптимізації батареї у системних налаштуваннях " +
                "(особливо актуально на Xiaomi, Samsung, Huawei).",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SettingSwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
