@file:OptIn(ExperimentalMaterial3Api::class)

package com.manzil.app.feature.search

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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manzil.app.core.design.ChipRow
import com.manzil.app.core.design.EmptyState
import com.manzil.app.core.design.ManzilCard
import com.manzil.app.core.design.ManzilScreenScaffold
import com.manzil.app.core.design.ManzilTextField
import com.manzil.app.core.design.Pill
import com.manzil.app.core.design.SectionHeader
import com.manzil.app.core.theme.ManzilColors
import com.manzil.app.domain.model.SearchHit

@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onCreateTask: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ManzilScreenScaffold(
        title = "Search",
        subtitle = if (state.query.length >= 2) {
            "${state.hits.size} results · ${state.tookMillis}ms · fully offline"
        } else {
            "Goals · tasks · events · journal"
        },
        onBack = onBack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                ManzilTextField(
                    value = state.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = "Search everything…",
                    leadingIcon = Icons.Filled.Search,
                    trailing = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = viewModel::clearQuery) {
                                Icon(
                                    Icons.Filled.Close,
                                    contentDescription = "Clear",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                )
                Spacer(Modifier.height(10.dp))
                ChipRow(
                    options = SearchViewModel.FILTERS,
                    selectedIndex = state.filterIndex,
                    onSelect = viewModel::setFilter
                )
                Spacer(Modifier.height(10.dp))
            }

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.query.length < 2) {
                    if (state.recent.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "Recent searches",
                                trailing = "Clear",
                                onTrailingClick = viewModel::clearRecent
                            )
                        }
                        items(state.recent) { recent ->
                            ManzilCard(onClick = { viewModel.onQueryChange(recent) }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(recent, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    } else {
                        item {
                            EmptyState(
                                icon = Icons.Filled.Category,
                                title = "Type at least two letters",
                                message = "Search runs on your device — instant, private, works on a plane."
                            )
                        }
                    }
                } else if (state.hits.isEmpty() && !state.searching) {
                    item {
                        EmptyState(
                            icon = Icons.Filled.Search,
                            title = "Nothing matched \"${state.query}\"",
                            message = "Create it as a task instead and it will be searchable right away.",
                            actionLabel = "Create \"${state.query.take(28)}\"",
                            onAction = { onCreateTask(state.query) }
                        )
                    }
                }

                items(state.hits, key = { it.type + it.id }) { hit ->
                    SearchHitRow(hit = hit, onCreateTask = { onCreateTask(hit.title) })
                }
            }
        }
    }
}

@Composable
private fun SearchHitRow(hit: SearchHit, onCreateTask: () -> Unit) {
    val (icon, color) = when (hit.type) {
        "GOAL" -> Icons.Filled.Flag to ManzilColors.teal
        "MILESTONE" -> Icons.Filled.CheckCircle to ManzilColors.success
        "TASK" -> Icons.Filled.CheckCircle to ManzilColors.info
        "EVENT" -> Icons.Filled.CalendarMonth to ManzilColors.warning
        else -> Icons.Filled.MenuBook to MaterialTheme.colorScheme.tertiary
    }
    ManzilCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    hit.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (hit.snippet.isNotBlank()) {
                    Text(
                        hit.snippet,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Pill(text = hit.type.lowercase(), color = color)
        }
    }
}
