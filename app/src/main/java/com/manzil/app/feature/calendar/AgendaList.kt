package com.manzil.app.feature.calendar
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
@Composable
fun AgendaList() {
    Column(Modifier.fillMaxWidth()) {
        Text("Agenda — Infinite list grouped by Today/Tomorrow/This week/Later sticky headers", style = MaterialTheme.typography.bodySmall)
    }
}
