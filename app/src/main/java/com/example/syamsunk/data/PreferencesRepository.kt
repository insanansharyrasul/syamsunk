package com.example.syamsunk.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "syamsunk_prefs")

/**
 * Persists location data and calculation settings via Jetpack Preferences DataStore.
 * Zero DB overhead — simple key-value storage.
 */
class PreferencesRepository(private val context: Context) {

    companion object {
        private val KEY_LATITUDE = doublePreferencesKey("latitude")
        private val KEY_LONGITUDE = doublePreferencesKey("longitude")
        private val KEY_ADDRESS = stringPreferencesKey("address")
        private val KEY_CALC_METHOD = stringPreferencesKey("calculation_method")
        private val KEY_MADHAB = stringPreferencesKey("madhab")
    }

    data class LocationData(
        val latitude: Double,
        val longitude: Double,
        val address: String
    )

    data class UserSettings(
        val calculationMethod: CalculationMethod = CalculationMethod.MUSLIM_WORLD_LEAGUE,
        val madhab: Madhab = Madhab.SHAFI
    )

    val locationFlow: Flow<LocationData?> = context.dataStore.data.map { prefs ->
        val lat = prefs[KEY_LATITUDE]
        val lng = prefs[KEY_LONGITUDE]
        val addr = prefs[KEY_ADDRESS]
        if (lat != null && lng != null) {
            LocationData(lat, lng, addr ?: "")
        } else null
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val methodRaw = prefs[KEY_CALC_METHOD]
        val madhabRaw = prefs[KEY_MADHAB]
        UserSettings(
            calculationMethod = methodRaw?.let { runCatching { CalculationMethod.valueOf(it) }.getOrNull() }
                ?: CalculationMethod.MUSLIM_WORLD_LEAGUE,
            madhab = madhabRaw?.let { runCatching { Madhab.valueOf(it) }.getOrNull() }
                ?: Madhab.SHAFI
        )
    }

    val calculationMethodFlow: Flow<CalculationMethod> = userSettingsFlow.map { it.calculationMethod }

    val madhabFlow: Flow<Madhab> = userSettingsFlow.map { it.madhab }

    suspend fun saveLocation(latitude: Double, longitude: Double, address: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LATITUDE] = latitude
            prefs[KEY_LONGITUDE] = longitude
            prefs[KEY_ADDRESS] = address
        }
    }

    suspend fun saveCalculationMethod(method: CalculationMethod) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CALC_METHOD] = method.name
        }
    }

    suspend fun saveCalculationMethod(method: String) {
        val calcMethod = runCatching { CalculationMethod.valueOf(method) }.getOrDefault(CalculationMethod.MUSLIM_WORLD_LEAGUE)
        saveCalculationMethod(calcMethod)
    }

    suspend fun saveMadhab(madhab: Madhab) {
        context.dataStore.edit { prefs ->
            prefs[KEY_MADHAB] = madhab.name
        }
    }

    suspend fun saveSettings(method: CalculationMethod, madhab: Madhab) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CALC_METHOD] = method.name
            prefs[KEY_MADHAB] = madhab.name
        }
    }
}
