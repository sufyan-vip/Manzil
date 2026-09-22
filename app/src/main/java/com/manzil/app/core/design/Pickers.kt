package com.manzil.app.core.design

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.LocalTime

/**
 * Platform date/time pickers. They are theme aware, accessible out of the box and
 * need no extra dependency — the important part is that they actually work offline.
 */
@Composable
fun rememberDatePicker(onPicked: (LocalDate?) -> Unit): () -> Unit {
    val context = LocalContext.current
    return remember(onPicked) {
        {
            val today = LocalDate.now()
            DatePickerDialog(
                context,
                { _, year, month, day -> onPicked(LocalDate.of(year, month + 1, day)) },
                today.year,
                today.monthValue - 1,
                today.dayOfMonth
            ).apply {
                setButton(DatePickerDialog.BUTTON_NEUTRAL, "Clear date") { _, _ -> onPicked(null) }
                setButton(DatePickerDialog.BUTTON_NEGATIVE, "Today") { _, _ -> onPicked(today) }
            }.show()
        }
    }
}

@Composable
fun rememberDatePicker(initial: LocalDate?, onPicked: (LocalDate?) -> Unit): () -> Unit {
    val context = LocalContext.current
    return remember(onPicked, initial) {
        {
            val seed = initial ?: LocalDate.now()
            DatePickerDialog(
                context,
                { _, year, month, day -> onPicked(LocalDate.of(year, month + 1, day)) },
                seed.year,
                seed.monthValue - 1,
                seed.dayOfMonth
            ).apply {
                setButton(DatePickerDialog.BUTTON_NEUTRAL, "Clear date") { _, _ -> onPicked(null) }
                setButton(DatePickerDialog.BUTTON_NEGATIVE, "Today") { _, _ -> onPicked(LocalDate.now()) }
            }.show()
        }
    }
}

@Composable
fun rememberTimePicker(initial: LocalTime?, onPicked: (LocalTime?) -> Unit): () -> Unit {
    val context = LocalContext.current
    return remember(onPicked, initial) {
        {
            val seed = initial ?: LocalTime.of(9, 0)
            TimePickerDialog(
                context,
                { _, hour, minute -> onPicked(LocalTime.of(hour, minute)) },
                seed.hour,
                seed.minute,
                false
            ).apply {
                setButton(TimePickerDialog.BUTTON_NEUTRAL, "Clear time") { _, _ -> onPicked(null) }
            }.show()
        }
    }
}
