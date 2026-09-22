package com.manzil.app.feature.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CalendarScreen(viewModel: CalendarViewModel = hiltViewModel()) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Real-time Calendar", style = MaterialTheme.typography.headlineSmall)
        Text("Day / 3-Day / Week / Month / Agenda • Red now line • Drag to reschedule", style = MaterialTheme.typography.bodySmall)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Week View (Premium UI)")
                Text("• 09:00 Task: React useEffect (hatched)")
                Text("• 14:00 Event: Client call")
                Text("• Conflict detection: overlapping stripe")
                Text("• Month dots: green ≤3, amber 4-6, red 7+")
            }
        }
    }
}
