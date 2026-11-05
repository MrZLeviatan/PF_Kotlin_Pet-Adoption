package co.edu.uniquindio.petadoptionapp.domain.usecase.location

import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.repository.GeocodingRepository

/**
Documenta el caso de uso que busca direcciones mientras se escribe (filtro del mapa).
Agrega la ciudad elegida al texto para que los resultados sean de ahí, y no busca con textos muy cortos.
 */
class SearchAddressUseCase(
    private val geocoding: GeocodingRepository
) {
    companion object {
        const val MIN_QUERY_LENGTH = 3
        const val MAX_RESULTS = 5
    }

    suspend operator fun invoke(query: String, city: Location?): Result<List<Location>> {
        val clean = query.trim()
        if (clean.length < MIN_QUERY_LENGTH) return Result.success(emptyList())
        val full = listOfNotNull(clean, city?.city?.takeIf { it.isNotBlank() && !clean.contains(it, ignoreCase = true) }, city?.country?.takeIf { it.isNotBlank() })
            .joinToString(", ")
        return geocoding.search(full, near = city, limit = MAX_RESULTS)
            .map { list -> list.filter { it.hasCoordinates() } }
    }
}
