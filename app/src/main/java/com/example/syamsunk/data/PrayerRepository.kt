package com.example.syamsunk.data

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.CalculationParameters
import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.DateComponents
import com.batoulapps.adhan2.Madhab
import com.batoulapps.adhan2.PrayerTimes
import com.example.syamsunk.data.model.DailyPrayerTimes
import kotlinx.datetime.LocalDate

/**
 * Calculates prayer times on-the-fly using the Adhan2 library.
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
            fajr = pt.fajr,
            dhuhr = pt.dhuhr,
            asr = pt.asr,
            maghrib = pt.maghrib,
            isha = pt.isha
        )
    }
}
