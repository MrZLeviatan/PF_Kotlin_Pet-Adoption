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

Implementa la interfaz GeocodingRepository del dominio (capa de datos, fuente "device") y se
enlaza en RepositoryModule. La usan los casos de uso de ubicación para dos tareas:
buscar una dirección escrita y obtener sus coordenadas (search), y convertir un punto tocado
en el mapa en una dirección legible (reverse). Ambas devuelven Result para que la interfaz
pueda mostrar un error sin que la app se cierre.
**/
// Una sola instancia en toda la app: el Geocoder se crea una vez y se reutiliza.
@Singleton
// Hilt construye la clase inyectando el Context de la aplicación (no el de una Activity, para no filtrar memoria).
class AndroidGeocodingRepository @Inject constructor(
    // Context de la aplicación, necesario para crear el Geocoder.
    @ApplicationContext private val context: Context,
) : GeocodingRepository {

    // Geocoder perezoso: se crea la primera vez que se usa, con el idioma del teléfono para los nombres de lugares.
    private val geocoder by lazy { Geocoder(context, Locale.getDefault()) }

    // Busca lugares que coincidan con el texto escrito y los devuelve como ubicaciones del dominio.
    override suspend fun search(query: String, near: Location?, limit: Int): Result<List<Location>> = runCatching {
        // Si el teléfono no tiene servicio de geocodificación, lanza un error que runCatching convierte en fallo.
        if (!Geocoder.isPresent()) error("Geocoder no disponible")
        // Pide las direcciones usando la API adecuada según la versión de Android.
        addresses { callback ->
            // En Android 13+ se usa la versión asíncrona con callback.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) geocoder.getFromLocationName(query, limit, callback)
            // En versiones anteriores se usa la versión bloqueante (obsoleta) y se entrega el resultado al callback.
            else @Suppress("DEPRECATION") callback(geocoder.getFromLocationName(query, limit).orEmpty())
        // Convierte cada Address de Android en una Location del dominio.
        }.map { it.toLocation() }
    }

    // Convierte unas coordenadas en la dirección más cercana (geocodificación inversa).
    override suspend fun reverse(latitude: Double, longitude: Double): Result<Location?> = runCatching {
        // Falla de forma controlada si el servicio no existe en el teléfono.
        if (!Geocoder.isPresent()) error("Geocoder no disponible")
        // Pide solo una dirección para el punto dado.
        addresses { callback ->
            // En Android 13+ se usa la versión asíncrona con callback.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) geocoder.getFromLocation(latitude, longitude, 1, callback)
            // En versiones anteriores se usa la versión bloqueante (obsoleta).
            else @Suppress("DEPRECATION") callback(geocoder.getFromLocation(latitude, longitude, 1).orEmpty())
        // Toma la primera dirección encontrada (o null si no hubo) y la convierte al modelo del dominio.
        }.firstOrNull()?.toLocation()
    }

    // En Android 13+ el Geocoder responde por callback; antes es bloqueante (por eso va en IO).
    // Esta función unifica ambos casos convirtiendo el callback en una función suspendida.
    private suspend fun addresses(request: (callback: (List<Address>) -> Unit) -> Unit): List<Address> =
        // Ejecuta la petición en el hilo de entrada/salida para no congelar la interfaz.
        withContext(Dispatchers.IO) {
            // Suspende la corrutina hasta que el callback entregue el resultado; se puede cancelar.
            suspendCancellableCoroutine { continuation ->
                // Lanza la petición y reanuda la corrutina con el resultado solo si sigue activa (evita reanudar dos veces o tras cancelar).
                request { result -> if (continuation.isActive) continuation.resume(result) }
            }
        }

    // Convierte una Address de Android en la Location del dominio.
    private fun Address.toLocation() = Location(
        // Latitud del punto encontrado.
        latitude = latitude,
        // Longitud del punto encontrado.
        longitude = longitude,
        // Ciudad: usa la localidad y, si falta, el área administrativa más cercana disponible.
        city = locality ?: subAdminArea ?: adminArea.orEmpty(),
        // Nombre del país o vacío si no se conoce.
        country = countryName.orEmpty(),
        // Calle y número; si no hay, la primera línea de la dirección.
        address = listOfNotNull(thoroughfare, subThoroughfare).joinToString(" # ").ifBlank { getAddressLine(0).orEmpty() },
    )
}
