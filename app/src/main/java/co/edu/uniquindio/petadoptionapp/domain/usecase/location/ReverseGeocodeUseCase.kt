package co.edu.uniquindio.petadoptionapp.domain.usecase.location

import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.repository.GeocodingRepository

/**
Caso de uso que convierte un punto del mapa en una ubicación con dirección.
Si no se encuentra dirección (sin internet o en medio del campo), igual devuelve el punto con la ciudad de respaldo:
lo importante para la app son las coordenadas.
Lo usan los selectores de ubicación (registro, publicar, perfil) al tocar el mapa o usar la ubicación actual.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class ReverseGeocodeUseCase(
    // Inyecta el repositorio de geocodificación.
    private val geocoding: GeocodingRepository
) {
    // Función principal: recibe el punto y la ciudad/país de respaldo, y devuelve siempre una ubicación.
    suspend operator fun invoke(latitude: Double, longitude: Double, fallbackCity: String = "", fallbackCountry: String = ""): Location {
        // Pide la dirección del punto; si falla, queda en null.
        val found = geocoding.reverse(latitude, longitude).getOrNull()
        // Construye la ubicación combinando lo encontrado con los valores de respaldo.
        return Location(
            // Conserva la latitud exacta del punto.
            latitude = latitude,
            // Conserva la longitud exacta del punto.
            longitude = longitude,
            // Ciudad encontrada o, si viene vacía, la de respaldo.
            city = found?.city?.takeIf { it.isNotBlank() } ?: fallbackCity,
            // País encontrado o, si viene vacío, el de respaldo.
            country = found?.country?.takeIf { it.isNotBlank() } ?: fallbackCountry,
            // Dirección encontrada o, si no hay, las coordenadas con 5 decimales como texto.
            address = found?.address?.takeIf { it.isNotBlank() } ?: String.format(java.util.Locale.US, "%.5f, %.5f", latitude, longitude),
        )
    }
}
