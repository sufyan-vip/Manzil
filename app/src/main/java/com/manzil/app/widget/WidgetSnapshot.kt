package com.manzil.app.widget

import android.content.Context
import com.manzil.app.core.common.Fmt

/** Tiny persisted snapshot the widget renders. Written by the app, read by the widget. */
data class WidgetState(
    val dateLabel: String,
    val lines: List<String>,
    val footer: String
)

object WidgetSnapshot {

    private const val PREFS = "manzil_widget"
    private const val KEY_DATE = "date_label"
    private const val KEY_LINES = "lines"
    private const val KEY_FOOTER = "footer"

    fun write(context: Context, state: WidgetState) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_DATE, state.dateLabel)
            .putString(KEY_LINES, state.lines.joinToString("\n"))
            .putString(KEY_FOOTER, state.footer)
            .apply()
        TodayWidgetProvider.refresh(context)
    }

    fun read(context: Context): WidgetState {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val lines = prefs.getString(KEY_LINES, null)
            ?.split("\n")
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        return WidgetState(
            dateLabel = prefs.getString(KEY_DATE, null) ?: Fmt.dateShort(Fmt.today()),
            lines = lines,
            footer = prefs.getString(KEY_FOOTER, null) ?: "Open Manzil"
        )
    }

    fun update(
        context: Context,
        tasks: List<Pair<String, Boolean>>,
        streakDays: Int,
        focusLabel: String?
    ) {
        val done = tasks.count { it.second }
        val pending = tasks.filter { !it.second }.map { "• " + it.first }
        val lines = if (pending.isEmpty() && tasks.isNotEmpty()) {
            listOf("All ${tasks.size} tasks done today 🎉")
        } else {
            pending
        }
        val footer = buildString {
            append("$done/${tasks.size} done")
            if (streakDays > 0) append("  ·  ${streakDays} day streak")
            focusLabel?.let { if (it.isNotBlank()) append("  ·  $it") }
        }
        write(
            context,
            WidgetState(
                dateLabel = Fmt.dateShort(Fmt.today()),
                lines = lines,
                footer = footer
            )
        )
    }
}
