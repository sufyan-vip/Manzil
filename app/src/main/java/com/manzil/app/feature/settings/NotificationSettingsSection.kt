package com.manzil.app.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotificationSettingsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Notifications — Free", style = MaterialTheme.typography.titleMedium)
        Text("Morning 07:00, Evening 21:30, Rest days, Quiet hours, Per-channel toggles", style = MaterialTheme.typography.bodySmall)
        Text("AI-briefing toggle, Send test briefing, Exact-alarm status", style = MaterialTheme.typography.bodySmall)
        Text("Anti-nag: max 6/day, never 23:00-07:00, Snooze 1h, Quiet this week", style = MaterialTheme.typography.bodySmall)
    }
}
