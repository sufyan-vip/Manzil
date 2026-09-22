@file:OptIn(ExperimentalMaterial3Api::class)

package com.manzil.app.feature.goals

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manzil.app.core.common.Fmt
import com.manzil.app.core.design.ChipRow
import com.manzil.app.core.design.EmptyState
import com.manzil.app.core.design.ManzilCard
import com.manzil.app.core.design.ManzilScreenScaffold
import com.manzil.app.core.design.Pill
import com.manzil.app.core.design.SectionHeader
import com.manzil.app.core.theme.ManzilColors
import com.manzil.app.data.local.entity.ChangeType
import com.manzil.app.domain.model.PulseItem

@Composable
fun GoalPulseScreen(
    onBack: () -> Unit,
    onSearch: () -> Unit,
    viewModel: GoalPulseViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ManzilScreenScaffold(
        title = "Goal Pulse",
        subtitle = "What changed in your plan",
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
            item {
                ChipRow(
                    options = listOf("This week", "This month", "All"),
                    selectedIndex = state.filterIndex,
                    onSelect = viewModel::setFilter
                )
            }

            item {
                ManzilCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    SectionHeader(title = "Explain what changed")
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Turn the diff feed into 3 plain lines — local summary first, AI polish when a key is set.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = viewModel::explainChanges, shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)) {
                        if (state.explaining) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                        }
                        Text("Explain changes")
                    }
                    state.explain?.let { text ->
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        TextButton(onClick = viewModel::dismissExplain) { Text("Hide") }
                    }
                }
            }

            if (state.items.isEmpty() && !state.loading) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Insights,
                        title = "No changes recorded yet",
                        message = "Edit a goal, close a milestone or log a metric reading — it appears here instantly.",
                        actionLabel = "Back to goals",
                        onAction = onBack
                    )
                }
            }

            items(state.items, key = { it.id }) { item ->
                PulseCard(item)
            }
        }
    }
}

@Composable
private fun PulseCard(item: PulseItem) {
    val accent = when (item.changeType) {
        ChangeType.CREATED -> ManzilColors.success
        ChangeType.UPDATED -> ManzilColors.info
        ChangeType.PROGRESS -> ManzilColors.teal
        ChangeType.METRIC -> ManzilColors.warning
        ChangeType.STATUS -> ManzilColors.warning
        ChangeType.DELETED -> ManzilColors.danger
    }
    ManzilCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Pill(text = item.changeType.name.lowercase(), color = accent)
            Spacer(Modifier.width(8.dp))
            Text(
                text = Fmt.dateShort(Fmt.dayOfMillis(item.atMillis)) + " · " + Fmt.time(item.atMillis),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            item.goalTitle,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        if (item.changes.isEmpty()) {
            Text(
                item.note ?: when (item.changeType) {
                    ChangeType.CREATED -> "New goal added"
                    ChangeType.DELETED -> "Goal removed"
                    else -> "Updated"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            item.changes.take(4).forEach { change ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        com.manzil.app.core.diff.JsonDiff.label(change.field),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    if (change.from.isNotBlank()) {
                        Text(
                            change.from,
                            style = MaterialTheme.typography.labelSmall,
                            color = ManzilColors.danger,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        "→ ${change.to}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ManzilColors.success,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
