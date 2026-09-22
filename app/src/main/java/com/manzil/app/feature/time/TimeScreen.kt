package com.manzil.app.feature.time

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TimeScreen(viewModel: TimeViewModel = hiltViewModel()) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Time Tracking", style = MaterialTheme.typography.headlineSmall)
        Text("Single active timer • Auto-stop >4h • Daily bar 14 days • Per-goal donut", style = MaterialTheme.typography.bodySmall)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Focus minutes today vs 7-day avg")
                Text("Weekly total vs 25h/week target")
            }
        }
    }
}
