@file:OptIn(ExperimentalMaterial3Api::class)

package com.manzil.app.feature.importer

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manzil.app.core.design.InfoBanner
import com.manzil.app.core.design.KeyValueRow
import com.manzil.app.core.design.ManzilCard
import com.manzil.app.core.design.ManzilScreenScaffold
import com.manzil.app.core.design.ManzilTextField
import com.manzil.app.core.design.SectionHeader
import com.manzil.app.core.theme.ManzilColors

@Composable
fun RoadmapImportScreen(
    onBack: () -> Unit,
    onSearch: () -> Unit,
    viewModel: RoadmapImportViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ManzilScreenScaffold(
        title = "Roadmap import",
        subtitle = "Paste markdown → goals + tasks + KPIs",
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
                    SectionHeader(title = "How it works")
                    Spacer(Modifier.height(8.dp))
                    listOf(
                        "## Heading becomes a goal",
                        "### Sub-heading becomes a sub-goal",
                        "- [ ] checklist line becomes a task",
                        "table row with a number becomes a KPI"
                    ).forEach {
                        Text("• $it", style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(3.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = viewModel::loadSample, shape = RoundedCornerShape(12.dp)) {
                            Text("Load sample roadmap")
                        }
                        OutlinedButton(onClick = viewModel::loadStarterPlan, shape = RoundedCornerShape(12.dp)) {
                            Text("Load free tools plan")
                        }
                    }
                }
            }

            item {
                ManzilCard {
                    ManzilTextField(
                        value = state.markdown,
                        onValueChange = viewModel::updateMarkdown,
                        label = "Markdown",
                        placeholder = "## Phase 1 — Foundation\n- [ ] Set up GitHub\n- [ ] freeCodeCamp RWD",
                        singleLine = false,
                        minLines = 8
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = viewModel::parsePreview, shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Filled.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Preview import")
                    }
                }
            }

            if (state.parsed) {
                item {
                    ManzilCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        SectionHeader(title = "Preview")
                        Spacer(Modifier.height(8.dp))
                        KeyValueRow("Goals", state.goals.size.toString())
                        KeyValueRow("Tasks", state.tasks.size.toString())
                        KeyValueRow("KPIs", state.kpis.size.toString())
                        Spacer(Modifier.height(10.dp))
                        if (state.goals.isNotEmpty()) {
                            Text("First goals", style = MaterialTheme.typography.labelMedium)
                            state.goals.take(5).forEach {
                                Text("• ${it.title}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = viewModel::commit,
                            enabled = state.goals.isNotEmpty() || state.tasks.isNotEmpty(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Import into Manzil")
                        }
                    }
                }

                if (state.tasks.isNotEmpty()) {
                    item { SectionHeader(title = "Tasks found") }
                    items(state.tasks.take(40).size) { index ->
                        val task = state.tasks[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                if (task.done) "☑" else "☐",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (task.done) ManzilColors.success else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                task.title,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Default
                            )
                        }
                    }
                }
            }
        }
    }
}
