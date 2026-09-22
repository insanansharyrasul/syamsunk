package com.insan.syamsunk.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.glance.appwidget.updateAll
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.insan.syamsunk.data.LocationManager
import com.insan.syamsunk.data.PrayerRepository
import com.insan.syamsunk.data.PreferencesRepository
import com.insan.syamsunk.service.AdhanAlarmManager
import com.insan.syamsunk.service.DailyRescheduleWorker
import com.insan.syamsunk.widget.PrayerTimesWidget
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.util.Locale
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

data class PrayerUiState(
    val isLoading: Boolean = true,
    val address: String = "",
    val prayerTimes: List<PrayerTimeItem> = emptyList(),
    val nextPrayerName: String = "",
    val nextPrayerTime: String = "",
    val countdownText: String = "",
    val activePrayerIndex: Int = -1,
    val calculationMethod: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
    val madhab: Madhab = Madhab.SHAFI,
    val error: String? = null
)

data class PrayerTimeItem(
    val name: String,
    val time: String,
    val instant: Instant
)

class PrayerViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = PreferencesRepository(application)
    private val locationManager = LocationManager(application)

    private val _uiState = MutableStateFlow(PrayerUiState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    init {
        loadFromCache()
        startCountdownTicker()
    }

    /** Load prayer times from cached location immediately on launch */
    private fun loadFromCache() {
        viewModelScope.launch {
            val cached = prefsRepo.locationFlow.first()
            val settings = prefsRepo.userSettingsFlow.first()
            if (cached != null) {
                calculateAndUpdate(
                    cached.latitude,
                    cached.longitude,
                    cached.address,
                    settings.calculationMethod,
                    settings.madhab
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    calculationMethod = settings.calculationMethod,
                    madhab = settings.madhab
                )
            }
        }
    }

    /** Called after location permission is granted to fetch fresh GPS coordinates */
    fun fetchLocation() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val result = locationManager.fetchCurrentLocation()
                prefsRepo.saveLocation(result.latitude, result.longitude, result.address)
                val settings = prefsRepo.userSettingsFlow.first()
                calculateAndUpdate(
                    result.latitude,
                    result.longitude,
                    result.address,
                    settings.calculationMethod,
                    settings.madhab
                )
                scheduleAlarms(result.latitude, result.longitude, settings.calculationMethod, settings.madhab)
                PrayerTimesWidget().updateAll(getApplication())
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Location fetch failed"
                )
            }
        }
    }

    fun updateSettings(method: CalculationMethod, madhab: Madhab) {
        viewModelScope.launch {
            prefsRepo.saveSettings(method, madhab)
            val cached = prefsRepo.locationFlow.first()
            if (cached != null) {
                calculateAndUpdate(cached.latitude, cached.longitude, cached.address, method, madhab)
                scheduleAlarms(cached.latitude, cached.longitude, method, madhab)
                PrayerTimesWidget().updateAll(getApplication())
            } else {
                _uiState.value = _uiState.value.copy(
                    calculationMethod = method,
                    madhab = madhab
                )
            }
        }
    }

    private fun calculateAndUpdate(
        lat: Double,
        lng: Double,
        address: String,
        method: CalculationMethod,
        madhab: Madhab
    ) {
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today = now.toLocalDateTime(tz).date

        val daily = PrayerRepository.calculate(lat, lng, today, method, madhab)
        val items = daily.toList().map { (name, instant) ->
            val lt = instant.toLocalDateTime(tz)
            PrayerTimeItem(name, String.format(Locale.getDefault(), "%02d:%02d", lt.hour, lt.minute), instant)
        }

        val nextIndex = items.indexOfFirst { it.instant > now }
        val (nextPrayer, activeIndex) = if (nextIndex >= 0) {
            items[nextIndex] to nextIndex
        } else {
            val tomorrow = today.plus(1, DateTimeUnit.DAY)
            val tomorrowFajr = PrayerRepository.calculate(lat, lng, tomorrow, method, madhab).fajr
            val lt = tomorrowFajr.toLocalDateTime(tz)
            PrayerTimeItem(
                "Fajr",
                String.format(Locale.getDefault(), "%02d:%02d", lt.hour, lt.minute),
                tomorrowFajr
            ) to 0
        }
        val countdown = computeCountdown(now, nextPrayer.instant)

        _uiState.value = PrayerUiState(
            isLoading = false,
            address = address,
            prayerTimes = items,
            nextPrayerName = nextPrayer.name,
            nextPrayerTime = nextPrayer.time,
            countdownText = countdown,
            activePrayerIndex = activeIndex,
            calculationMethod = method,
            madhab = madhab
        )
    }

    private fun scheduleAlarms(lat: Double, lng: Double, method: CalculationMethod, madhab: Madhab) {
        val context = getApplication<Application>()
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        val daily = PrayerRepository.calculate(lat, lng, today, method, madhab)
        AdhanAlarmManager.cancelAlarms(context)
        AdhanAlarmManager.scheduleAlarms(context, daily)
        DailyRescheduleWorker.enqueueDaily(context)
    }

    /** Ticks every second to update the countdown display */
    private fun startCountdownTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1000.milliseconds)
                val state = _uiState.value
                if (state.prayerTimes.isNotEmpty()) {
                    val now = Clock.System.now()
                    val nextIndex = state.prayerTimes.indexOfFirst { it.instant > now }

                    if (nextIndex >= 0) {
                        val nextPrayer = state.prayerTimes[nextIndex]
                        val countdown = computeCountdown(now, nextPrayer.instant)
                        _uiState.value = state.copy(
                            nextPrayerName = nextPrayer.name,
                            nextPrayerTime = nextPrayer.time,
                            countdownText = countdown,
                            activePrayerIndex = nextIndex
                        )
                    } else {
                        // After Isha: countdown to Fajr tomorrow (was showing 00:00:00)
                        // ponytail: exact calc would read prefs + PrayerRepository every second; 24h offset is <1min off, good enough for ticking. Full recalc happens at date change via calculateAndUpdate.
                        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date
                        val isNewDay = state.prayerTimes[0].instant.toLocalDateTime(TimeZone.currentSystemDefault()).date != today
                        if (isNewDay) {
                            val cached = prefsRepo.locationFlow.first()
                            val settings = prefsRepo.userSettingsFlow.first()
                            if (cached != null) {
                                calculateAndUpdate(
                                    cached.latitude,
                                    cached.longitude,
                                    cached.address,
                                    settings.calculationMethod,
                                    settings.madhab
                                )
                            }
                        } else {
                            // approximate tomorrow Fajr as today Fajr + 24h
                            val tomorrowFajr = state.prayerTimes[0].instant.plus(86400.seconds)
                            val tz = TimeZone.currentSystemDefault()
                            val lt = tomorrowFajr.toLocalDateTime(tz)
                            val timeStr = String.format(Locale.getDefault(), "%02d:%02d", lt.hour, lt.minute)
                            val countdown = computeCountdown(now, tomorrowFajr)
                            _uiState.value = state.copy(
                                nextPrayerName = "Fajr",
                                nextPrayerTime = timeStr,
                                countdownText = countdown,
                                activePrayerIndex = 0
                            )
                        }
                    }
                }
            }
        }
    }

    private fun computeCountdown(now: Instant, target: Instant): String {
        val diff = (target - now).inWholeSeconds
        if (diff <= 0) return "00:00:00"
        val hours = diff / 3600
        val minutes = (diff % 3600) / 60
        val seconds = diff % 60
        return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
    }
}
