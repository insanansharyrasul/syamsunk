package com.example.syamsunk.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

/**
 * Receives BOOT_COMPLETED broadcast to re-schedule prayer alarms after device reboot.
 * Delegates to [DailyRescheduleWorker] to perform the actual rescheduling.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val workRequest = OneTimeWorkRequestBuilder<DailyRescheduleWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }
}
