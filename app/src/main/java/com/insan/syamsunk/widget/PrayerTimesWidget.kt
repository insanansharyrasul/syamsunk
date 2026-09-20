package com.insan.syamsunk.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.insan.syamsunk.MainActivity
import com.insan.syamsunk.R
import com.insan.syamsunk.data.PrayerRepository
import com.insan.syamsunk.data.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Locale

/**
 * AppWidgetProvider displaying the 5 daily prayer times in a horizontal layout.
 */
class PrayerTimesWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                performUpdate(context, appWidgetManager, appWidgetIds)
            } finally {
                pendingResult.finish()
            }
        }
    }

    fun updateAll(context: Context) {
        Companion.updateAll(context)
    }

    companion object {
        fun updateAll(context: Context) {
            CoroutineScope(Dispatchers.IO).launch {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val ids = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, PrayerTimesWidget::class.java)
                )
                if (ids.isNotEmpty()) {
                    performUpdate(context, appWidgetManager, ids)
                }
            }
        }

        fun updateWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                performUpdate(context, appWidgetManager, appWidgetIds)
            }
        }

        private suspend fun performUpdate(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray
        ) {
            val prefsRepo = PreferencesRepository(context)
            val location = prefsRepo.locationFlow.first()

            val tz = TimeZone.currentSystemDefault()
            val now = Clock.System.now()
            val today = now.toLocalDateTime(tz).date

            val settings = prefsRepo.userSettingsFlow.first()
            val daily = if (location != null) {
                PrayerRepository.calculate(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    date = today,
                    method = settings.calculationMethod,
                    madhab = settings.madhab
                )
            } else {
                null
            }

            fun formatTime(instant: Instant?): String {
                if (instant == null) return "--:--"
                val lt = instant.toLocalDateTime(tz)
                return String.format(Locale.getDefault(), "%02d:%02d", lt.hour, lt.minute)
            }

            val fajrStr = formatTime(daily?.fajr)
            val dhuhrStr = formatTime(daily?.dhuhr)
            val asrStr = formatTime(daily?.asr)
            val maghribStr = formatTime(daily?.maghrib)
            val ishaStr = formatTime(daily?.isha)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            for (appWidgetId in appWidgetIds) {
                val views = RemoteViews(context.packageName, R.layout.prayer_widget)

                views.setTextViewText(R.id.fajr_time, fajrStr)
                views.setTextViewText(R.id.dhuhr_time, dhuhrStr)
                views.setTextViewText(R.id.asr_time, asrStr)
                views.setTextViewText(R.id.maghrib_time, maghribStr)
                views.setTextViewText(R.id.isha_time, ishaStr)

                // Launch main app when tapped
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                views.setOnClickPendingIntent(R.id.fajr_label, pendingIntent)
                views.setOnClickPendingIntent(R.id.fajr_time, pendingIntent)
                views.setOnClickPendingIntent(R.id.dhuhr_label, pendingIntent)
                views.setOnClickPendingIntent(R.id.dhuhr_time, pendingIntent)
                views.setOnClickPendingIntent(R.id.asr_label, pendingIntent)
                views.setOnClickPendingIntent(R.id.asr_time, pendingIntent)
                views.setOnClickPendingIntent(R.id.maghrib_label, pendingIntent)
                views.setOnClickPendingIntent(R.id.maghrib_time, pendingIntent)
                views.setOnClickPendingIntent(R.id.isha_label, pendingIntent)
                views.setOnClickPendingIntent(R.id.isha_time, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
}
