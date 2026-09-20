package com.insan.syamsunk.data.model

import kotlinx.datetime.Instant

/**
 * Domain model holding the five daily prayer times as [Instant] values.
 */
data class DailyPrayerTimes(
    val fajr: Instant,
    val dhuhr: Instant,
    val asr: Instant,
    val maghrib: Instant,
    val isha: Instant
) {
    /** Returns prayer times as an ordered list of (name, time) pairs. */
    fun toList(): List<Pair<String, Instant>> = listOf(
        "Fajr" to fajr,
        "Dhuhr" to dhuhr,
        "Asr" to asr,
        "Maghrib" to maghrib,
        "Isha" to isha
    )
}
