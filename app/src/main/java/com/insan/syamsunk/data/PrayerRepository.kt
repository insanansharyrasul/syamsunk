package com.insan.syamsunk.data

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.data.DateComponents
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.insan.syamsunk.data.model.DailyPrayerTimes
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Calculates prayer times on-the-fly using the Adhan library.
 * No database storage — computation is fast enough for real-time use.
 */
object PrayerRepository {

    fun calculate(
        latitude: Double,
        longitude: Double,
        date: LocalDate,
        method: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
        madhab: Madhab = Madhab.SHAFI
    ): DailyPrayerTimes {
        val coordinates = Coordinates(latitude, longitude)
        val dateComponents = DateComponents(date.year, date.monthNumber, date.dayOfMonth)
        val params = method.parameters
        params.madhab = madhab
        val pt = PrayerTimes(coordinates, dateComponents, params)
        return DailyPrayerTimes(
            fajr = Instant.fromEpochMilliseconds(pt.fajr.time),
            dhuhr = Instant.fromEpochMilliseconds(pt.dhuhr.time),
            asr = Instant.fromEpochMilliseconds(pt.asr.time),
            maghrib = Instant.fromEpochMilliseconds(pt.maghrib.time),
            isha = Instant.fromEpochMilliseconds(pt.isha.time)
        )
    }
}
