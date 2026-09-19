package com.example.syamsunk.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "syamsunk_prefs")

/**
 * Persists location data and settings via Jetpack Preferences DataStore.
 * Zero DB overhead — simple key-value storage.
 */
class PreferencesRepository(private val context: Context) {

    companion object {
        private val KEY_LATITUDE = doublePreferencesKey("latitude")
        private val KEY_LONGITUDE = doublePreferencesKey("longitude")
        private val KEY_ADDRESS = stringPreferencesKey("address")
        private val KEY_CALC_METHOD = stringPreferencesKey("calculation_method")
    }

    data class LocationData(
        val latitude: Double,
        val longitude: Double,
        val address: String
    )

    val locationFlow: Flow<LocationData?> = context.dataStore.data.map { prefs ->
        val lat = prefs[KEY_LATITUDE]
        val lng = prefs[KEY_LONGITUDE]
        val addr = prefs[KEY_ADDRESS]
        if (lat != null && lng != null) {
            LocationData(lat, lng, addr ?: "")
        } else null
    }

    val calculationMethodFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_CALC_METHOD] ?: "MUSLIM_WORLD_LEAGUE"
    }

    suspend fun saveLocation(latitude: Double, longitude: Double, address: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LATITUDE] = latitude
            prefs[KEY_LONGITUDE] = longitude
            prefs[KEY_ADDRESS] = address
        }
    }

    suspend fun saveCalculationMethod(method: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CALC_METHOD] = method
        }
    }
}
