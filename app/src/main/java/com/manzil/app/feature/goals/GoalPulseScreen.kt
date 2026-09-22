package com.manzil.app.feature.goals
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
@Composable
fun GoalPulseScreen() {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("GOAL PULSE — What Changed", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        items(5) { i ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(listOf("🟢 CREATED","🔵 UPDATED","📈 PROGRESS","💰 METRIC","🔴 DELETED")[i%5], fontWeight = FontWeight.Bold)
                    Text("Diff: old red strikethrough, new green", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
