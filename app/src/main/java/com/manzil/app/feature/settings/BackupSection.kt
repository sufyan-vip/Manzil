package com.manzil.app.feature.settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun BackupSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Data — Export/Import", style = MaterialTheme.typography.titleMedium)
        Text("JSON schemaVersion 1, excludes SecretVault, SAF auto-backup weekly", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {}) { Text("Export JSON") }
            OutlinedButton(onClick = {}) { Text("Export CSV") }
            OutlinedButton(onClick = {}) { Text("Import") }
        }
        Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Erase all data — type to confirm") }
    }
}
