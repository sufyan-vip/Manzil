@file:OptIn(ExperimentalMaterial3Api::class)

package com.manzil.app.feature.tools

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manzil.app.core.design.InfoBanner
import com.manzil.app.core.design.ManzilCard
import com.manzil.app.core.design.ManzilScreenScaffold
import com.manzil.app.core.design.Pill
import com.manzil.app.core.design.SectionHeader
import com.manzil.app.core.theme.ManzilColors

@Composable
fun ClientHuntScreen(
    onBack: () -> Unit,
    onSearch: () -> Unit,
    viewModel: ToolsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ManzilScreenScaffold(
        title = "Client hunt",
        subtitle = "Free methods — no Fiverr fees, no cold spam",
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
                ManzilCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Groups,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Today's action: ${state.todayAction.platform}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    LabelledLine("Find them", state.todayAction.searchQuery)
                    LabelledLine("Say this", state.todayAction.script)
                    LabelledLine("Follow up", state.todayAction.followUp)
                    LabelledLine("Expect", state.todayAction.expected)
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.addClientTask() },
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Add as today's task")
                    }
                }
            }

            item {
                ManzilCard {
                    SectionHeader(title = "Why this works")
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "Direct clients pay 100% — marketplaces take 20%.",
                        "Local businesses reply faster than global job boards.",
                        "A free audit opens the door; the retainer keeps it open.",
                        "Post in public daily and inbound leads replace cold outreach."
                    ).forEach { line ->
                        Text("• $line", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }

            item { SectionHeader(title = "Platform playbook") }

            items(state.playbook) { entry ->
                ManzilCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(entry.platform, style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.width(8.dp))
                        Pill(text = entry.cadence, color = ManzilColors.info)
                    }
                    Spacer(Modifier.height(8.dp))
                    LabelledLine("Where", entry.where)
                    LabelledLine("Script", entry.script)
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.addTask(entry.taskTitle) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Add task: ${entry.taskTitle.take(28)}…")
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelledLine(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(72.dp)
        )
        Text(value, style = MaterialTheme.typography.bodySmall)
    }
}
