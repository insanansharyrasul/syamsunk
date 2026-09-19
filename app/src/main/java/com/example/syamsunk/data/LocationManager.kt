package com.example.syamsunk.data

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Wraps [FusedLocationProviderClient] for a single high-accuracy location fix
 * and [Geocoder] for reverse geocoding into a human-readable address string.
 */
class LocationManager(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    data class LocationResult(
        val latitude: Double,
        val longitude: Double,
        val address: String
    )

    /**
     * Fetches current location with high accuracy.
     * Returns [LocationResult] with coordinates and reverse-geocoded address.
     */
    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(): LocationResult {
        val location = suspendCancellableCoroutine { cont ->
            val cts = CancellationTokenSource()
            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        cont.resume(loc)
                    } else {
                        // Fallback to last known location
                        fusedClient.lastLocation.addOnSuccessListener { last ->
                            if (last != null) {
                                cont.resume(last)
                            } else {
                                cont.resumeWithException(
                                    IllegalStateException("Unable to get location")
                                )
                            }
                        }.addOnFailureListener { e ->
                            cont.resumeWithException(e)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
            cont.invokeOnCancellation { cts.cancel() }
        }

        val address = reverseGeocode(location.latitude, location.longitude)
        return LocationResult(location.latitude, location.longitude, address)
    }

    /**
     * Resolves coordinates into an administrative location string.
     * Format: "Province, City, District (TimeZone)" e.g. "Jawa Barat, Kota Depok, Kecamatan Beji (WIB)"
     */
    @Suppress("DEPRECATION")
    private suspend fun reverseGeocode(lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(lat, lng, 1) { results ->
                        cont.resume(results)
                    }
                }
            } else {
                geocoder.getFromLocation(lat, lng, 1) ?: emptyList()
            }

            if (addresses.isNotEmpty()) {
                val addr = addresses[0]
                val parts = listOfNotNull(
                    addr.adminArea,      // Province (e.g., Jawa Barat)
                    addr.subAdminArea ?: addr.locality, // City (e.g., Kota Depok)
                    addr.subLocality     // District (e.g., Kecamatan Beji)
                )
                val tz = java.util.TimeZone.getDefault()
                val tzAbbr = tz.getDisplayName(false, java.util.TimeZone.SHORT, Locale.getDefault())
                if (parts.isNotEmpty()) {
                    "${parts.joinToString(", ")} ($tzAbbr)"
                } else {
                    addr.getAddressLine(0) ?: "Unknown location"
                }
            } else {
                "Lat: $lat, Lng: $lng"
            }
        } catch (e: Exception) {
            "Lat: $lat, Lng: $lng"
        }
    }
}
