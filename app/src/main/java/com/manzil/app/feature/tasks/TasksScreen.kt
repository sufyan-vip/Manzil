@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.manzil.app.feature.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manzil.app.core.common.Fmt
import com.manzil.app.core.design.ChipRow
import com.manzil.app.core.design.EmptyState
import com.manzil.app.core.design.InfoBanner
import com.manzil.app.core.design.ManzilScreenScaffold
import com.manzil.app.core.design.ManzilSheet
import com.manzil.app.core.design.ManzilTextField
import com.manzil.app.core.design.OutlinedPillButton
import com.manzil.app.core.design.SectionHeader
import com.manzil.app.core.design.rememberDatePicker
import com.manzil.app.core.design.rememberTimePicker
import com.manzil.app.core.theme.ManzilColors
import com.manzil.app.feature.today.TaskRow

@Composable
fun TasksScreen(
    onSearch: () -> Unit,
    onOpenCalendar: () -> Unit,
    prefillTitle: String = "",
    viewModel: TasksViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }

    androidx.compose.runtime.LaunchedEffect(prefillTitle) {
        if (prefillTitle.isNotBlank()) viewModel.openNewTask(prefillTitle)
    }

    ManzilScreenScaffold(
        title = "Tasks",
        subtitle = "${state.counts.getOrElse(0) { 0 }} today · ${state.counts.getOrElse(2) { 0 }} overdue",
        onSearch = onSearch,
        snackbarHostState = snackbar,
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openNewTask() }) {
                Icon(Icons.Filled.Add, contentDescription = "New task")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                ChipRow(
                    options = TasksState.FILTERS.mapIndexed { index, label ->
                        "$label (${state.counts.getOrElse(index) { 0 }})"
                    },
                    selectedIndex = state.filterIndex,
                    onSelect = viewModel::selectFilter
                )
                state.banner?.let {
                    Spacer(Modifier.height(10.dp))
                    InfoBanner(text = it, color = ManzilColors.success, onClose = viewModel::dismissBanner)
                }
                Spacer(Modifier.height(10.dp))
            }

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.rows.isEmpty() && !state.loading) {
                    item {
                        EmptyState(
                            icon = Icons.Filled.Checklist,
                            title = when (state.filterIndex) {
                                0 -> "No tasks left today"
                                1 -> "Nothing scheduled ahead"
                                2 -> "Nothing overdue — well played"
                                3 -> "No completed tasks yet"
                                else -> "No tasks yet"
                            },
                            message = "Tasks hold the plan together. Add the next real one.",
                            actionLabel = "New task",
                            onAction = { viewModel.openNewTask() }
                        )
                    }
                }

                items(state.rows, key = { it.task.id }) { row ->
                    Column {
                        TaskRow(
                            dayTask = com.manzil.app.domain.model.DayTask(
                                task = row.task,
                                date = row.task.dueDate ?: Fmt.today(),
                                done = row.done,
                                completedAt = row.task.completedAt,
                                fromInstance = false
                            ),
                            goalTitle = row.goalTitle,
                            onToggle = { viewModel.toggleDone(row) },
                            onPostpone = { viewModel.postponeToTomorrow(row) },
                            onClick = { viewModel.openTask(row.task) }
                        )
                        row.overdueDays.takeIf { it > 0 }?.let { days ->
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "  Slipping · $days day${if (days > 1) "s" else ""} late",
                                style = MaterialTheme.typography.labelSmall,
                                color = ManzilColors.danger
                            )
                        }
                    }
                }
            }
        }
    }

    state.draft?.let { draft ->
        ManzilSheet(onDismiss = viewModel::closeDraft) {
            TaskEditor(
                draft = draft,
                goals = state.goals,
                onUpdate = viewModel::updateDraft,
                onSave = viewModel::saveDraft,
                onDelete = viewModel::deleteDraft,
                onOpenCalendar = onOpenCalendar
            )
        }
    }
}

@Composable
private fun TaskEditor(
    draft: TaskDraft,
    goals: List<Pair<String, String>>,
    onUpdate: ((TaskDraft) -> TaskDraft) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onOpenCalendar: () -> Unit
) {
    val pickDate = rememberDatePicker(draft.dueDate) { date ->
        onUpdate { it.copy(dueDate = date) }
    }
    val pickTime = rememberTimePicker(draft.dueTime) { time ->
        onUpdate { it.copy(dueTime = time) }
    }

    Text(
        text = if (draft.isNew) "New task" else "Edit task",
        style = MaterialTheme.typography.titleLarge
    )
    Spacer(Modifier.height(14.dp))

    ManzilTextField(
        value = draft.title,
        onValueChange = { value -> onUpdate { it.copy(title = value) } },
        label = "Task",
        placeholder = "What exactly will you do?"
    )
    Spacer(Modifier.height(10.dp))
    ManzilTextField(
        value = draft.notes,
        onValueChange = { value -> onUpdate { it.copy(notes = value) } },
        label = "Notes",
        singleLine = false,
        minLines = 2
    )

    Spacer(Modifier.height(16.dp))
    SectionHeader(title = "When")
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedPillButton(
            text = draft.dueDate?.let { Fmt.relativeDay(it) } ?: "No date",
            icon = Icons.Filled.CalendarMonth,
            onClick = pickDate
        )
        OutlinedPillButton(
            text = draft.dueTime?.let { Fmt.time(it) } ?: "No time",
            icon = Icons.Filled.Schedule,
            onClick = pickTime
        )
    }

    Spacer(Modifier.height(16.dp))
    SectionHeader(title = "Priority")
    Spacer(Modifier.height(8.dp))
    ChipRow(
        options = listOf("P1 urgent", "P2 high", "P3 normal", "P4 later"),
        selectedIndex = (draft.priority - 1).coerceIn(0, 3),
        onSelect = { index -> onUpdate { it.copy(priority = index + 1) } }
    )

    Spacer(Modifier.height(16.dp))
    SectionHeader(title = "Estimate")
    Spacer(Modifier.height(8.dp))
    ChipRow(
        options = listOf("15m", "30m", "1h", "2h", "3h"),
        selectedIndex = when (draft.estimatedMinutes) {
            15 -> 0
            30 -> 1
            60 -> 2
            120 -> 3
            180 -> 4
            else -> 1
        },
        onSelect = { index ->
            val minutes = listOf(15, 30, 60, 120, 180)[index]
            onUpdate { it.copy(estimatedMinutes = minutes) }
        }
    )

    Spacer(Modifier.height(16.dp))
    SectionHeader(title = "Repeat")
    Spacer(Modifier.height(8.dp))
    ChipRow(
        options = listOf("Once", "Daily", "Weekdays", "Weekly"),
        selectedIndex = when {
            draft.recurrenceRule == null -> 0
            draft.recurrenceRule!!.contains("BYDAY=MO,TU,WE,TH,FR") -> 2
            draft.recurrenceRule!!.startsWith("FREQ=DAILY") -> 1
            else -> 3
        },
        onSelect = { index ->
            val rule = when (index) {
                1 -> "FREQ=DAILY"
                2 -> "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR"
                3 -> "FREQ=WEEKLY"
                else -> null
            }
            onUpdate { it.copy(recurrenceRule = rule) }
        }
    )
    if (draft.recurrenceRule != null) {
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Repeat,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "Repeats create real occurrences for the next 60 days.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (goals.isNotEmpty()) {
        Spacer(Modifier.height(16.dp))
        SectionHeader(title = "Goal")
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            goals.take(6).forEach { (id, title) ->
                val selected = draft.goalId == id
                OutlinedPillButton(
                    text = title.take(22) + if (title.length > 22) "…" else "",
                    icon = if (selected) Icons.Filled.Flag else null,
                    onClick = { onUpdate { it.copy(goalId = if (selected) null else id) } }
                )
            }
        }
    }

    Spacer(Modifier.height(22.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = onSave, shape = RoundedCornerShape(14.dp)) {
            Text(if (draft.isNew) "Add task" else "Save changes")
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
    Spacer(Modifier.height(6.dp))
    TextButton(onClick = onOpenCalendar) {
        Text("Open calendar to see this in the week view", style = MaterialTheme.typography.labelSmall)
    }
}
