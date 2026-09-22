package com.manzil.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.manzil.app.MainActivity
import com.manzil.app.R

/**
 * The home-screen widget is deliberately dumb: the app writes a small text snapshot
 * whenever today's plan changes and the widget renders it. No database access and no
 * background work inside the widget process, so it can never crash the app.
 */
class TodayWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        val views = buildViews(context)
        appWidgetIds.forEach { id -> manager.updateAppWidget(id, views) }
    }

    fun buildViews(context: Context): RemoteViews {
        val snapshot = WidgetSnapshot.read(context)
        val views = RemoteViews(context.packageName, R.layout.widget_today)
        views.setTextViewText(
            R.id.widget_title,
            context.getString(R.string.app_name) + " · " + snapshot.dateLabel
        )
        val lines = snapshot.lines.take(3)
        views.setTextViewText(R.id.widget_task1, lines.getOrElse(0) { context.getString(R.string.widget_empty) })
        views.setTextViewText(R.id.widget_task2, lines.getOrElse(1) { "" })
        views.setTextViewText(R.id.widget_task3, lines.getOrElse(2) { "" })
        views.setTextViewText(R.id.widget_footer, snapshot.footer)
        val pending = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, pending)
        return views
    }

    companion object {
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(ComponentName(context, TodayWidgetProvider::class.java))
            if (ids.isEmpty()) return
            val views = TodayWidgetProvider().buildViews(context)
            ids.forEach { id -> manager.updateAppWidget(id, views) }
        }
    }
}
