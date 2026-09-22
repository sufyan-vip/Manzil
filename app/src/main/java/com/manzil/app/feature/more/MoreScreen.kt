@file:OptIn(ExperimentalMaterial3Api::class)

package com.manzil.app.feature.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.manzil.app.core.design.ManzilCard
import com.manzil.app.core.design.ManzilScreenScaffold
import com.manzil.app.core.design.SectionHeader
import com.manzil.app.core.theme.ManzilColors

@Composable
fun MoreScreen(
    onOpen: (String) -> Unit,
    onSearch: () -> Unit
) {
    ManzilScreenScaffold(
        title = "More",
        subtitle = "Everything else Manzil can do",
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
                ManzilCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        "Goal Pulse is the heart of Manzil",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "It shows exactly what changed in your plan — deadlines moved, milestones closed, metrics updated.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            item { SectionHeader(title = "Track") }
            item {
                MoreRow(Icons.Filled.Insights, "Goal Pulse", "What changed in your goals", ManzilColors.info) {
                    onOpen("pulse")
                }
            }
            item {
                MoreRow(Icons.Filled.TrendingUp, "KPI dashboard", "Targets from your plan, with trends", ManzilColors.success) {
                    onOpen("kpi")
                }
            }
            item {
                MoreRow(Icons.Filled.Timer, "Time tracking", "Focus ledger, 14-day chart, weekly target", ManzilColors.warning) {
                    onOpen("time")
                }
            }
            item {
                MoreRow(Icons.Filled.MenuBook, "Journal & weekly review", "Mood, wins, blockers, next 3 priorities", ManzilColors.teal) {
                    onOpen("journal")
                }
            }

            item { SectionHeader(title = "Grow") }
            item {
                MoreRow(Icons.Filled.Groups, "Client hunt", "Free playbook — Instagram, FB, LinkedIn, X", ManzilColors.info) {
                    onOpen("clients")
                }
            }
            item {
                MoreRow(Icons.Filled.Build, "Free tools", "12-week zero-to-goal plan, all free tools", ManzilColors.success) {
                    onOpen("tools")
                }
            }
            item {
                MoreRow(Icons.Filled.AutoAwesome, "AI coach", "Plan my day, weekly review, unblock me", ManzilColors.warning) {
                    onOpen("ai")
                }
            }
            item {
                MoreRow(Icons.Filled.Upload, "Roadmap import", "Paste markdown → goals, tasks, KPIs", ManzilColors.teal) {
                    onOpen("import")
                }
            }

            item { SectionHeader(title = "App") }
            item {
                MoreRow(Icons.Filled.Search, "Search everything", "Offline instant search", MaterialTheme.colorScheme.primary) {
                    onSearch()
                }
            }
            item {
                MoreRow(Icons.Filled.Settings, "Settings", "AI key, notifications, theme, backup", MaterialTheme.colorScheme.onSurfaceVariant) {
                    onOpen("settings")
                }
            }

            item { Spacer(Modifier.height(10.dp)) }
            item {
                Text(
                    "Manzil works offline. The only network call it ever makes is to OpenRouter, with your own key.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MoreRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    ManzilCard(onClick = onClick, contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
