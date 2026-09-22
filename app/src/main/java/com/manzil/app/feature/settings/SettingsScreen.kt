package com.manzil.app.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    var apiKey by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("OpenRouter API Key (Top — as requested)", fontWeight = FontWeight.Bold)
                Text("Stored encrypted on device only • Green lock icon", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("sk-or-v1-••••••••") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {}) { Text("Save") }
                    OutlinedButton(onClick = {}) { Text("Test connection") }
                    OutlinedButton(onClick = {}) { Text("Remove") }
                }
                Text("Model picker: openrouter/auto • Free badge • Usage: requests today / soft cap", style = MaterialTheme.typography.bodySmall)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Notifications", fontWeight = FontWeight.Bold)
                Text("Morning 07:00, Evening 21:30, Rest days, Quiet hours, Per-channel toggles, AI-briefing toggle, Send test briefing", style = MaterialTheme.typography.bodySmall)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Appearance — Theme System/Light/Dark, Accent, Font scale, Compact mode", style = MaterialTheme.typography.bodySmall)
                Text("Language — English / اردو (runtime switch)", style = MaterialTheme.typography.bodySmall)
                Text("Data — Export JSON (minus secrets) SAF, CSV, Import, Backup reminder, Erase all", style = MaterialTheme.typography.bodySmall)
                Text("Plan — Root goal dates, deep-work target, currency, timezone, week start", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
