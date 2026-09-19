package com.example.syamsunk.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * AppWidgetReceiver for the prayer times Glance widget.
 * Handles widget lifecycle events (add, update, delete).
 */
class PrayerTimesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PrayerTimesWidget()
}
