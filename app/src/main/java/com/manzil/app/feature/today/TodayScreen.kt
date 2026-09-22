package com.manzil.app.feature.today

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    viewModel: TodayViewModel = hiltViewModel()
) {
    val today = LocalDate.now()
    var timerRunning by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Live Header - BS SE Edition
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Good Morning, ${viewModel.userName} — Day ${viewModel.dayCounter} of your plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(today.format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")), style = MaterialTheme.typography.bodySmall)
                    Text("BS Software Engineering • ${viewModel.semester} • ${viewModel.university}", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        item {
            // Briefing Card with 3 blocks mandatory: karna hai / ho gaya / reh gaya
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Daily Briefing", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Divider()
                    Text("DO TODAY (3)", fontWeight = FontWeight.Bold)
                    Text("• 09:00 React: useEffect + data fetching (2h)")
                    Text("• 14:00 Send 10 proposals via Instagram DM")
                    Text("• 20:00 FYP Chapter 2 outline")
                    Spacer(Modifier.height(8.dp))
                    Text("DONE YESTERDAY (4/5) — 80%", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Still pending: \"CS50 week 3\"", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Text("GOAL MOVE: Freelance income 25k → 40k PKR this month (+60%)", style = MaterialTheme.typography.bodySmall)
                    Text("Streak: 11 days 🔥 • Root goal: 18%", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { viewModel.regenerateWithAI() }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.SmartToy, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Regenerate with AI (Adaptive Engine)")
                    }
                }
            }
        }

        item {
            // Focus Block
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Focus of the Day", fontWeight = FontWeight.Bold)
                    Text("React: useEffect + data fetching", style = MaterialTheme.typography.titleMedium)
                    Text("00:42:15", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilledButton(onClick = { timerRunning = !timerRunning }) {
                            Text(if (timerRunning) "Pause" else "Start")
                        }
                        OutlinedButton(onClick = { timerRunning = false }) {
                            Text("Stop & Save TimeEntry")
                        }
                    }
                }
            }
        }

        item {
            Text("Today's Plan (Smart Rescheduler Active)", fontWeight = FontWeight.Bold)
        }

        items(3) { i ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = i == 0, onCheckedChange = {})
                    Column(Modifier.weight(1f)) {
                        Text("Task ${i+1}: Proposal via LinkedIn", fontWeight = FontWeight.Medium)
                        Text("Priority P1 • #client • Auto-adjusts to tomorrow if missed", style = MaterialTheme.typography.labelSmall)
                    }
                    Icon(Icons.Filled.DragHandle, contentDescription = "Drag")
                }
            }
        }

        item {
            // Progress Strip
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ProgressRing(label = "Today", progress = 0.6f)
                ProgressRing(label = "Week", progress = 0.45f)
                ProgressRing(label = "Goal", progress = 0.18f)
            }
        }

        item {
            // Streak Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Streak: 11 days 🔥", fontWeight = FontWeight.Bold)
                    Text("GitHub-style heatmap (12 weeks) — streak at risk after 20:00 if nothing done", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            OutlinedTextField(value = "", onValueChange = {}, label = { Text("Quick capture… e.g. proposal 5pm friday !1 #client") }, modifier = Modifier.fillMaxWidth())
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(16.dp)) {
                    Text("Yesterday's Leftovers", fontWeight = FontWeight.Bold)
                    Text("CS50 week 3 problem set — Auto-adjusted to today by Adaptive Engine", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {}) { Text("Move to today") }
                        OutlinedButton(onClick = {}) { Text("Drop") }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressRing(label: String, progress: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
        Text("${(progress*100).toInt()}%", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FilledButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Button(onClick = onClick) { content() }
}
