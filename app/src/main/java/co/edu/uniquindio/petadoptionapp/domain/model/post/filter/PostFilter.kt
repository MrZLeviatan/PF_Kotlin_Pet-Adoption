package co.edu.uniquindio.petadoptionapp.domain.model.post.filter

import co.edu.uniquindio.petadoptionapp.domain.model.location.Location

/**
Filtros del feed. Todos son opcionales: un filtro en `null` no se aplica.
 */
data class PostFilter(
    // Solo publicaciones de esta categoria.
    val categoryId: String? = null,
    // Solo publicaciones de este tipo de animal.
    val animalTypeId: String? = null,
    // Solo publicaciones de esta ciudad (filtro "en la ciudad")
    val city: String? = null,
    // Punto desde el que se mide la cercanía (normalmente la ubicación del usuario).
    val center: Location? = null,
    // Radio en kilómetros alrededor de [center] (filtro "cercanos")
    val radiusKm: Double? = null,
    // Orden del resultado.
    val sortBy: PostSort = PostSort.RECENT
) {

    // ¿El filtro de cercanía está completo (punto con coordenadas y radio positivo)?
    fun hasNearbyArea(): Boolean =
        center?.hasCoordinates() == true && radiusKm != null && radiusKm > 0
}