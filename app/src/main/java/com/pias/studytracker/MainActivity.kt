package com.pias.studytracker

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pias.studytracker.ui.CalendarView
import com.pias.studytracker.ui.GlassCard
import com.pias.studytracker.ui.GlowingBlobBackground
import com.pias.studytracker.ui.NameEntryScreen
import com.pias.studytracker.ui.SettingsScreen
import com.pias.studytracker.ui.SplashScreen
import com.pias.studytracker.ui.StudyTrackerTheme
import com.pias.studytracker.ui.StudyViewModel
import kotlinx.coroutines.launch
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
                    RootNavigation(viewModel)
                }
            }
        }
    }
}

private enum class Screen { SPLASH, NAME_ENTRY, MAIN, SETTINGS }

@Composable
private fun RootNavigation(viewModel: StudyViewModel) {
    var screen by remember { mutableStateOf(Screen.SPLASH) }
    val userName by viewModel.userName.collectAsState()
    val scope = rememberCoroutineScope()

    val context = androidx.compose.ui.platform.LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val json = viewModel.exportBackupJson()
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (text != null) viewModel.importBackupJson(text)
            }
        }
    }

    when (screen) {
        Screen.SPLASH -> SplashScreen(onFinished = {
            screen = if (userName.isNullOrBlank()) Screen.NAME_ENTRY else Screen.MAIN
        })

        Screen.NAME_ENTRY -> NameEntryScreen(onNameConfirmed = { name ->
            viewModel.setUserName(name)
            screen = Screen.MAIN
        })

        Screen.MAIN -> StudyTrackerApp(
            viewModel = viewModel,
            userName = userName ?: "",
            onOpenSettings = { screen = Screen.SETTINGS }
        )

        Screen.SETTINGS -> {
            val tiers by viewModel.rankTiers.collectAsState()
            SettingsScreen(
                currentName = userName ?: "",
                rankTiers = tiers,
                onBack = { screen = Screen.MAIN },
                onNameChange = { viewModel.setUserName(it) },
                onTierUpsert = { viewModel.upsertRankTier(it) },
                onTierDelete = { viewModel.deleteRankTier(it) },
                onResetTiers = { viewModel.resetRankTiersToDefault() },
                onExport = { exportLauncher.launch("study-tracker-backup.json") },
                onImport = { importLauncher.launch(arrayOf("application/json")) }
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun StudyTrackerApp(viewModel: StudyViewModel, userName: String, onOpenSettings: () -> Unit) {
    val hoursByDate by viewModel.hoursByDate.collectAsState()
    val ultimateAverage by viewModel.ultimateAverage.collectAsState()
    val averageRank by viewModel.averageRank.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val visibleMonth by viewModel.visibleMonth.collectAsState()
    val dailyRank by viewModel.selectedDayRank.collectAsState()

    var hoursInput by remember(selectedDate, hoursByDate) {
        mutableStateOf((hoursByDate[selectedDate] ?: 0f).let {
            if (it == 0f) "" else if (it % 1f == 0f) it.toInt().toString() else it.toString()
        })
    }
    val hasExistingEntry = (hoursByDate[selectedDate] ?: 0f) > 0f

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    if (userName.isNotBlank()) "$userName's Study Tracker" else "Study Tracker",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )
    }) { padding ->
        Box(Modifier.fillMaxSize()) {
            GlowingBlobBackground(modifier = Modifier.fillMaxSize())

            Column(
                Modifier
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // ---- Ultimate average + its rank ----
                GlassCard {
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
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Average Rank: ${averageRank.tierName} (Level ${averageRank.level})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ---- Entry form ----
                GlassCard {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = selectedDate.format(
                                DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(Locale.getDefault())
                            ),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                        if (hasExistingEntry) {
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    viewModel.deleteEntry(selectedDate)
                                    hoursInput = ""
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.width(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Delete this entry")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ---- Daily rank card ----
                GlassCard {
                    Column(Modifier.padding(16.dp)) {
                        Text("Today's Rank", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = dailyRank.tierName,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Level ${dailyRank.level} \u00b7 ${"%.1f".format(dailyRank.hoursIntoTier)}h into this tier",
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
                GlassCard {
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
}
