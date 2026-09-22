@file:OptIn(ExperimentalMaterial3Api::class)

package com.manzil.app.feature.goals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.manzil.app.core.design.ManzilCard
import com.manzil.app.core.design.ManzilScreenScaffold
import com.manzil.app.core.design.ManzilSheet
import com.manzil.app.core.design.ManzilTextField
import com.manzil.app.core.design.Sparkline
import com.manzil.app.core.theme.ManzilColors
import com.manzil.app.domain.model.KpiCard

@Composable
fun KpiScreen(
    onBack: () -> Unit,
    onSearch: () -> Unit,
    viewModel: KpiViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ManzilScreenScaffold(
        title = "KPI dashboard",
        subtitle = "Targets from your plan",
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

            items(state.cards, key = { it.key }) { card ->
                KpiCardView(card = card, onAdd = { viewModel.startEditing(card.key, card.label) })
            }
        }
    }

    state.editingKey?.let { key ->
        ManzilSheet(onDismiss = viewModel::cancelEditing) {
            Text("Add reading — ${state.editingLabel ?: key}", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(14.dp))
            ManzilTextField(
                value = state.value,
                onValueChange = viewModel::updateValue,
                label = "Value",
                placeholder = "25000"
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = viewModel::save, shape = RoundedCornerShape(14.dp)) {
                Text("Save reading")
            }
        }
    }
}

@Composable
private fun KpiCardView(card: KpiCard, onAdd: () -> Unit) {
    ManzilCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(card.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(
                    "${Fmt.kpiDisplay(card.current, card.unit)} of ${Fmt.kpiDisplay(card.target, card.unit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                Fmt.percent(card.progress),
                style = MaterialTheme.typography.titleMedium,
                color = if (card.progress >= 1f) ManzilColors.success else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = { card.progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        if (card.history.size >= 2) {
            Spacer(Modifier.height(10.dp))
            Sparkline(values = card.history.map { it.second })
            Text(
                "last update ${Fmt.dateShort(card.history.last().first)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(6.dp))
        Button(onClick = onAdd, shape = RoundedCornerShape(12.dp)) {
            androidx.compose.material3.Icon(
                Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Add reading")
        }
    }
}
