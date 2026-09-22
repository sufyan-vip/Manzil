package com.manzil.app.feature.ai

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AiCoachScreen(viewModel: AiCoachViewModel = hiltViewModel()) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("AI Coach — Strict Task Manager", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Powered by OpenRouter — Offline-first, AI is enhancement", style = MaterialTheme.typography.bodySmall)
        
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("STRICT SYSTEM PROMPT FOR AI:", fontWeight = FontWeight.Bold)
                Text("You are Manzil Adaptive Engine. You MUST:", style = MaterialTheme.typography.bodySmall)
                Text("1. Check task condition (TODO/BLOCKED/OVERDUE)", style = MaterialTheme.typography.bodySmall)
                Text("2. Auto-arrange next tasks to push user to goal", style = MaterialTheme.typography.bodySmall)
                Text("3. If task unfinished → adjust with tomorrow + reason", style = MaterialTheme.typography.bodySmall)
                Text("4. Update tasks with latest market info (client hunting)", style = MaterialTheme.typography.bodySmall)
                Text("5. Never suggest Fiverr/Upwork as primary — suggest Instagram, FB, LinkedIn, X, Reddit, Discord", style = MaterialTheme.typography.bodySmall)
                Text("6. Keep goal perpetual till DONE", style = MaterialTheme.typography.bodySmall)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Plan my day") }
            Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Weekly review") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Explain changes") }
            Button(onClick = {}, modifier = Modifier.weight(1f)) { Text("Unblock me") }
        }

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Chat History (local)")
                Text("User: My task is blocked", style = MaterialTheme.typography.bodySmall)
                Text("AI: Here are 3 concrete next actions...", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
