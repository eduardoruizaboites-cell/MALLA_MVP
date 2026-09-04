package com.malla.mvp.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationProvider {

    suspend fun getCurrentLocation(context: Context): Location? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        val fused = LocationServices.getFusedLocationProviderClient(context)

        // Intentar obtener ubicación actual, con fallback a última conocida
        return try {
            fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).awaitTask()
        } catch (e: Exception) {
            fused.lastLocation.awaitTask()
        }
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
