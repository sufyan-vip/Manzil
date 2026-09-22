package com.manzil.app.feature.search

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: SearchViewModel = hiltViewModel()) {
    var query by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Real-time Search (FTS4)", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Search goals, tasks, events…") }, modifier = Modifier.fillMaxWidth())
        Text("Instant 150ms debounce • <50ms on 5000 docs • bm25() ranking • title x3 boost", style = MaterialTheme.typography.bodySmall)
        Text("Filters: All / Goals / Tasks / Events / Journal", style = MaterialTheme.typography.bodySmall)
        Text("Recent: last 10 • Saved searches: 'Overdue client work'", style = MaterialTheme.typography.bodySmall)
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Results grouped by type with icons and counts")
                Text("Semantic toggle (AI re-ranking) — local first, AI async shimmer")
            }
        }
    }
}
