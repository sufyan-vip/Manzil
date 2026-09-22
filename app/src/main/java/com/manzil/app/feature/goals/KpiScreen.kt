package com.manzil.app.feature.goals
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
@Composable
fun KpiScreen() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("KPI Dashboard — Free Tracking", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(12) { i ->
                Card { Column(Modifier.padding(12.dp)) {
                    Text(listOf("Savings","MRR","Intl Clients","Local","Team","GitHub","Reviews","CGPA","Deep Work","Income Y1","Income Y2","Capital")[i], fontWeight = FontWeight.Bold)
                    LinearProgressIndicator(progress = { i*0.08f }, modifier = Modifier.fillMaxWidth())
                }}
            }
        }
    }
}
