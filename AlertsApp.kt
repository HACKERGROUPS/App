package com.ukrainealerts.map.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ukrainealerts.map.ui.screens.MapScreen
import com.ukrainealerts.map.ui.screens.SettingsScreen
import com.ukrainealerts.map.ui.screens.TableScreen

private enum class Tab { MAP, TABLE, SETTINGS }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsApp(viewModel: MainViewModel) {
    var tab by remember { mutableStateOf(Tab.MAP) }
    val uiState by viewModel.uiState.collectAsState()
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("🚨 Карта тривог") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Tab.MAP,
                    onClick = { tab = Tab.MAP },
                    icon = { Text("🗺") },
                    label = { Text("Карта") },
                )
                NavigationBarItem(
                    selected = tab == Tab.TABLE,
                    onClick = { tab = Tab.TABLE },
                    icon = { Text("📋") },
                    label = { Text("Таблиця") },
                )
                NavigationBarItem(
                    selected = tab == Tab.SETTINGS,
                    onClick = { tab = Tab.SETTINGS },
                    icon = { Text("⚙️") },
                    label = { Text("Налаштування") },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (tab) {
                Tab.MAP -> MapScreen(uiState = uiState, onRefresh = viewModel::refreshNow)
                Tab.TABLE -> TableScreen(uiState = uiState, onRefresh = viewModel::refreshNow)
                Tab.SETTINGS -> SettingsScreen(settings = settings, viewModel = viewModel)
            }
        }
    }
}
