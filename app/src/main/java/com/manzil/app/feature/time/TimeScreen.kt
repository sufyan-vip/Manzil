@file:OptIn(ExperimentalMaterial3Api::class)

package com.manzil.app.feature.time

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manzil.app.core.common.Fmt
import com.manzil.app.core.design.InfoBanner
import com.manzil.app.core.design.KeyValueRow
import com.manzil.app.core.design.ManzilCard
import com.manzil.app.core.design.ManzilScreenScaffold
import com.manzil.app.core.design.ManzilTextField
import com.manzil.app.core.design.MiniBarChart
import com.manzil.app.core.design.Pill
import com.manzil.app.core.design.SectionHeader
import com.manzil.app.core.theme.ManzilColors

@Composable
fun TimeScreen(
    onBack: () -> Unit,
    onSearch: () -> Unit,
    viewModel: TimeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val stats = state.stats
    val weeklyTargetMinutes = state.weeklyTargetHours * 60

    ManzilScreenScaffold(
        title = "Time tracking",
        subtitle = "${Fmt.duration(stats.todayMinutes)} today · ${Fmt.duration(stats.sevenDayAverageMinutes)} daily average",
        onBack = onBack,
        onSearch = onSearch
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            state.banner?.let {
                item { InfoBanner(text = it, color = ManzilColors.success, onClose = viewModel::dismissBanner) }
            }

            item {
                ManzilCard {
                    SectionHeader(title = "Last 14 days")
                    Spacer(Modifier.height(10.dp))
                    MiniBarChart(
                        values = stats.last14Days.map { it.second },
                        labels = stats.last14Days.map { Fmt.dateShort(it.first).substringBefore(' ') },
                        target = 120
                    )
                }
            }

            item {
                ManzilCard {
                    SectionHeader(title = "This week")
                    Spacer(Modifier.height(10.dp))
                    KeyValueRow("Focused", Fmt.duration(stats.weekMinutes))
                    KeyValueRow("Target", "${state.weeklyTargetHours}h / week")
                    KeyValueRow(
                        "Remaining",
                        Fmt.duration((weeklyTargetMinutes - stats.weekMinutes).coerceAtLeast(0)),
                        if (stats.weekMinutes >= weeklyTargetMinutes) ManzilColors.success else ManzilColors.warning
                    )
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = {
                            if (weeklyTargetMinutes <= 0) 0f
                            else (stats.weekMinutes.toFloat() / weeklyTargetMinutes).coerceIn(0f, 1f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                    )
                }
            }

            if (stats.perGoal.isNotEmpty()) {
                item {
                    ManzilCard {
                        SectionHeader(title = "Where the time went")
                        Spacer(Modifier.height(10.dp))
                        stats.perGoal.forEach { (goalId, minutes) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    state.goalTitles[goalId] ?: "Ad-hoc work",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    Fmt.duration(minutes),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            item {
                ManzilCard {
                    SectionHeader(title = "Log time manually")
                    Spacer(Modifier.height(10.dp))
                    ManzilTextField(
                        value = state.manualLabel,
                        onValueChange = viewModel::updateManualLabel,
                        label = "What did you work on?",
                        placeholder = "Client call, React course…"
                    )
                    Spacer(Modifier.height(10.dp))
                    ManzilTextField(
                        value = state.manualMinutes,
                        onValueChange = viewModel::updateManualMinutes,
                        label = "Minutes",
                        placeholder = "30"
                    )
                    Spacer(Modifier.height(14.dp))
                    Button(onClick = viewModel::addManualEntry, shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add entry")
                    }
                }
            }

            item { SectionHeader(title = "Ledger") }

            if (state.entries.isEmpty() && !state.loading) {
                item {
                    ManzilCard {
                        Text(
                            "No time tracked yet. Start the focus timer on TODAY — every session lands here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(state.entries, key = { it.id }) { entry ->
                ManzilCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(entry.label, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                buildString {
                                    append(Fmt.dateShort(Fmt.dayOfMillis(entry.startedAt)))
                                    append("  ·  ").append(Fmt.time(entry.startedAt))
                                    entry.endedAt?.let {
                                        val minutes = ((it - entry.startedAt) / 60000L).toInt()
                                        append("  ·  ").append(Fmt.duration(minutes))
                                    }
                                    entry.goalId?.let { goalId ->
                                        state.goalTitles[goalId]?.let { append("  ·  ").append(it) }
                                    }
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Pill(
                            text = entry.source.name.lowercase(),
                            color = ManzilColors.info
                        )
                        IconButton(onClick = { viewModel.startTimerFromEntry(entry) }) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = "Start a timer with this label",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(onClick = { viewModel.deleteEntry(entry) }) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete entry",
                                modifier = Modifier.size(18.dp),
                                tint = ManzilColors.danger
                            )
                        }
                    }
                }
            }
        }
    }
}
