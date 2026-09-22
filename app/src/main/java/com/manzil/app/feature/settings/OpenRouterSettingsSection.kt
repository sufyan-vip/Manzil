package com.manzil.app.feature.settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun OpenRouterSettingsSection() {
    var key by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("OpenRouter API Key — Free models available", style = MaterialTheme.typography.titleMedium)
        Text("Get free key from openrouter.ai/keys — many free models", style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(value = key, onValueChange = { key = it }, label = { Text("sk-or-v1-••••") }, modifier = Modifier.fillMaxWidth())
        Text("Stored encrypted on device only — green lock icon", style = MaterialTheme.typography.bodySmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {}) { Text("Save") }
            OutlinedButton(onClick = {}) { Text("Test") }
            OutlinedButton(onClick = {}) { Text("Remove") }
        }
        Text("Model picker: openrouter/auto (free) • Free badge • Usage counter / soft cap 50/day", style = MaterialTheme.typography.bodySmall)
    }
}
