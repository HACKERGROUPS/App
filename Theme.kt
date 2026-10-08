package com.ukrainealerts.map.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AppColorScheme = darkColorScheme(
    background = BgDark,
    surface = PanelDark,
    primary = AccentBlue,
    onBackground = TextLight,
    onSurface = TextLight,
    secondary = MutedGray,
    error = StatusFull,
)

@Composable
fun AlertsMapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        content = content,
    )
}
