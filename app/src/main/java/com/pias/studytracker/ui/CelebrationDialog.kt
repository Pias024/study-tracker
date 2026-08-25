package com.pias.studytracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/** One-shot celebration event — either a daily rank-up or a lifetime total-hours milestone. */
sealed class CelebrationEvent {
    data class DailyRankUp(val tierName: String) : CelebrationEvent()
    data class Milestone(val hours: Float) : CelebrationEvent()
}

@Composable
fun CelebrationDialog(event: CelebrationEvent, onDismiss: () -> Unit) {
    val (title, message) = when (event) {
        is CelebrationEvent.DailyRankUp -> "\uD83C\uDF89 Rank up!" to "You're now ${event.tierName} for today."
        is CelebrationEvent.Milestone -> "\uD83C\uDF89 Milestone!" to
            "You've logged ${event.hours.toInt()} hours since day one."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Nice") } },
        title = { Text(title) },
        text = { Text(message, modifier = androidx.compose.ui.Modifier.padding(top = 4.dp)) }
    )
}
