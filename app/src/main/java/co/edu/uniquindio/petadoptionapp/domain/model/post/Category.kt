package co.edu.uniquindio.petadoptionapp.domain.model.post


/**
 Categoría de publicación del catálogo del sistema.

 Precargada con: Adopción, Perdidos, Encontrados, Temporal y Veterinaria.
 Un moderador puede agregar más sin recompilar la app. Las publicaciones
 la referencian por [id].
 */
data class Category(
    val id: String = "",
    val name: String = "",
    // Oculta la categoría para publicaciones nuevas sin borrar las existentes.
    val active: Boolean = true
)