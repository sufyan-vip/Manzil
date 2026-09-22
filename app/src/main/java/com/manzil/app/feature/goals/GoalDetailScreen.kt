package com.manzil.app.feature.goals
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun GoalDetailScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Goal Detail — Perpetual", style = MaterialTheme.typography.titleLarge)
        Text("Progress = weighted avg, Metric bar + sparkline, Overdue red border", style = MaterialTheme.typography.bodySmall)
    }
}
