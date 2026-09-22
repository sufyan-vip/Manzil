package com.manzil.app.feature.tasks
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun TaskListScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Tasks — List / Kanban / Matrix", style = MaterialTheme.typography.titleLarge)
        Text("Views: Today / Tomorrow / This week / Later / No date", style = MaterialTheme.typography.bodySmall)
        Text("Kanban: TODO → IN_PROGRESS → DONE", style = MaterialTheme.typography.bodySmall)
        Text("Eisenhower Matrix 2x2", style = MaterialTheme.typography.bodySmall)
    }
}
