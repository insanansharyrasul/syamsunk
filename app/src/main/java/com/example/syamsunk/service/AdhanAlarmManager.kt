package com.example.syamsunk.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.syamsunk.data.model.DailyPrayerTimes
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Schedules exact alarms for each prayer time using [AlarmManager.setExactAndAllowWhileIdle].
 * This ensures alarms fire even during Doze mode for critical Adhan notifications.
 */
object AdhanAlarmManager {

    private const val REQUEST_CODE_BASE = 7000

    fun scheduleAlarms(context: Context, prayerTimes: DailyPrayerTimes) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val now = System.currentTimeMillis()

        prayerTimes.toList().forEachIndexed { index, (name, instant) ->
            val triggerAtMillis = instant.toEpochMilliseconds()
            if (triggerAtMillis > now) {
                val intent = Intent(context, AdhanBroadcastReceiver::class.java).apply {
                    action = "com.example.syamsunk.ADHAN_ALARM"
                    putExtra("prayer_name", name)
                    val tz = TimeZone.currentSystemDefault()
                    val localTime = instant.toLocalDateTime(tz)
                    putExtra("prayer_time", "%02d:%02d".format(localTime.hour, localTime.minute))
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    REQUEST_CODE_BASE + index,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
                        )
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
                    )
                }
            }
        }
    }

    fun cancelAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in 0..4) {
            val intent = Intent(context, AdhanBroadcastReceiver::class.java).apply {
                action = "com.example.syamsunk.ADHAN_ALARM"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE_BASE + i,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
