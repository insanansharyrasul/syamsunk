package com.example.syamsunk.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.syamsunk.data.PrayerRepository
import com.example.syamsunk.data.PreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Glance widget displaying the 5 daily prayer times in a horizontal layout.
 * Matches the compact dark card design from Screenshot 2.
 */
class PrayerTimesWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefsRepo = PreferencesRepository(context)
        val location = prefsRepo.locationFlow.first()

        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        val times = if (location != null) {
            val daily = PrayerRepository.calculate(location.latitude, location.longitude, today)
            daily.toList().map { (name, instant) ->
                val lt = instant.toLocalDateTime(tz)
                name to String.format(java.util.Locale.getDefault(), "%02d:%02d", lt.hour, lt.minute)
            }
        } else {
            listOf("Fajr" to "--:--", "Dhuhr" to "--:--", "Asr" to "--:--", "Maghrib" to "--:--", "Isha" to "--:--")
        }

        provideContent {
            GlanceTheme {
                WidgetContent(times)
            }
        }
    }
}

@Composable
private fun WidgetContent(times: List<Pair<String, String>>) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(androidx.glance.ImageProvider(com.example.syamsunk.R.drawable.widget_background))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        times.forEachIndexed { index, (name, time) ->
            Column(
                modifier = GlanceModifier.defaultWeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = name,
                    style = TextStyle(
                        color = ColorProvider(android.graphics.Color.parseColor("#9E9E9E")),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = time,
                    style = TextStyle(
                        color = ColorProvider(android.graphics.Color.parseColor("#757575")),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }
    }
}
