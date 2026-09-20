package com.insan.syamsunk

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.insan.syamsunk.data.PrayerRepository
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
}