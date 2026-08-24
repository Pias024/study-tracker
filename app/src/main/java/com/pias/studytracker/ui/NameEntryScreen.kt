package com.pias.studytracker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun NameEntryScreen(onNameConfirmed: (String) -> Unit) {
    var name by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        GlowingBlobBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(
                "What should we call you?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Your name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonTeal,
                    cursorColor = NeonTeal
                )
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { if (name.isNotBlank()) onNameConfirmed(name.trim()) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Continue")
            }
        }
    }
}
