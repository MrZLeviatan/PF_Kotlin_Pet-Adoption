package co.edu.uniquindio.petadoptionapp.data.repository.device

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.repository.GeocodingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/**
Geocodificación con el Geocoder de Android: no necesita llaves (usa el servicio del teléfono).
Si el teléfono no tiene el servicio, falla y el mapa sigue funcionando tocando el punto.
 */
@Singleton
class AndroidGeocodingRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : GeocodingRepository {

    private val geocoder by lazy { Geocoder(context, Locale.getDefault()) }

    override suspend fun search(query: String, near: Location?, limit: Int): Result<List<Location>> = runCatching {
        if (!Geocoder.isPresent()) error("Geocoder no disponible")
        addresses { callback ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) geocoder.getFromLocationName(query, limit, callback)
            else @Suppress("DEPRECATION") callback(geocoder.getFromLocationName(query, limit).orEmpty())
        }.map { it.toLocation() }
    }

    override suspend fun reverse(latitude: Double, longitude: Double): Result<Location?> = runCatching {
        if (!Geocoder.isPresent()) error("Geocoder no disponible")
        addresses { callback ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) geocoder.getFromLocation(latitude, longitude, 1, callback)
            else @Suppress("DEPRECATION") callback(geocoder.getFromLocation(latitude, longitude, 1).orEmpty())
        }.firstOrNull()?.toLocation()
    }

    // En Android 13+ el Geocoder responde por callback; antes es bloqueante (por eso va en IO).
    private suspend fun addresses(request: (callback: (List<Address>) -> Unit) -> Unit): List<Address> =
        withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                request { result -> if (continuation.isActive) continuation.resume(result) }
            }
        }

    private fun Address.toLocation() = Location(
        latitude = latitude,
        longitude = longitude,
        city = locality ?: subAdminArea ?: adminArea.orEmpty(),
        country = countryName.orEmpty(),
        // Calle y número; si no hay, la primera línea de la dirección.
        address = listOfNotNull(thoroughfare, subThoroughfare).joinToString(" # ").ifBlank { getAddressLine(0).orEmpty() },
    )
}
