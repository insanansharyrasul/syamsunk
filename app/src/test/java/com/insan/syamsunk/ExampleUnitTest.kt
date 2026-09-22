package com.insan.syamsunk

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.insan.syamsunk.data.PrayerRepository
import com.insan.syamsunk.widget.PrayerTimesWidget
import com.insan.syamsunk.widget.WidgetPrayer
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun hanafiAsrIsLaterThanShafiAsr() {
        val lat = -6.2088
        val lng = 106.8456
        val date = LocalDate(2026, 9, 20)

        val shafiTimes = PrayerRepository.calculate(
            latitude = lat,
            longitude = lng,
            date = date,
            method = CalculationMethod.SINGAPORE,
            madhab = Madhab.SHAFI
        )

        val hanafiTimes = PrayerRepository.calculate(
            latitude = lat,
            longitude = lng,
            date = date,
            method = CalculationMethod.SINGAPORE,
            madhab = Madhab.HANAFI
        )

        assertTrue("Hanafi Asr should be later than Shafi Asr", hanafiTimes.asr > shafiTimes.asr)
        // Fajr, Dhuhr, and Maghrib should remain identical across Madhabs (within 1 second floating point tolerance)
        assertEquals(shafiTimes.fajr.epochSeconds, hanafiTimes.fajr.epochSeconds)
        assertEquals(shafiTimes.dhuhr.epochSeconds, hanafiTimes.dhuhr.epochSeconds)
        assertEquals(shafiTimes.maghrib.epochSeconds, hanafiTimes.maghrib.epochSeconds)
    }

    @Test
    fun calculationMethodsAffectFajrOrIsha() {
        val lat = -6.2088
        val lng = 106.8456
        val date = LocalDate(2026, 9, 20)

        val mwlTimes = PrayerRepository.calculate(
            latitude = lat,
            longitude = lng,
            date = date,
            method = CalculationMethod.MUSLIM_WORLD_LEAGUE
        )

        val singaporeTimes = PrayerRepository.calculate(
            latitude = lat,
            longitude = lng,
            date = date,
            method = CalculationMethod.SINGAPORE
        )

        // MUIS Singapore uses 20° fajr angle vs MWL 18°, so Fajr should differ
        assertTrue("Different calculation methods should produce different Fajr times", mwlTimes.fajr != singaporeTimes.fajr)
    }

    @Test
    fun widgetNarrowsToNextPrayersByWidth() {
        val base = Instant.parse("2026-09-20T00:00:00Z")
        val names = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
        val all = names.mapIndexed { i, name -> WidgetPrayer(name, "0${i + 1}:00", base + (i + 1).hours) }

        // 02:30 -> next is Asr (03:00)
        val now = base + 2.hours + 30.minutes
        assertEquals(listOf("Asr"), PrayerTimesWidget.visiblePrayers(all, now, 150.dp).map { it.name })
        assertEquals(
            listOf("Asr", "Maghrib", "Isha"),
            PrayerTimesWidget.visiblePrayers(all, now, 250.dp).map { it.name }
        )
        assertEquals(
            listOf("Asr", "Maghrib", "Isha", "Fajr", "Dhuhr"),
            PrayerTimesWidget.visiblePrayers(all, now, 320.dp).map { it.name }
        )
        // After Isha wraps to Fajr
        assertEquals(listOf("Fajr"), PrayerTimesWidget.visiblePrayers(all, base + 6.hours, 150.dp).map { it.name })
    }
}