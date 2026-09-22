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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manzil.app.core.common.FreeToolsGuide
import com.manzil.app.core.design.ManzilCard
import com.manzil.app.core.design.ManzilScreenScaffold
import com.manzil.app.core.design.Pill
import com.manzil.app.core.design.SectionHeader
import com.manzil.app.core.design.SegmentedControl
import com.manzil.app.core.theme.ManzilColors

@Composable
fun FreeToolsScreen(
    onBack: () -> Unit,
    onSearch: () -> Unit,
    viewModel: ToolsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(0) }

    ManzilScreenScaffold(
        title = "Free tools",
        subtitle = "Zero rupee path from here to the goal",
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
                SegmentedControl(
                    options = listOf("Roadmap", "Tools"),
                    selectedIndex = tab,
                    onSelect = { tab = it }
                )
            }

            if (tab == 0) {
                item {
                    ManzilCard {
                        SectionHeader(title = "12 week zero-to-goal plan")
                        Spacer(Modifier.height(10.dp))
                        Text(
                            state.roadmap,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            } else {
                toolGroup("Development", FreeToolsGuide.development)
                toolGroup("Learning", FreeToolsGuide.learning)
                toolGroup("Client hunting", FreeToolsGuide.clientHuntingFree)
                toolGroup("Business & money", FreeToolsGuide.businessFree)
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.toolGroup(
    title: String,
    tools: List<FreeToolsGuide.FreeTool>
) {
    item { SectionHeader(title = title) }
    items(tools, key = { it.name }) { tool ->
        ManzilCard {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(tool.name, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        tool.useFor,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        tool.link,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(8.dp))
                Pill(
                    text = tool.cost,
                    color = if (tool.cost.equals("Free", ignoreCase = true)) {
                        ManzilColors.success
                    } else {
                        ManzilColors.warning
                    }
                )
            }
        }
    }
}
