package com.example.syamsunk.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan.CalculationMethod
import com.example.syamsunk.data.LocationManager
import com.example.syamsunk.data.PrayerRepository
import com.example.syamsunk.data.PreferencesRepository
import com.example.syamsunk.data.model.DailyPrayerTimes
import com.example.syamsunk.service.AdhanAlarmManager
import com.example.syamsunk.service.DailyRescheduleWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Locale

data class PrayerUiState(
    val isLoading: Boolean = true,
    val address: String = "",
    val prayerTimes: List<PrayerTimeItem> = emptyList(),
    val nextPrayerName: String = "",
    val nextPrayerTime: String = "",
    val countdownText: String = "",
    val activePrayerIndex: Int = -1,
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
            if (cached != null) {
                calculateAndUpdate(cached.latitude, cached.longitude, cached.address)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
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
                calculateAndUpdate(result.latitude, result.longitude, result.address)
                scheduleAlarms(result.latitude, result.longitude)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Location fetch failed"
                )
            }
        }
    }

    private fun calculateAndUpdate(lat: Double, lng: Double, address: String) {
        val tz = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val today = now.toLocalDateTime(tz).date

        val daily = PrayerRepository.calculate(lat, lng, today)
        val items = daily.toList().map { (name, instant) ->
            val lt = instant.toLocalDateTime(tz)
            PrayerTimeItem(name, String.format(Locale.getDefault(), "%02d:%02d", lt.hour, lt.minute), instant)
        }

        // Find next prayer (first one after now)
        val nextIndex = items.indexOfFirst { it.instant > now }
        val activeIndex = if (nextIndex >= 0) nextIndex else 0

        val nextPrayer = items.getOrNull(activeIndex)
        val countdown = nextPrayer?.let { computeCountdown(now, it.instant) } ?: ""

        _uiState.value = PrayerUiState(
            isLoading = false,
            address = address,
            prayerTimes = items,
            nextPrayerName = nextPrayer?.name ?: "",
            nextPrayerTime = nextPrayer?.time ?: "",
            countdownText = countdown,
            activePrayerIndex = activeIndex
        )
    }

    private fun scheduleAlarms(lat: Double, lng: Double) {
        val context = getApplication<Application>()
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        val daily = PrayerRepository.calculate(lat, lng, today)
        AdhanAlarmManager.cancelAlarms(context)
        AdhanAlarmManager.scheduleAlarms(context, daily)
        DailyRescheduleWorker.enqueueDaily(context)
    }

    /** Ticks every second to update the countdown display */
    private fun startCountdownTicker() {
        viewModelScope.launch {
            while (true) {
                delay(1000)
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
                        // All prayers passed - recalculate for tomorrow
                        val cached = prefsRepo.locationFlow.first()
                        if (cached != null) {
                            calculateAndUpdate(cached.latitude, cached.longitude, cached.address)
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
