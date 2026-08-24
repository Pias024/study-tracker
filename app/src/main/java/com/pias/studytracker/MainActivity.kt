package com.pias.studytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pias.studytracker.ui.CalendarView
import com.pias.studytracker.ui.StudyTrackerTheme
import com.pias.studytracker.ui.StudyViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: StudyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyTrackerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StudyTrackerApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun StudyTrackerApp(viewModel: StudyViewModel) {
    val hoursByDate by viewModel.hoursByDate.collectAsState()
    val ultimateAverage by viewModel.ultimateAverage.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val visibleMonth by viewModel.visibleMonth.collectAsState()
    val dailyRank by viewModel.selectedDayRank.collectAsState()

    var hoursInput by remember(selectedDate, hoursByDate) {
        mutableStateOf((hoursByDate[selectedDate] ?: 0f).let {
            if (it == 0f) "" else if (it % 1f == 0f) it.toInt().toString() else it.toString()
        })
    }

    Scaffold(topBar = {
        Surface(color = MaterialTheme.colorScheme.primary) {
            Text(
                "Study Tracker",
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ---- Ultimate average card ----
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Ultimate Average", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = "%.2f h/day".format(ultimateAverage),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Total: %.1fh across %d tracked day(s)".format(
                            hoursByDate.values.sum(), hoursByDate.size
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Entry form ----
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = selectedDate.format(
                            DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.getDefault())
                        ),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = hoursInput,
                            onValueChange = { hoursInput = it },
                            label = { Text("Hours studied") },
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(12.dp))
                        Button(onClick = {
                            val value = hoursInput.toFloatOrNull() ?: 0f
                            viewModel.saveHours(selectedDate, value)
                        }) {
                            Text("Save")
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Daily rank card (resets each day, levels up every 0.5h that day) ----
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Today's Rank", style = MaterialTheme.typography.labelLarge)
                    Text(
                        text = dailyRank.tierName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Level ${dailyRank.level} · ${"%.1f".format(dailyRank.hoursIntoTier)}h into this tier",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { dailyRank.progressToNextLevel },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "%.1fh to next level".format(dailyRank.hoursToNextLevel),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Calendar ----
            Card(Modifier.fillMaxWidth()) {
                CalendarView(
                    month = visibleMonth,
                    hoursByDate = hoursByDate,
                    selectedDate = selectedDate,
                    onSelectDate = { viewModel.selectDate(it) },
                    onChangeMonth = { viewModel.changeMonth(it) }
                )
            }
        }
    }
}
