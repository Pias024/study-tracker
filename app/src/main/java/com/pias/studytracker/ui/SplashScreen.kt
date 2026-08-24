package com.pias.studytracker.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private const val QUOTE = "\u201cDon't wait to feel motivated.\nStart, and motivation will follow.\u201d"
private const val VISIBLE_DURATION_MS = 2600L

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 900),
        label = "splashAlpha"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(VISIBLE_DURATION_MS)
        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        GlowingBlobBackground(modifier = Modifier.fillMaxSize())
        Text(
            text = QUOTE,
            modifier = Modifier.padding(32.dp).alpha(alpha),
            color = NeonTeal,
            style = MaterialTheme.typography.headlineSmall,
            fontStyle = FontStyle.Italic,
            textAlign = TextAlign.Center
        )
    }
}
