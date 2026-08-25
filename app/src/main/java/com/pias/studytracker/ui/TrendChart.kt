package com.pias.studytracker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/** Simple bar chart, no external charting library — just enough to show a trend at a glance. */
@Composable
fun TrendChart(series: List<Pair<LocalDate, Float>>, modifier: Modifier = Modifier) {
    val barColor = NeonGreen
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
    val maxHours = (series.maxOfOrNull { it.second } ?: 0f).coerceAtLeast(1f)

    Canvas(modifier = modifier.fillMaxWidth().height(140.dp)) {
        val barCount = series.size
        if (barCount == 0) return@Canvas

        val gap = size.width * 0.015f
        val barWidth = (size.width - gap * (barCount - 1)) / barCount

        // baseline
        drawLine(
            color = axisColor,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 2f
        )

        series.forEachIndexed { index, (_, hours) ->
            val barHeight = (hours / maxHours) * (size.height - 4.dp.toPx())
            val left = index * (barWidth + gap)
            val top = size.height - barHeight
            drawRect(
                color = if (hours > 0f) barColor.copy(alpha = 0.75f) else axisColor,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight.coerceAtLeast(1f))
            )
        }
    }
}
