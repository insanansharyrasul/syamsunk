package com.insan.syamsunk.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.insan.syamsunk.MainActivity
import com.insan.syamsunk.R
import com.insan.syamsunk.data.PrayerRepository
import com.insan.syamsunk.data.PreferencesRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Locale

data class WidgetPrayer(val name: String, val time: String, val instant: Instant)

// ponytail: background is hardcoded dark (widget_background.xml), so text is hardcoded white to match
private val WidgetTextColor = ColorProvider(android.R.color.white)

/**
 * Responsive home widget: narrow widths show only the next prayer,
 * medium widths the next three, wide widths all five.
 */
class PrayerTimesWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(Compact, Medium, Expanded)
    )

    override suspend fun provideGlance(context: Context, glanceId: GlanceId) {
        val prayers = loadPrayers(context)
        provideContent {
            val width = LocalSize.current.width
            val visible = visiblePrayers(prayers, Clock.System.now(), width)
            Box(
                modifier = GlanceModifier.fillMaxSize()
                    .background(ImageProvider(R.drawable.widget_background))
                    .clickable(actionStartActivity<MainActivity>())
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (visible.size == 1) NextPrayerHero(visible[0])
                else PrayerRow(visible)
            }
        }
    }

    private suspend fun loadPrayers(context: Context): List<WidgetPrayer> {
        val prefsRepo = PreferencesRepository(context)
        val location = prefsRepo.locationFlow.first()
        val tz = TimeZone.currentSystemDefault()
        val settings = prefsRepo.userSettingsFlow.first()

        fun format(instant: Instant?): String {
            if (instant == null) return "--:--"
            val lt = instant.toLocalDateTime(tz)
            return String.format(Locale.getDefault(), "%02d:%02d", lt.hour, lt.minute)
        }

        // ponytail: far-future sentinel keeps "--:--" placeholders sortable without nulls
        val farFuture = Instant.fromEpochMilliseconds(Long.MAX_VALUE / 2)
        if (location == null) {
            return listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha").map {
                WidgetPrayer(it, "--:--", farFuture)
            }
        }
        val today = Clock.System.now().toLocalDateTime(tz).date
        val daily = PrayerRepository.calculate(
            latitude = location.latitude,
            longitude = location.longitude,
            date = today,
            method = settings.calculationMethod,
            madhab = settings.madhab
        )
        return daily.toList().map { (name, instant) -> WidgetPrayer(name, format(instant), instant) }
    }

    companion object {
        // ponytail: heights stay <= minHeight (56dp) so the launcher always fits
        // height-wise and picks the bucket by width; tall 180dp buckets could never
        // match a 1-row widget and forced the narrow fallback on big phones.
        private val Compact = DpSize(120.dp, 60.dp)
        private val Medium = DpSize(240.dp, 60.dp)
        private val Expanded = DpSize(360.dp, 60.dp)

        /** Upcoming-first ordering, wrapped past midnight; count follows width. */
        fun visiblePrayers(all: List<WidgetPrayer>, now: Instant, width: Dp): List<WidgetPrayer> {
            val next = all.indexOfFirst { it.instant > now }.takeIf { it >= 0 } ?: 0
            val ordered = all.drop(next) + all.take(next)
            val count = when {
                width < 200.dp -> 1
                width < 300.dp -> 3
                else -> 5
            }
            return ordered.take(count)
        }
    }
}

class PrayerTimesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PrayerTimesWidget()
}

@Composable
private fun PrayerRow(prayers: List<WidgetPrayer>) {
    Row(
        modifier = GlanceModifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        prayers.forEach { PrayerCell(it, isNext = it == prayers[0]) }
    }
}

@Composable
private fun RowScope.PrayerCell(prayer: WidgetPrayer, isNext: Boolean) {
    Column(
        modifier = GlanceModifier.defaultWeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = prayer.name,
            maxLines = 1,
            style = TextStyle(
                color = WidgetTextColor,
                fontSize = 16.sp,
                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        )
        Text(
            text = prayer.time,
            maxLines = 1,
            style = TextStyle(color = WidgetTextColor, fontSize = 15.sp, textAlign = TextAlign.Center)
        )
    }
}

@Composable
private fun NextPrayerHero(prayer: WidgetPrayer) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = prayer.name,
            maxLines = 1,
            style = TextStyle(color = WidgetTextColor, fontSize = 16.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
        )
        Text(
            text = prayer.time,
            maxLines = 1,
            style = TextStyle(color = WidgetTextColor, fontSize = 34.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        )
    }
}
