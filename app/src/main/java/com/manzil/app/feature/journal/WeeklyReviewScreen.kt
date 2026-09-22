package com.manzil.app.feature.journal
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun WeeklyReviewScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Weekly Review", style = MaterialTheme.typography.titleLarge)
        Text("7-day score trend, focus-hours total, goal-diff summary, habit heatmap", style = MaterialTheme.typography.bodySmall)
        Text("AI narrative + 3 priorities for next week → one-tap tasks", style = MaterialTheme.typography.bodySmall)
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Generate AI Weekly Review") }
    }
}
