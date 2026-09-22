package com.manzil.app.feature.calendar
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun DayColumn() {
    Column(Modifier.fillMaxWidth().padding(8.dp)) {
        Text("Day View — Red now line moves every 30s, auto-scroll to now ±1h", style = MaterialTheme.typography.bodySmall)
        Text("Tasks as hatched blocks, drag to reschedule, resize to change duration", style = MaterialTheme.typography.bodySmall)
    }
}
