package co.edu.uniquindio.petadoptionapp.domain.usecase.location

import co.edu.uniquindio.petadoptionapp.domain.model.location.Location
import co.edu.uniquindio.petadoptionapp.domain.repository.GeocodingRepository

/**
Caso de uso que busca direcciones mientras se escribe (filtro del mapa).
Agrega la ciudad elegida al texto para que los resultados sean de ahí, y no busca con textos muy cortos.
Lo usan los buscadores de dirección del mapa y de los selectores de ubicación.
**/
// Declara la clase del caso de uso con sus dependencias inyectadas.
class SearchAddressUseCase(
    // Inyecta el repositorio de geocodificación.
    private val geocoding: GeocodingRepository
) {
    // Constantes de configuración de la búsqueda.
    companion object {
        // Mínimo de caracteres para empezar a buscar.
        const val MIN_QUERY_LENGTH = 3
        // Máximo de resultados que se muestran.
        const val MAX_RESULTS = 5
    }

    // Función principal: busca direcciones para el texto, cerca de la ciudad elegida.
    suspend operator fun invoke(query: String, city: Location?): Result<List<Location>> {
        // Quita espacios sobrantes del texto.
        val clean = query.trim()
        // Con textos muy cortos no busca y devuelve una lista vacía.
        if (clean.length < MIN_QUERY_LENGTH) return Result.success(emptyList())
        // Arma el texto completo: consulta + ciudad (si no está ya escrita) + país, separados por comas.
        val full = listOfNotNull(clean, city?.city?.takeIf { it.isNotBlank() && !clean.contains(it, ignoreCase = true) }, city?.country?.takeIf { it.isNotBlank() })
            .joinToString(", ")
        // Busca en el servicio de geocodificación.
        return geocoding.search(full, near = city, limit = MAX_RESULTS)
            // Descarta los resultados sin coordenadas válidas.
            .map { list -> list.filter { it.hasCoordinates() } }
    }
}
