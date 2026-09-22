package com.manzil.app.feature.goals

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun GoalsScreen(viewModel: GoalsViewModel = hiltViewModel()) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Goals + GOAL PULSE", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Tree view • Progress rings • Metric goals • Overdue red border", style = MaterialTheme.typography.bodySmall)
        
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(16.dp)) {
                Text("Adaptive Goal Engine (Perpetual)", fontWeight = FontWeight.Bold)
                Text("• Goal runs till YOU mark DONE, not fixed 2030", style = MaterialTheme.typography.bodySmall)
                Text("• Progress = weighted avg of children", style = MaterialTheme.typography.bodySmall)
                Text("• AI monitors condition and re-arranges tasks", style = MaterialTheme.typography.bodySmall)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("GOAL PULSE Feed", fontWeight = FontWeight.Bold)
                Text("🟢 CREATED — New goal: Land first client via Instagram", style = MaterialTheme.typography.bodySmall)
                Text("🔵 UPDATED — targetDate 2030-06-01 → 2030-09-01 (old red strikethrough, new green)", style = MaterialTheme.typography.bodySmall)
                Text("📈 PROGRESS — 42% → 55% (+13)", style = MaterialTheme.typography.bodySmall)
                Text("💰 METRIC — Income 25k → 40k PKR (+60%)", style = MaterialTheme.typography.bodySmall)
                Button(onClick = {}) { Text("Explain what changed (AI)") }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Weekly Goal Digest: 2 added • 1 deadline moved • 3 milestones closed • +7%", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
