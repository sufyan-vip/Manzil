@file:OptIn(ExperimentalMaterial3Api::class)

package com.manzil.app.feature.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manzil.app.core.common.Fmt
import com.manzil.app.core.design.Divider
import com.manzil.app.core.design.EmptyState
import com.manzil.app.core.design.GoalChip
import com.manzil.app.core.design.Heatmap
import com.manzil.app.core.design.InfoBanner
import com.manzil.app.core.design.ManzilCard
import com.manzil.app.core.design.ManzilScreenScaffold
import com.manzil.app.core.design.ManzilTextField
import com.manzil.app.core.design.Pill
import com.manzil.app.core.design.PriorityDot
import com.manzil.app.core.design.ProgressRing
import com.manzil.app.core.design.SectionHeader
import com.manzil.app.core.theme.ManzilColors
import com.manzil.app.domain.model.DayTask
import kotlinx.coroutines.delay
import java.time.LocalDate

@Composable
fun TodayScreen(
    onSearch: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenGoals: () -> Unit,
    onOpenJournal: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    var capture by remember { mutableStateOf("") }

    LaunchedEffect(state.timer.isRunning) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    ManzilScreenScaffold(
        title = state.greeting,
        subtitle = state.dateLabel,
        onSearch = onSearch
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { HeaderCard(state, now) }

            state.banner?.let { banner ->
                item {
                    InfoBanner(
                        text = banner,
                        icon = if (state.bannerIsError) Icons.Filled.Bolt else Icons.Filled.Lightbulb,
                        color = if (state.bannerIsError) ManzilColors.danger else ManzilColors.info,
                        onClose = viewModel::dismissBanner
                    )
                }
            }

            item {
                BriefingCard(
                    text = state.briefingText,
                    planning = state.planning,
                    hasApiKey = state.hasApiKey,
                    onRegenerate = viewModel::regeneratePlan
                )
            }

            item {
                FocusCard(
                    state = state,
                    now = now,
                    onStart = { viewModel.startTimer(state.tasks.firstOrNull { !it.done }) },
                    onPause = viewModel::pauseTimer,
                    onResume = viewModel::resumeTimer,
                    onStop = viewModel::stopTimer
                )
            }

            item { ProgressStrip(state, now) }

            if (state.events.isNotEmpty()) {
                item { TodayEventsCard(state, onOpenCalendar) }
            }

            item {
                SectionHeader(
                    title = "Today's plan",
                    trailing = "Manage",
                    onTrailingClick = onOpenTasks
                )
            }

            if (state.tasks.isEmpty() && !state.loading) {
                item {
                    ManzilCard {
                        EmptyState(
                            icon = Icons.Filled.CheckCircle,
                            title = "Nothing planned for today",
                            message = "Add one real task — three small ones beat a perfect list of ten.",
                            actionLabel = "Add a task",
                            onAction = onOpenTasks
                        )
                    }
                }
            } else {
                items(state.tasks, key = { it.id }) { dayTask ->
                    TaskRow(
                        dayTask = dayTask,
                        goalTitle = null,
                        onToggle = { viewModel.toggleDone(dayTask) },
                        onPostpone = { viewModel.postpone(dayTask.task, LocalDate.now().plusDays(1)) },
                        onMoveUp = { viewModel.moveTaskOrder(dayTask, -1) },
                        onMoveDown = { viewModel.moveTaskOrder(dayTask, 1) }
                    )
                }
            }

            if (state.leftovers.isNotEmpty()) {
                item {
                    SectionHeader(title = "Yesterday's leftovers")
                }
                items(state.leftovers, key = { "left_" + it.id }) { dayTask ->
                    LeftoverRow(
                        dayTask = dayTask,
                        onMove = { viewModel.moveLeftoverToToday(dayTask) },
                        onDrop = { viewModel.dropLeftover(dayTask) }
                    )
                }
            }

            if (state.habits.isNotEmpty()) {
                item { HabitsCard(state, onToggle = viewModel::toggleHabit, onOpenJournal = onOpenJournal) }
            }

            item {
                QuickCaptureCard(
                    value = capture,
                    onValueChange = { capture = it },
                    onSubmit = {
                        viewModel.capture(capture)
                        capture = ""
                    }
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

/* --------------------------------------------------------------------------------------------- */

@Composable
private fun HeaderCard(state: TodayState, now: Long) {
    ManzilCard(containerColor = MaterialTheme.colorScheme.primaryContainer) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "${state.greeting}, ${state.userName}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = state.dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (state.planLine.isNotBlank()) {
                    Text(
                        text = state.planLine,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Fmt.clockNow(Fmt.localTimeOf(now)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Pill(
                    text = "Day ${state.dayCounter}",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun BriefingCard(
    text: String,
    planning: Boolean,
    hasApiKey: Boolean,
    onRegenerate: () -> Unit
) {
    ManzilCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Daily briefing",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            if (planning) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                TextButton(onClick = onRegenerate) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (hasApiKey) "AI plan" else "Adaptive plan", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Divider()
        Spacer(Modifier.height(10.dp))
        Text(
            text = text.ifBlank { "Your briefing appears here once tasks exist." },
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FocusCard(
    state: TodayState,
    now: Long,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    val timer = state.timer
    val elapsed = timer.elapsed(now)
    ManzilCard(containerColor = MaterialTheme.colorScheme.secondaryContainer) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Focus of the day",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.weight(1f)
            )
            if (timer.isRunning) {
                Pill(text = "running", color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = timer.label.ifBlank { state.tasks.firstOrNull { !it.done }?.task?.title ?: "Deep work" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = Fmt.stopwatch(elapsed),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            when {
                timer.isRunning -> {
                    Button(onClick = onPause, shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Pause")
                    }
                }
                elapsed > 0L -> {
                    Button(onClick = onResume, shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Resume")
                    }
                }
                else -> {
                    Button(onClick = onStart, shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Start focus")
                    }
                }
            }
            if (elapsed > 0L) {
                OutlinedButton(onClick = onStop, shape = RoundedCornerShape(14.dp)) {
                    Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Stop & save")
                }
            }
        }
    }
}

@Composable
private fun ProgressStrip(state: TodayState, now: Long) {
    ManzilCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProgressRing(
                progress = state.rings.today,
                centerLabel = "${(state.rings.today * 100).toInt()}%",
                centerSubLabel = "Today"
            )
            ProgressRing(
                progress = state.rings.week,
                centerLabel = "${(state.rings.week * 100).toInt()}%",
                centerSubLabel = "Week",
                color = ManzilColors.info
            )
            ProgressRing(
                progress = state.rings.goal,
                centerLabel = "${(state.rings.goal * 100).toInt()}%",
                centerSubLabel = "Goal",
                color = ManzilColors.success
            )
        }
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = ManzilColors.warning,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${state.streak.current} day streak",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "Longest ${state.streak.longest} days · 12 week heatmap",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Heatmap(days = state.heat)
        if (state.rings.today == 0f && Fmt.localTimeOf(now).hour >= 20) {
            Spacer(Modifier.height(10.dp))
            InfoBanner(
                text = "Streak at risk — nothing completed today yet. One small task is enough.",
                color = ManzilColors.warning
            )
        }
    }
}

@Composable
private fun TodayEventsCard(state: TodayState, onOpenCalendar: () -> Unit) {
    ManzilCard(onClick = onOpenCalendar) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Today's calendar", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
            Text(
                "${state.events.size} items",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(8.dp))
        state.events.take(3).forEach { event ->
            Row(modifier = Modifier.padding(vertical = 3.dp)) {
                Text(
                    Fmt.time(event.startAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.width(56.dp)
                )
                Text(
                    event.title,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun TaskRow(
    dayTask: DayTask,
    goalTitle: String?,
    compact: Boolean = false,
    onToggle: () -> Unit,
    onPostpone: (() -> Unit)? = null,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    val task = dayTask.task
    ManzilCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = dayTask.done,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = ManzilColors.success)
            )
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PriorityDot(task.priority)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (dayTask.done) TextDecoration.LineThrough else null,
                        color = if (dayTask.done) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    task.dueTime?.let {
                        Icon(
                            Icons.Filled.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(
                            Fmt.time(it),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    task.estimatedMinutes?.let {
                        Text(
                            Fmt.duration(it),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    if (task.recurrenceRule != null) {
                        Pill(text = "repeats", color = ManzilColors.info)
                        Spacer(Modifier.width(6.dp))
                    }
                    if (task.autoRolledCount > 0) {
                        Pill(text = "moved ${task.autoRolledCount}x", color = ManzilColors.warning)
                        Spacer(Modifier.width(6.dp))
                    }
                    if (!compact && goalTitle != null) GoalChip(goalTitle)
                }
            }
            if (!compact && !dayTask.done) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (onMoveUp != null) {
                        IconButton(onClick = onMoveUp, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Filled.ArrowUpward,
                                contentDescription = "Move up",
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    if (onMoveDown != null) {
                        IconButton(onClick = onMoveDown, modifier = Modifier.size(28.dp)) {
                            Icon(
                                Icons.Filled.ArrowDownward,
                                contentDescription = "Move down",
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
                if (onPostpone != null) {
                    IconButton(onClick = onPostpone, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Filled.Schedule,
                            contentDescription = "Postpone to tomorrow",
                            modifier = Modifier.size(17.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (compact) {
                Icon(
                    Icons.Filled.DragHandle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun LeftoverRow(dayTask: DayTask, onMove: () -> Unit, onDrop: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = ManzilColors.danger,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    dayTask.task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            dayTask.task.lastAiReason?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onMove, shape = RoundedCornerShape(12.dp)) { Text("Move to today") }
                OutlinedButton(onClick = onDrop, shape = RoundedCornerShape(12.dp)) { Text("Drop") }
            }
        }
    }
}

@Composable
private fun HabitsCard(state: TodayState, onToggle: (String, Boolean) -> Unit, onOpenJournal: () -> Unit) {
    ManzilCard {
        SectionHeader(title = "Keystone habits", trailing = "Journal", onTrailingClick = onOpenJournal)
        Spacer(Modifier.height(10.dp))
        state.habits.forEach { habit ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggle(habit.id, !habit.doneToday) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            Color(habit.colorArgb).copy(alpha = if (habit.doneToday) 1f else 0.16f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = habit.icon,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (habit.doneToday) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(habit.name, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${habit.weekDots.count { it }} of last 7 days · target ${habit.targetPerWeek}/week",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    habit.weekDots.forEach { done ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (done) ManzilColors.success else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (habit.doneToday) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                    contentDescription = if (habit.doneToday) "Done" else "Not done",
                    tint = if (habit.doneToday) ManzilColors.success else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickCaptureCard(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val preview = remember(value) {
        if (value.isBlank()) null else com.manzil.app.core.common.QuickCaptureParser.parse(value)
    }
    ManzilCard {
        SectionHeader(title = "Quick capture")
        Spacer(Modifier.height(8.dp))
        ManzilTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = "proposal 5pm friday !1 #client",
            singleLine = true
        )
        if (preview != null && preview.title.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "→ ${preview.title}  ·  ${com.manzil.app.core.common.QuickCaptureParser.describe(preview)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onSubmit, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add task")
            }
            if (value.isNotBlank()) {
                TextButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Clear")
                }
            }
        }
    }
}
