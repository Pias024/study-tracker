package com.pias.studytracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pias.studytracker.data.RankTier

@Composable
fun SettingsScreen(
    currentName: String,
    rankTiers: List<RankTier>,
    onBack: () -> Unit,
    onNameChange: (String) -> Unit,
    onTierUpsert: (RankTier) -> Unit,
    onTierDelete: (RankTier) -> Unit,
    onResetTiers: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    var nameField by remember(currentName) { mutableStateOf(currentName) }

    androidx.compose.material3.Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ---- Name ----
            GlassCard {
                Column(Modifier.padding(16.dp)) {
                    Text("Your name", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = nameField,
                            onValueChange = { nameField = it },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { if (nameField.isNotBlank()) onNameChange(nameField.trim()) }) {
                            Text("Save")
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Backup ----
            GlassCard {
                Column(Modifier.padding(16.dp)) {
                    Text("Backup", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "Export saves everything to a file. Import replaces current data with a backup file.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(12.dp))
                    Row {
                        Button(onClick = onExport, modifier = Modifier.weight(1f)) { Text("Export") }
                        Spacer(Modifier.width(12.dp))
                        OutlinedButton(onClick = onImport, modifier = Modifier.weight(1f)) { Text("Import") }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---- Rank tiers ----
            GlassCard {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rank tiers", style = MaterialTheme.typography.labelLarge)
                        IconButton(onClick = onResetTiers) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset to default")
                        }
                    }
                    Text(
                        "Sorted automatically by hours. Edit a tier's hours to reorder it.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))

                    rankTiers.sortedBy { it.hours }.forEach { tier ->
                        RankTierRow(
                            tier = tier,
                            onUpdate = { updated -> onTierUpsert(updated) },
                            onDelete = { onTierDelete(tier) }
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onTierUpsert(RankTier(hours = 0f, name = "New Rank")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ Add tier")
                    }
                }
            }
        }
    }
}

@Composable
private fun RankTierRow(
    tier: RankTier,
    onUpdate: (RankTier) -> Unit,
    onDelete: () -> Unit
) {
    var hoursText by remember(tier.id) { mutableStateOf(if (tier.hours % 1f == 0f) tier.hours.toInt().toString() else tier.hours.toString()) }
    var nameText by remember(tier.id) { mutableStateOf(tier.name) }

    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = hoursText,
            onValueChange = {
                hoursText = it
                it.toFloatOrNull()?.let { h -> onUpdate(tier.copy(hours = h, name = nameText)) }
            },
            label = { Text("h") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.width(70.dp)
        )
        Spacer(Modifier.width(8.dp))
        OutlinedTextField(
            value = nameText,
            onValueChange = {
                nameText = it
                onUpdate(tier.copy(hours = hoursText.toFloatOrNull() ?: tier.hours, name = it))
            },
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete tier")
        }
    }
}

@Composable
fun GlassCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = HudSurface.copy(alpha = 0.75f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        content()
    }
}
