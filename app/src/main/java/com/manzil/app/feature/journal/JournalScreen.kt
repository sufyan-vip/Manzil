package com.manzil.app.feature.journal

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun JournalScreen(viewModel: JournalViewModel = hiltViewModel()) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Journal & Weekly Review", style = MaterialTheme.typography.headlineSmall)
        Text("Mood 1-5 emoji • Wins • Blockers • Free note • Pre-filled with done/pending", style = MaterialTheme.typography.bodySmall)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("7-day score trend, focus-hours, goal-diff, habit heatmap")
                Text("AI narrative + 3 priorities for next week → one-tap tasks")
            }
        }
    }
}
