package com.pias.studytracker.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Teal = Color(0xFF4CAF9E)
private val TealLight = Color(0xFF7CD9C4)
private val DarkBg = Color(0xFF1B2430)

private val DarkColors = darkColorScheme(
    primary = Teal,
    secondary = TealLight,
    background = DarkBg,
    surface = Color(0xFF242F3E)
)

private val LightColors = lightColorScheme(
    primary = Teal,
    secondary = TealLight
)

@Composable
fun StudyTrackerTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
