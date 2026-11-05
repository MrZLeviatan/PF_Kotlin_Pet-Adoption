package co.edu.uniquindio.petadoptionapp.domain.usecase.location

import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.repository.GeocodingRepository

/**
Documenta el caso de uso que convierte un punto del mapa en una ubicación con dirección.
Si no se encuentra dirección (sin internet o en medio del campo), igual devuelve el punto con la ciudad de respaldo:
lo importante para la app son las coordenadas.
 */
class ReverseGeocodeUseCase(
    private val geocoding: GeocodingRepository
) {
    suspend operator fun invoke(latitude: Double, longitude: Double, fallbackCity: String = "", fallbackCountry: String = ""): Location {
        val found = geocoding.reverse(latitude, longitude).getOrNull()
        return Location(
            latitude = latitude,
            longitude = longitude,
            city = found?.city?.takeIf { it.isNotBlank() } ?: fallbackCity,
            country = found?.country?.takeIf { it.isNotBlank() } ?: fallbackCountry,
            address = found?.address?.takeIf { it.isNotBlank() } ?: String.format(java.util.Locale.US, "%.5f, %.5f", latitude, longitude),
        )
    }
}
