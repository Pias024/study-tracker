package com.pias.studytracker.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ---- Palette: dark HUD + neon teal/green glow, matching the desktop HUD's tone ----
val HudBackground = Color(0xFF0D1420)
val HudSurface = Color(0xFF16202E)
val NeonTeal = Color(0xFF2FE6C4)
val NeonGreen = Color(0xFF39FF6A)
val GlassBorder = Color(0x332FE6C4) // low-alpha teal for card outlines

private val DarkColors = darkColorScheme(
    primary = NeonTeal,
    secondary = NeonGreen,
    background = HudBackground,
    surface = HudSurface,
    onBackground = Color(0xFFE3F6F1),
    onSurface = Color(0xFFE3F6F1)
)

@Composable
fun StudyTrackerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColors, content = content)
}

/**
 * Slow-drifting glowing blobs behind the content — jellyfish-inspired motion,
 * rendered as soft radial-gradient glow rather than literal creature imagery,
 * so it reads as ambient HUD atmosphere instead of clashing with the hacker theme.
 */
@Composable
fun GlowingBlobBackground(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "blobs")
    val drift1 by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift1"
    )
    val drift2 by transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(19000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift2"
    )

    Canvas(modifier = modifier.fillMaxSize().background(HudBackground)) {
        val w = size.width
        val h = size.height

        // Blob 1: teal, upper-left drifting diagonally.
        val c1 = Offset(w * (0.15f + 0.15f * drift1), h * (0.20f + 0.10f * drift1))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonTeal.copy(alpha = 0.20f), NeonTeal.copy(alpha = 0f)),
                center = c1,
                radius = w * 0.55f
            ),
            radius = w * 0.55f,
            center = c1
        )

        // Blob 2: green, lower-right drifting the opposite way.
        val c2 = Offset(w * (0.85f - 0.15f * drift2), h * (0.75f - 0.12f * drift2))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NeonGreen.copy(alpha = 0.14f), NeonGreen.copy(alpha = 0f)),
                center = c2,
                radius = w * 0.6f
            ),
            radius = w * 0.6f,
            center = c2
        )
    }
}
