package com.example.syamsunk.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.syamsunk.data.PrayerRepository
import com.example.syamsunk.data.PreferencesRepository
import com.example.syamsunk.widget.PrayerTimesWidget
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Periodic WorkManager job that:
 * 1. Reads cached location from DataStore
 * 2. Recalculates today's prayer times
 * 3. Reschedules exact alarms via [AdhanAlarmManager]
 * 4. Triggers Glance widget refresh
 *
 * Runs once daily and also on-demand after BOOT_COMPLETED.
 */
class DailyRescheduleWorker(
    private val appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val WORK_NAME = "daily_reschedule"

        fun enqueueDaily(context: Context) {
            val request = PeriodicWorkRequestBuilder<DailyRescheduleWorker>(
                24, TimeUnit.HOURS
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }

    override suspend fun doWork(): Result {
        val prefsRepo = PreferencesRepository(appContext)
        val location = prefsRepo.locationFlow.first() ?: return Result.retry()

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val settings = prefsRepo.userSettingsFlow.first()
        val prayerTimes = PrayerRepository.calculate(
            latitude = location.latitude,
            longitude = location.longitude,
            date = today,
            method = settings.calculationMethod,
            madhab = settings.madhab
        )

        // Reschedule exact alarms
        AdhanAlarmManager.cancelAlarms(appContext)
        AdhanAlarmManager.scheduleAlarms(appContext, prayerTimes)

        // Refresh widget
        try {
            PrayerTimesWidget.updateAll(appContext)
        } catch (_: Exception) {
            // Widget may not be placed
        }

        return Result.success()
    }
}
