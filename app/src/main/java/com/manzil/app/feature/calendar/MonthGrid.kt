package com.manzil.app.feature.calendar
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
@Composable
fun MonthGrid() {
    Column(Modifier.fillMaxWidth()) {
        Text("Month View — Dots per day: green ≤3, amber 4-6, red 7+, long-press → Plan this day", style = MaterialTheme.typography.bodySmall)
    }
}
