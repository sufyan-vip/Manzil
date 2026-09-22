@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.manzil.app.feature.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manzil.app.core.common.Fmt
import com.manzil.app.core.design.ChipRow
import com.manzil.app.core.design.EmptyState
import com.manzil.app.core.design.InfoBanner
import com.manzil.app.core.design.KeyValueRow
import com.manzil.app.core.design.ManzilCard
import com.manzil.app.core.design.ManzilScreenScaffold
import com.manzil.app.core.design.ManzilSheet
import com.manzil.app.core.design.ManzilTextField
import com.manzil.app.core.design.Pill
import com.manzil.app.core.design.ProgressRing
import com.manzil.app.core.design.SectionHeader
import com.manzil.app.core.design.rememberDatePicker
import com.manzil.app.core.theme.ManzilColors
import com.manzil.app.data.local.entity.Goal
import com.manzil.app.data.local.entity.GoalCategory
import com.manzil.app.data.local.entity.GoalStatus
import com.manzil.app.domain.model.GoalNode

@Composable
fun GoalsScreen(
    onSearch: () -> Unit,
    onOpenPulse: () -> Unit,
    onOpenKpi: () -> Unit,
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ManzilScreenScaffold(
        title = "Goals",
        subtitle = "${state.activeCount} active · overall ${state.overallProgress}%",
        onSearch = onSearch,
        actions = {
            TextButton(onClick = onOpenPulse) { Text("Pulse") }
            TextButton(onClick = onOpenKpi) { Text("KPI") }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openNewGoal() }) {
                Icon(Icons.Filled.Add, contentDescription = "New goal")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            state.banner?.let {
                item { InfoBanner(text = it, color = ManzilColors.success, onClose = viewModel::dismissBanner) }
            }

            if (state.tree.isEmpty() && !state.loading) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Flag,
                        title = "No goals yet",
                        message = "A goal with a date and a number attached is a plan. Everything else is a wish.",
                        actionLabel = "Add your first goal",
                        onAction = { viewModel.openNewGoal() }
                    )
                }
            }

            state.tree.forEach { node ->
                item(key = "goal_${node.goal.id}") {
                    GoalNodeCard(
                        node = node,
                        expanded = state.expanded.contains(node.goal.id),
                        onToggleExpand = { viewModel.toggleExpanded(node.goal.id) },
                        onEdit = { viewModel.openGoal(node.goal) },
                        onAddMilestone = { viewModel.openMilestoneDraft(node.goal.id) },
                        onToggleMilestone = viewModel::toggleMilestone,
                        onDeleteMilestone = viewModel::deleteMilestone,
                        onUpdateMetric = { viewModel.openMetric(node.goal) },
                        onAddChild = { viewModel.openNewGoal(node.goal.id) },
                        onComplete = { viewModel.setStatus(node.goal, GoalStatus.DONE) },
                        onPause = {
                            viewModel.setStatus(
                                node.goal,
                                if (node.goal.status == GoalStatus.PAUSED) GoalStatus.ACTIVE else GoalStatus.PAUSED
                            )
                        }
                    )
                }
            }
        }
    }

    state.draft?.let { draft ->
        ManzilSheet(onDismiss = viewModel::closeDraft) {
            GoalEditor(
                draft = draft,
                goals = state.tree.flatMap { flatten(it) }.map { it.goal },
                onUpdate = viewModel::updateDraft,
                onSave = viewModel::saveDraft,
                onDelete = {
                    state.tree.flatMap { flatten(it) }.firstOrNull { it.goal.id == draft.id }
                        ?.let { viewModel.deleteGoal(it.goal) }
                }
            )
        }
    }

    state.milestoneDraft?.let { draft ->
        ManzilSheet(onDismiss = viewModel::closeMilestoneDraft) {
            val pickDate = rememberDatePicker(draft.dueDate) { date ->
                viewModel.updateMilestoneDraft { it.copy(dueDate = date) }
            }
            Text("New milestone", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(14.dp))
            ManzilTextField(
                value = draft.title,
                onValueChange = { value -> viewModel.updateMilestoneDraft { it.copy(title = value) } },
                label = "Milestone",
                placeholder = "React hooks solid — CS50 week 3 done"
            )
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = pickDate) {
                Text("Due: ${draft.dueDate?.let { Fmt.dateShort(it) } ?: "not set"}")
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = viewModel::saveMilestone, shape = RoundedCornerShape(14.dp)) {
                Text("Add milestone")
            }
        }
    }

    state.metricGoal?.let { goal ->
        ManzilSheet(onDismiss = viewModel::closeMetric) {
            Text("Update ${goal.metricLabel ?: "metric"}", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(
                "Current ${goal.metricCurrent} of ${goal.metricTarget ?: 0.0} ${goal.metricUnit ?: ""}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            ManzilTextField(
                value = state.metricValue,
                onValueChange = viewModel::updateMetricValue,
                label = "New value",
                placeholder = "25000"
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = viewModel::saveMetric, shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save reading")
            }
        }
    }
}

private fun flatten(node: GoalNode): List<GoalNode> = listOf(node) + node.children.flatMap { flatten(it) }

@Composable
private fun GoalNodeCard(
    node: GoalNode,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onEdit: () -> Unit,
    onAddMilestone: () -> Unit,
    onToggleMilestone: (com.manzil.app.data.local.entity.Milestone) -> Unit,
    onDeleteMilestone: (com.manzil.app.data.local.entity.Milestone) -> Unit,
    onUpdateMetric: () -> Unit,
    onAddChild: () -> Unit,
    onComplete: () -> Unit,
    onPause: () -> Unit,
    depth: Int = 0
) {
    val goal = node.goal
    ManzilCard(
        containerColor = if (depth == 0) {
            MaterialTheme.colorScheme.surface
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressRing(
                progress = node.progress / 100f,
                size = 46.dp,
                strokeWidth = 5.dp,
                centerLabel = "",
                color = when {
                    node.isSlipping -> ManzilColors.danger
                    goal.status == GoalStatus.DONE -> ManzilColors.success
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (goal.status == GoalStatus.DONE) TextDecoration.LineThrough else null
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${node.progress}%  ·  ${node.tasksDone}/${node.tasksTotal} tasks" +
                            if (node.milestones.isNotEmpty()) "  ·  ${node.milestones.count { it.done }}/${node.milestones.size} ms" else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Pill(text = goal.category.name.lowercase(), color = MaterialTheme.colorScheme.primary)
                    if (node.isSlipping) {
                        Pill(text = "slipping ${node.daysLate}d", color = ManzilColors.danger)
                    }
                    if (goal.status == GoalStatus.PAUSED) {
                        Pill(text = "paused", color = ManzilColors.warning)
                    }
                    goal.targetDate?.let {
                        Pill(text = "by ${Fmt.dateShort(it)}", color = ManzilColors.info)
                    }
                }
            }
            IconButton(onClick = onToggleExpand) {
                Icon(
                    if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand"
                )
            }
        }

        if (goal.metricTarget != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                "${goal.metricLabel ?: "Metric"}: ${Fmt.kpiDisplay(goal.metricCurrent, goal.metricUnit)} of " +
                    "${Fmt.kpiDisplay(goal.metricTarget, goal.metricUnit)}",
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = {
                    if (goal.metricTarget <= 0.0) 0f
                    else (goal.metricCurrent / goal.metricTarget).toFloat().coerceIn(0f, 1f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onUpdateMetric) {
                Icon(Icons.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add reading")
            }
        }

        if (expanded) {
            Spacer(Modifier.height(10.dp))
            SectionHeader(title = "Milestones", trailing = "Add", onTrailingClick = onAddMilestone)
            if (node.milestones.isEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "No milestones yet — break this goal into 3 checkpoints.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            node.milestones.forEach { milestone ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onToggleMilestone(milestone) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (milestone.done) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (milestone.done) ManzilColors.success else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            milestone.title,
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = if (milestone.done) TextDecoration.LineThrough else null
                        )
                        milestone.dueDate?.let {
                            Text(
                                "due ${Fmt.dateShort(it)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = { onDeleteMilestone(milestone) }, modifier = Modifier.size(30.dp)) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete milestone",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            if (node.children.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                SectionHeader(title = "Sub-goals")
                Spacer(Modifier.height(6.dp))
                node.children.forEach { child ->
                    GoalNodeCard(
                        node = child,
                        expanded = false,
                        onToggleExpand = onToggleExpand,
                        onEdit = onEdit,
                        onAddMilestone = onAddMilestone,
                        onToggleMilestone = onToggleMilestone,
                        onDeleteMilestone = onDeleteMilestone,
                        onUpdateMetric = onUpdateMetric,
                        onAddChild = onAddChild,
                        onComplete = onComplete,
                        onPause = onPause,
                        depth = depth + 1
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onAddChild) { Text("Sub-goal") }
                TextButton(onClick = onPause) {
                    Text(if (goal.status == GoalStatus.PAUSED) "Resume" else "Pause")
                }
                TextButton(onClick = onComplete) {
                    Text("Mark done", color = ManzilColors.success)
                }
            }
        }
    }
}

@Composable
private fun GoalEditor(
    draft: GoalDraft,
    goals: List<Goal>,
    onUpdate: ((GoalDraft) -> GoalDraft) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    val pickDate = rememberDatePicker(draft.targetDate) { date ->
        onUpdate { it.copy(targetDate = date) }
    }
    Text(if (draft.isNew) "New goal" else "Edit goal", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(14.dp))
    ManzilTextField(
        value = draft.title,
        onValueChange = { value -> onUpdate { it.copy(title = value) } },
        label = "Goal",
        placeholder = "First 3 paying clients"
    )
    Spacer(Modifier.height(10.dp))
    ManzilTextField(
        value = draft.description,
        onValueChange = { value -> onUpdate { it.copy(description = value) } },
        label = "Why it matters",
        singleLine = false,
        minLines = 2
    )

    Spacer(Modifier.height(14.dp))
    SectionHeader(title = "Category")
    Spacer(Modifier.height(8.dp))
    ChipRow(
        options = GoalCategory.entries.map { it.name.lowercase() },
        selectedIndex = GoalCategory.entries.indexOf(draft.category).coerceAtLeast(0),
        onSelect = { index -> onUpdate { it.copy(category = GoalCategory.entries[index]) } }
    )

    Spacer(Modifier.height(14.dp))
    SectionHeader(title = "Parent goal")
    Spacer(Modifier.height(8.dp))
    ChipRow(
        options = listOf("None") + goals.take(5).map { it.title.take(16) },
        selectedIndex = goals.indexOfFirst { it.id == draft.parentGoalId } + 1,
        onSelect = { index ->
            onUpdate { it.copy(parentGoalId = if (index == 0) null else goals[index - 1].id) }
        }
    )

    Spacer(Modifier.height(14.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = pickDate) {
            Text("Target: ${draft.targetDate?.let { Fmt.dateShort(it) } ?: "open ended"}")
        }
    }

    Spacer(Modifier.height(6.dp))
    SectionHeader(title = "Metric (optional)")
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ManzilTextField(
            value = draft.metricLabel,
            onValueChange = { value -> onUpdate { it.copy(metricLabel = value) } },
            label = "Label",
            modifier = Modifier.weight(1.4f)
        )
        ManzilTextField(
            value = draft.metricTarget,
            onValueChange = { value -> onUpdate { it.copy(metricTarget = value) } },
            label = "Target",
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(Modifier.height(10.dp))
    ManzilTextField(
        value = draft.metricUnit,
        onValueChange = { value -> onUpdate { it.copy(metricUnit = value) } },
        label = "Unit (PKR, clients, %)",
        singleLine = true
    )

    Spacer(Modifier.height(14.dp))
    KeyValueRow(
        label = "Perpetual goal",
        value = if (draft.isPerpetual) "runs till DONE" else "fixed deadline"
    )
    TextButton(onClick = { onUpdate { it.copy(isPerpetual = !it.isPerpetual) } }) {
        Text("Toggle perpetual mode")
    }

    Spacer(Modifier.height(18.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = onSave, shape = RoundedCornerShape(14.dp)) {
            Text(if (draft.isNew) "Add goal" else "Save changes")
        }
        if (!draft.isNew) {
            TextButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = ManzilColors.danger
                )
                Spacer(Modifier.width(6.dp))
                Text("Delete", color = ManzilColors.danger)
            }
        }
    }
    Spacer(Modifier.height(10.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}
