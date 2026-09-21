package com.insan.syamsunk.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.insan.syamsunk.R

/**
 * Fires when an exact alarm triggers for a prayer time.
 * Creates a high-priority notification with the prayer name and time.
 */
class AdhanBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val CHANNEL_ID = "adhan_channel"
        const val NOTIFICATION_ID_BASE = 8000
    }

    override fun onReceive(context: Context, intent: Intent) {
        val prayerName = intent.getStringExtra("prayer_name") ?: "Prayer"
        val prayerTime = intent.getStringExtra("prayer_time") ?: ""

        createNotificationChannel(context)

        // Check POST_NOTIFICATIONS permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_name)
            .setContentTitle("$prayerName - $prayerTime")
            .setContentText("It's time for $prayerName prayer")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()

        val notificationId = NOTIFICATION_ID_BASE + prayerName.hashCode() % 100
        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Adhan Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Prayer time notifications"
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
