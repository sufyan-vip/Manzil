package com.manzil.app.feature.today.components
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun QuickCaptureField(value: String, onValueChange: (String) -> Unit, onSubmit: () -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValueChange, label = { Text("Quick capture… e.g. proposal 5pm friday !1 #client") }, modifier = Modifier.fillMaxWidth())
}
@Composable
fun StreakCard(streak: Int) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Streak: $streak days")
            Text("GitHub-style heatmap — 12 weeks", style = MaterialTheme.typography.bodySmall)
        }
    }
}
