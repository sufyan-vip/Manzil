@file:OptIn(ExperimentalMaterial3Api::class)

package com.manzil.app.feature.calendar

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.manzil.app.core.common.Fmt
import com.manzil.app.core.design.Divider
import com.manzil.app.core.design.EmptyState
import com.manzil.app.core.design.InfoBanner
import com.manzil.app.core.design.ManzilCard
import com.manzil.app.core.design.ManzilScreenScaffold
import com.manzil.app.core.design.ManzilSheet
import com.manzil.app.core.design.ManzilTextField
import com.manzil.app.core.design.Pill
import com.manzil.app.core.design.SectionHeader
import com.manzil.app.core.design.SegmentedControl
import com.manzil.app.core.design.rememberDatePicker
import com.manzil.app.core.design.rememberTimePicker
import com.manzil.app.core.theme.ManzilColors
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

@Composable
fun CalendarScreen(
    onSearch: () -> Unit,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var nowHour by remember { mutableStateOf(LocalTime.now().hour) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            nowHour = LocalTime.now().hour
            delay(30_000L)
        }
    }

    ManzilScreenScaffold(
        title = "Calendar",
        subtitle = Fmt.dateLong(state.selectedDate),
        onSearch = onSearch,
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.openNewEvent() }) {
                Icon(Icons.Filled.Add, contentDescription = "New event")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                SegmentedControl(
                    options = listOf("Month", "Week", "Agenda"),
                    selectedIndex = state.mode.ordinal,
                    onSelect = { index -> viewModel.setMode(CalendarMode.entries[index]) }
                )
                state.banner?.let {
                    Spacer(Modifier.height(10.dp))
                    InfoBanner(text = it, color = ManzilColors.success, onClose = viewModel::dismissBanner)
                }
                if (state.conflicts > 0 && state.mode != CalendarMode.AGENDA) {
                    Spacer(Modifier.height(10.dp))
                    InfoBanner(
                        text = "${state.conflicts} events overlap in this range — check the striped blocks.",
                        color = ManzilColors.warning
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (state.mode == CalendarMode.MONTH) viewModel.shiftMonth(-1) else viewModel.shiftDays(-7)
                    }) { Icon(Icons.Filled.ArrowBack, contentDescription = "Previous") }
                    Text(
                        text = if (state.mode == CalendarMode.MONTH) {
                            state.month.month.name.lowercase().replaceFirstChar { it.uppercase() } + " ${state.month.year}"
                        } else {
                            "Week of ${Fmt.dateShort(Fmt.startOfWeek(state.selectedDate))}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = {
                        if (state.mode == CalendarMode.MONTH) viewModel.shiftMonth(1) else viewModel.shiftDays(7)
                    }) { Icon(Icons.Filled.ArrowForward, contentDescription = "Next") }
                }
                Spacer(Modifier.height(4.dp))
            }

            when (state.mode) {
                CalendarMode.MONTH -> MonthGrid(
                    month = state.month,
                    selected = state.selectedDate,
                    loadFor = { date ->
                        viewModel.eventsOn(date).size + viewModel.tasksOn(date).size
                    },
                    onSelect = viewModel::selectDate,
                    onLongPress = { date -> viewModel.openNewEvent(date) }
                )
                CalendarMode.WEEK -> WeekGrid(
                    anchor = state.selectedDate,
                    nowHour = nowHour,
                    eventsFor = { date -> viewModel.eventsOn(date) },
                    onSelectHour = { date, hour -> viewModel.openNewEvent(date, hour) },
                    onOpenEvent = viewModel::openEvent
                )
                CalendarMode.AGENDA -> Unit
            }

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    SectionHeader(
                        title = if (state.mode == CalendarMode.AGENDA) "Agenda" else Fmt.dayLabel(state.selectedDate),
                        trailing = "Add event",
                        onTrailingClick = { viewModel.openNewEvent(state.selectedDate) }
                    )
                }

                val days = if (state.mode == CalendarMode.AGENDA) {
                    (0..13).map { state.selectedDate.plusDays(it.toLong()) }
                } else {
                    listOf(state.selectedDate)
                }

                days.forEach { date ->
                    val events = viewModel.eventsOn(date)
                    val tasks = viewModel.tasksOn(date)
                    if (state.mode == CalendarMode.AGENDA && events.isEmpty() && tasks.isEmpty()) return@forEach

                    item(key = "header_$date") {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = Fmt.relativeDay(date),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "${events.size + tasks.size} items",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (events.isEmpty() && tasks.isEmpty() && state.mode != CalendarMode.AGENDA) {
                        item(key = "empty_$date") {
                            ManzilCard {
                                EmptyState(
                                    icon = Icons.Filled.Event,
                                    title = "Nothing planned",
                                    message = "Tap an empty slot in the grid, or plan a task block.",
                                    actionLabel = "New event",
                                    onAction = { viewModel.openNewEvent(date) }
                                )
                            }
                        }
                    }

                    items(events.size, key = { index -> "event_${date}_$index" }) { index ->
                        val event = events[index]
                        val striped = index > 0 && events[index - 1].endAt > event.startAt
                        ManzilCard(
                            contentPadding = PaddingValues(14.dp),
                            onClick = { viewModel.openEvent(event) }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (striped) ManzilColors.warning else ManzilColors.info)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        event.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        "${Fmt.time(event.startAt)} – ${Fmt.time(event.endAt)}" +
                                            if (striped) "  ·  overlaps another event" else "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Pill(text = event.source.name.lowercase(), color = ManzilColors.info)
                            }
                        }
                    }

                    items(tasks.size, key = { index -> "task_${date}_$index" }) { index ->
                        val task = tasks[index]
                        ManzilCard(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentPadding = PaddingValues(14.dp),
                            onClick = { viewModel.planTaskBlock(task) }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        task.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        buildString {
                                            append(task.dueTime?.let { Fmt.time(it) } ?: "no time")
                                            task.estimatedMinutes?.let { append("  ·  ").append(Fmt.duration(it)) }
                                            append("  ·  tap to block it")
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Pill(text = "task", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }

    state.draft?.let { draft ->
        ManzilSheet(onDismiss = viewModel::closeDraft) {
            EventEditor(
                draft = draft,
                onUpdate = viewModel::updateDraft,
                onSave = viewModel::saveDraft,
                onDelete = viewModel::deleteDraft
            )
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    selected: LocalDate,
    loadFor: (LocalDate) -> Int,
    onSelect: (LocalDate) -> Unit,
    onLongPress: (LocalDate) -> Unit
) {
    val firstDay = month.atDay(1)
    val leading = (firstDay.dayOfWeek.value + 6) % 7
    val days = month.lengthOfMonth()
    val today = LocalDate.now()

    Column(Modifier.padding(horizontal = 12.dp)) {
        Row(Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        val cells = leading + days
        val rows = (cells + 6) / 7
        for (row in 0 until rows) {
            Row(Modifier.fillMaxWidth()) {
                for (column in 0 until 7) {
                    val cellIndex = row * 7 + column
                    val dayNumber = cellIndex - leading + 1
                    if (dayNumber in 1..days) {
                        val date = month.atDay(dayNumber)
                        val load = loadFor(date)
                        val isSelected = date == selected
                        val isToday = date == today
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primaryContainer
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onSelect(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayNumber.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                if (load > 0) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 3.dp)
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> MaterialTheme.colorScheme.onPrimary
                                                    load <= 3 -> ManzilColors.success
                                                    load <= 6 -> ManzilColors.warning
                                                    else -> ManzilColors.danger
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    } else {
                        Spacer(Modifier.weight(1f).height(46.dp))
                    }
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onLongPress(selected) }) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Plan this day", style = MaterialTheme.typography.labelSmall)
            }
        }
        Divider()
    }
}

@Composable
private fun WeekGrid(
    anchor: LocalDate,
    nowHour: Int,
    eventsFor: (LocalDate) -> List<com.manzil.app.data.local.entity.CalendarEvent>,
    onSelectHour: (LocalDate, Int) -> Unit,
    onOpenEvent: (com.manzil.app.data.local.entity.CalendarEvent) -> Unit
) {
    val weekStart = Fmt.startOfWeek(anchor)
    val days = (0..6).map { weekStart.plusDays(it.toLong()) }
    Column(Modifier.padding(horizontal = 10.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.width(42.dp))
            days.forEach { date ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        Fmt.weekdayShort(date).take(3),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (date == LocalDate.now()) FontWeight.Bold else FontWeight.Normal,
                        color = if (date == LocalDate.now()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(392.dp)
        ) {
            for (hour in 7..22) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            if (hour == nowHour) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            else Color.Transparent
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "$hour:00",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(42.dp)
                    )
                    days.forEach { date ->
                        val blocks = eventsFor(date).filter { Fmt.localTimeOf(it.startAt).hour == hour }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(24.dp)
                                .padding(1.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    if (blocks.isEmpty()) {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    } else {
                                        ManzilColors.info.copy(alpha = 0.85f)
                                    }
                                )
                                .clickable {
                                    blocks.firstOrNull()?.let(onOpenEvent) ?: onSelectHour(date, hour)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            blocks.firstOrNull()?.let { event ->
                                Text(
                                    text = event.title.take(6),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Tap an empty slot to add an event · red-washed row is the current hour",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Divider()
    }
}

@Composable
private fun EventEditor(
    draft: EventDraft,
    onUpdate: ((EventDraft) -> EventDraft) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit
) {
    val pickDate = rememberDatePicker(draft.date) { date ->
        if (date != null) onUpdate { it.copy(date = date) }
    }
    val pickStart = rememberTimePicker(draft.startTime) { time ->
        if (time != null) onUpdate { it.copy(startTime = time) }
    }
    val pickEnd = rememberTimePicker(draft.endTime) { time ->
        if (time != null) onUpdate { it.copy(endTime = time) }
    }

    Text(if (draft.isNew) "New event" else "Edit event", style = MaterialTheme.typography.titleLarge)
    Spacer(Modifier.height(14.dp))
    ManzilTextField(
        value = draft.title,
        onValueChange = { value -> onUpdate { it.copy(title = value) } },
        label = "Title",
        placeholder = "Client call, class, deep work…"
    )
    Spacer(Modifier.height(10.dp))
    ManzilTextField(
        value = draft.description,
        onValueChange = { value -> onUpdate { it.copy(description = value) } },
        label = "Notes",
        singleLine = false,
        minLines = 2
    )
    Spacer(Modifier.height(14.dp))
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pickDate() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(Fmt.dateLong(draft.date), style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.clickable { pickStart() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Schedule, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("From ${Fmt.time(draft.startTime)}", style = MaterialTheme.typography.bodyMedium)
                }
                Row(
                    modifier = Modifier.clickable { pickEnd() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("To ${Fmt.time(draft.endTime)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
    Spacer(Modifier.height(20.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(onClick = onSave, shape = RoundedCornerShape(14.dp)) {
            Text(if (draft.isNew) "Add event" else "Save")
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
}
