package com.malla.mvp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationProvider {

    suspend fun getCurrentLocation(context: Context): Location? {
        if (!hasLocationPermission(context)) return null

        // Si Google Play Services no está disponible, usar LocationManager directamente
        if (!isGooglePlayServicesAvailable(context)) {
            return getLastKnownLocationFromManager(context)
        }

        val fused = LocationServices.getFusedLocationProviderClient(context)

        // Intentar obtener ubicación actual, con fallback a última conocida de Fused
        val fusedLocation = try {
            fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).awaitTask()
        } catch (e: Exception) {
            null
        } ?: try {
            fused.lastLocation.awaitTask()
        } catch (e: Exception) {
            null
        }

        // Si Fused falla, usar LocationManager
        return fusedLocation ?: getLastKnownLocationFromManager(context)
    }

    private fun hasLocationPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun isGooglePlayServicesAvailable(context: Context): Boolean {
        return try {
            val apiAvailability = GoogleApiAvailability.getInstance()
            apiAvailability.isGooglePlayServicesAvailable(context) == com.google.android.gms.common.ConnectionResult.SUCCESS
        } catch (e: Exception) {
            false
        }
    }

    private fun getLastKnownLocationFromManager(context: Context): Location? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        if (!hasLocationPermission(context)) return null
        var best: Location? = null
        try { best = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (_: Exception) {}
        if (best == null) try { best = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) {}
        if (best == null) try { best = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) } catch (_: Exception) {}
        return best
    }

    private suspend fun <T> Task<T>.awaitTask(): T? =
        suspendCancellableCoroutine { cont ->
            addOnSuccessListener { result ->
                if (cont.isActive) cont.resume(result)
            }
            addOnFailureListener { _ ->
                if (cont.isActive) cont.resume(null)
            }
            addOnCanceledListener {
                if (cont.isActive) cont.resume(null)
            }
        }
}
