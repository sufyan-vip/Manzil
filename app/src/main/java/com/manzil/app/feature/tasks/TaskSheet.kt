package com.manzil.app.feature.tasks
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskSheet(onDismiss: () -> Unit = {}) {
    var title by remember { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Create Task — Free Method", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title — e.g. proposal 5pm friday !1 #client") }, modifier = Modifier.fillMaxWidth())
            Text("Quick Capture Parser: auto-detects time, day, priority, goal tag — offline", style = MaterialTheme.typography.bodySmall)
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Save — Auto-adjusts to tomorrow if missed") }
            Spacer(Modifier.height(32.dp))
        }
    }
}
