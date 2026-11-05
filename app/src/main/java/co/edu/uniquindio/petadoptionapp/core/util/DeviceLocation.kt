package co.edu.uniquindio.petadoptionapp.core.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import co.edu.uniquindio.petadoptionapp.core.component.map.GeoPoint
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/**
 Ubicación actual del teléfono, sin servicios de Google (LocationManager del sistema).
 Pide una lectura nueva (hasta 10 s) y, si no llega, usa la última conocida. Requiere el permiso ya concedido.
 */
@SuppressLint("MissingPermission")
suspend fun currentDeviceLocation(context: Context): GeoPoint? {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        .filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        for (provider in providers) {
            val fresh = withTimeoutOrNull(10_000) {
                suspendCancellableCoroutine { continuation ->
                    manager.getCurrentLocation(provider, null, ContextCompat.getMainExecutor(context)) { location ->
                        if (continuation.isActive) continuation.resume(location)
                    }
                }
            }
            if (fresh != null) return GeoPoint(fresh.latitude, fresh.longitude)
        }
    }
    // Última conocida (también en Android viejos): la más reciente entre los proveedores.
    return (providers + LocationManager.PASSIVE_PROVIDER)
        .mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
        .maxByOrNull { it.time }
        ?.let { GeoPoint(it.latitude, it.longitude) }
}
