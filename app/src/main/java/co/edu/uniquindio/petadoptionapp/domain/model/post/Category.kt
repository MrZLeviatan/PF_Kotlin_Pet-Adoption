package co.edu.uniquindio.petadoptionapp.domain.model.post

import kotlinx.serialization.Serializable


/**
 Categoría de publicación del catálogo del sistema.

 Precargada con: Adopción, Perdidos, Encontrados, Temporal y Veterinaria.
 Un moderador puede agregar más sin recompilar la app. Las publicaciones
 la referencian por [id].
 */
@Serializable
data class Category(
    val id: String = "",
    val name: String = "",
    // Oculta la categoría para publicaciones nuevas sin borrar las existentes.
    val active: Boolean = true,
    // `true` si las publicaciones de esta categoría admiten el botón "Me interesa adoptar".
    val allowsAdoptionRequests: Boolean = false,
    // Ícono del marcador en el mapa y en los filtros: una de las claves de [ICONS].
    val icon: String = DEFAULT_ICON,
    // Color del marcador, en hexadecimal (#RRGGBB). Vacío usa el color de la marca.
    val color: String = "",
    // `true` si representa un lugar (hogar de paso, veterinaria): el marcador es cuadrado en vez de redondo.
    val isPlace: Boolean = false
) {
    companion object {
        const val DEFAULT_ICON = "pets"

        // Íconos que la app sabe dibujar en el mapa.
        val ICONS = listOf("pets", "search", "flag", "home", "hospital", "heart", "star")
    }
}