@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.manzil.app.feature.journal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manzil.app.core.common.Fmt
import com.manzil.app.core.design.EmptyState
import com.manzil.app.core.design.InfoBanner
import com.manzil.app.core.design.KeyValueRow
import com.manzil.app.core.design.ManzilCard
import com.manzil.app.core.design.ManzilScreenScaffold
import com.manzil.app.core.design.ManzilTextField
import com.manzil.app.core.design.Pill
import com.manzil.app.core.design.SectionHeader
import com.manzil.app.core.design.SegmentedControl
import com.manzil.app.core.design.rememberDatePicker
import com.manzil.app.core.theme.ManzilColors

@Composable
fun JournalScreen(
    onBack: () -> Unit,
    onSearch: () -> Unit,
    viewModel: JournalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pickDate = rememberDatePicker(state.date) { date ->
        if (date != null) viewModel.selectDate(date)
    }

    ManzilScreenScaffold(
        title = "Journal",
        subtitle = Fmt.dateLong(state.date),
        onBack = onBack,
        onSearch = onSearch,
        actions = {
            TextButton(onClick = pickDate) { Text("Date") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SegmentedControl(
                    options = listOf("Today", "History", "Weekly review"),
                    selectedIndex = state.tabIndex,
                    onSelect = viewModel::setTab
                )
            }

            state.banner?.let {
                item { InfoBanner(text = it, color = ManzilColors.success, onClose = viewModel::dismissBanner) }
            }

            when (state.tabIndex) {
                0 -> {
                    item { MoodCard(state.mood, viewModel::setMood) }
                    item {
                        ManzilCard {
                            SectionHeader(title = "Auto summary")
                            Spacer(Modifier.height(6.dp))
                            Text(
                                state.prompt,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    item {
                        ManzilCard {
                            ManzilTextField(
                                value = state.wins,
                                onValueChange = viewModel::updateWins,
                                label = "Wins",
                                placeholder = "What actually worked today?",
                                singleLine = false,
                                minLines = 2
                            )
                        }
                    }
                    item {
                        ManzilCard {
                            ManzilTextField(
                                value = state.blockers,
                                onValueChange = viewModel::updateBlockers,
                                label = "Blockers",
                                placeholder = "What stopped you?",
                                singleLine = false,
                                minLines = 2
                            )
                        }
                    }
                    item {
                        ManzilCard {
                            ManzilTextField(
                                value = state.note,
                                onValueChange = viewModel::updateNote,
                                label = "Notes for future you",
                                singleLine = false,
                                minLines = 3
                            )
                        }
                    }
                    item {
                        Button(onClick = viewModel::save, shape = RoundedCornerShape(14.dp)) {
                            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Save journal")
                        }
                    }
                }
                1 -> {
                    if (state.history.isEmpty() && !state.loading) {
                        item {
                            EmptyState(
                                icon = Icons.Filled.MenuBook,
                                title = "No entries yet",
                                message = "Two minutes of honesty a day beats an hour of planning a month."
                            )
                        }
                    }
                    items(state.history, key = { it.id }) { entry ->
                        ManzilCard(onClick = { viewModel.selectDate(entry.date); viewModel.setTab(0) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Mood ${entry.mood}/5",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ManzilColors.warning
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    Fmt.dateLong(entry.date),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (entry.wins.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text("Won: ${entry.wins}", style = MaterialTheme.typography.bodySmall)
                            }
                            if (entry.blockers.isNotBlank()) {
                                Text(
                                    "Stuck: ${entry.blockers}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ManzilColors.warning
                                )
                            }
                        }
                    }
                }
                else -> {
                    val weekly = state.weekly
                    item {
                        ManzilCard {
                            SectionHeader(title = "This week at a glance")
                            Spacer(Modifier.height(10.dp))
                            KeyValueRow("Tasks done", "${weekly.done} of ${weekly.planned}")
                            KeyValueRow("Still open", weekly.overdue.toString(), ManzilColors.warning)
                            KeyValueRow("Focus time", Fmt.duration(weekly.focusMinutes))
                            KeyValueRow("Average day score", "${weekly.averageScore}%")
                            KeyValueRow("Habit consistency", "${weekly.habitPercent}%")
                        }
                    }
                    item {
                        ManzilCard {
                            SectionHeader(title = "Next week's three priorities")
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Tap any one of them to put it on today's list.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                            weekly.priorities.forEach { priority ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.createPriorityTask(priority) }
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(priority, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    if (weekly.goalChanges.isNotEmpty()) {
                        item {
                            ManzilCard {
                                SectionHeader(title = "Goal changes this week")
                                Spacer(Modifier.height(8.dp))
                                weekly.goalChanges.forEach { change ->
                                    Text("• $change", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    if (weekly.wins.isNotEmpty() || weekly.blockers.isNotEmpty()) {
                        item {
                            ManzilCard {
                                SectionHeader(title = "From your journals")
                                Spacer(Modifier.height(8.dp))
                                weekly.wins.forEach { Text("✅ $it", style = MaterialTheme.typography.bodySmall) }
                                weekly.blockers.forEach {
                                    Text("⚠️ $it", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                    item {
                        TextButton(onClick = viewModel::refreshWeekly) { Text("Refresh summary") }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodCard(mood: Int, onMood: (Int) -> Unit) {
    val labels = listOf("Rough", "Meh", "Okay", "Good", "Great")
    ManzilCard {
        SectionHeader(title = "How did the day feel?")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..5).forEach { value ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (value == mood) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onMood(value) }
                ) {
                    Text(
                        text = value.toString(),
                        modifier = Modifier.padding(vertical = 12.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (value == mood) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Pill(text = labels.getOrElse(mood - 1) { "Okay" }, color = ManzilColors.teal)
    }
}
