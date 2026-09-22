package com.manzil.app.feature.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingCompleteScreen(onStart: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🎉", style = MaterialTheme.typography.displayLarge)
        Spacer(Modifier.height(16.dp))
        Text("You're all set, zero se start!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Free tools se goal tak — Manzil will auto-adjust tasks, hunt clients via Instagram/LinkedIn/FB, and keep your goal alive till DONE.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("What happens next:", fontWeight = FontWeight.Bold)
                Text("• Today: 3 tasks auto-generated (free methods)", style = MaterialTheme.typography.bodySmall)
                Text("• If task missed → kal ke sath adjust + reason", style = MaterialTheme.typography.bodySmall)
                Text("• AI checks condition and re-arranges next tasks", style = MaterialTheme.typography.bodySmall)
                Text("• Weekly latest info sync — tasks update with market trends", style = MaterialTheme.typography.bodySmall)
                Text("• Client hunting: Insta, FB, LinkedIn, X, Reddit, Discord — no Fiverr torture", style = MaterialTheme.typography.bodySmall)
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("Start My Journey — Day 1") }
    }
}
