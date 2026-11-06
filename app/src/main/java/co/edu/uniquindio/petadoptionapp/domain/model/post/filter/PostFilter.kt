package co.edu.uniquindio.petadoptionapp.domain.model.post.filter

import co.edu.uniquindio.petadoptionapp.domain.model.post.AnimalSize
import co.edu.uniquindio.petadoptionapp.domain.model.location.Location

/**
Modelo de dominio: filtros del feed. Todos son opcionales: un filtro en `null` no se aplica.
Lo arma la pantalla de explorar/feed y lo recibe GetFeedUseCase, que lo pasa a PostRepository.
**/
// Declara los filtros como clase de datos inmutable.
data class PostFilter(
    // Solo publicaciones de esta categoría.
    val categoryId: String? = null,
    // Solo publicaciones de este tipo de animal.
    val animalTypeId: String? = null,
    // Solo publicaciones de esta ciudad (filtro "en la ciudad").
    val city: String? = null,
    // Punto desde el que se mide la cercanía (normalmente la ubicación del usuario).
    val center: Location? = null,
    // Radio en kilómetros alrededor de [center] (filtro "cercanos").
    val radiusKm: Double? = null,
    // Tamaño del animal (null = cualquiera).
    val size: AnimalSize? = null,
    // Solo las que tienen las vacunas al día.
    val vaccinatedOnly: Boolean = false,
    // Orden del resultado.
    val sortBy: PostSort = PostSort.RECENT
) {

    // ¿El filtro de cercanía está completo (punto con coordenadas y radio positivo)?
    fun hasNearbyArea(): Boolean =
        // Exige un centro con coordenadas válidas y un radio mayor que cero.
        center?.hasCoordinates() == true && radiusKm != null && radiusKm > 0
}
