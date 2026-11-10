package com.senac.viaja.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import com.senac.viaja.data.model.CatalogPlace

internal class NearbyPlaces(private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private var signal: CancellationSignal? = null
    private var timeout: Runnable? = null

    val hasPermission: Boolean
        get() = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun locate(onResult: (Location?) -> Unit) {
        cancel()
        val manager = context.getSystemService(LocationManager::class.java)
        if (!hasPermission || manager == null || !LocationManagerCompat.isLocationEnabled(manager)) {
            onResult(null)
            return
        }
        val enabled = manager.getProviders(true)
        val provider = listOf(LocationManager.FUSED_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .firstOrNull { it in enabled }
        if (provider == null) {
            onResult(null)
            return
        }

        val request = CancellationSignal()
        signal = request
        val finish: (Location?) -> Unit = { location ->
            if (signal === request) {
                cancel()
                onResult(location)
            }
        }
        timeout = Runnable { finish(null) }.also { handler.postDelayed(it, 12_000) }
        try {
            LocationManagerCompat.getCurrentLocation(manager, provider, request, ContextCompat.getMainExecutor(context)) {
                finish(it)
            }
        } catch (_: SecurityException) {
            finish(null)
        } catch (_: IllegalArgumentException) {
            finish(null)
        }
    }

    fun cancel() {
        timeout?.let(handler::removeCallbacks)
        timeout = null
        val active = signal
        signal = null
        active?.cancel()
    }
}

internal fun closestPlaces(location: Location, candidates: List<CatalogPlace>): List<Pair<CatalogPlace, Float>> {
    val distance = FloatArray(1)
    return candidates.mapNotNull { place ->
        val latitude = place.lat ?: return@mapNotNull null
        val longitude = place.lng ?: return@mapNotNull null
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return@mapNotNull null
        Location.distanceBetween(location.latitude, location.longitude, latitude, longitude, distance)
        if (distance[0] <= 20_000) place to distance[0] else null
    }.sortedBy { it.second }.take(3)
}
