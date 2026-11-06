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
Lectura de la ubicación actual del dispositivo, usada por [rememberCurrentLocationRequester] (paquete map) una vez
que el permiso de ubicación ya fue concedido. No depende de Google Play Services: usa el [LocationManager] del
sistema, de modo que funciona también en dispositivos sin servicios de Google. Convierte la API de callbacks en una
función suspendida con [suspendCancellableCoroutine].
**/

/**
Ubicación actual del teléfono, sin servicios de Google (LocationManager del sistema).
Pide una lectura nueva (hasta 10 s) y, si no llega, usa la última conocida. Requiere el permiso ya concedido.
**/
@SuppressLint("MissingPermission")
suspend fun currentDeviceLocation(context: Context): GeoPoint? {
    // Obtiene el servicio de ubicación del sistema; si no existe, no hay ubicación.
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    // Proveedores a consultar (GPS y red), dejando solo los que estén activos.
    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        .filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }

    // getCurrentLocation solo existe desde Android 11 (API 30).
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        // Intenta cada proveedor en orden hasta obtener una lectura.
        for (provider in providers) {
            // Espera una lectura nueva como máximo 10 segundos; si se agota, el resultado es null.
            val fresh = withTimeoutOrNull(10_000) {
                // Convierte el callback del sistema en un valor suspendido.
                suspendCancellableCoroutine { continuation ->
                    // Pide una sola lectura actual; el resultado llega en el hilo principal.
                    manager.getCurrentLocation(provider, null, ContextCompat.getMainExecutor(context)) { location ->
                        // Solo reanuda si la corrutina sigue activa (no se canceló por tiempo).
                        if (continuation.isActive) continuation.resume(location)
                    }
                }
            }
            // Si se obtuvo una lectura, se devuelve como punto del mapa.
            if (fresh != null) return GeoPoint(fresh.latitude, fresh.longitude)
        }
    }
    // Última conocida (también en Android viejos): la más reciente entre los proveedores.
    return (providers + LocationManager.PASSIVE_PROVIDER)
        // Lee la última ubicación de cada proveedor, ignorando errores y valores nulos.
        .mapNotNull { runCatching { manager.getLastKnownLocation(it) }.getOrNull() }
        // Se queda con la más reciente.
        .maxByOrNull { it.time }
        // La convierte a punto del mapa.
        ?.let { GeoPoint(it.latitude, it.longitude) }
}
